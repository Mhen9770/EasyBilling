package com.easybilling.auth.service;

import com.easybilling.auth.dto.LoginRequest;
import com.easybilling.auth.dto.LoginResponse;
import com.easybilling.auth.dto.RefreshTokenRequest;
import com.easybilling.auth.dto.RegisterRequest;
import com.easybilling.auth.entity.User;
import com.easybilling.auth.repository.UserRepository;
import com.easybilling.common.exception.BusinessException;
import com.easybilling.common.exception.UnauthorizedException;
import com.easybilling.multitenancy.context.TenantContext;
import com.easybilling.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Service for authentication operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;
    
    /**
     * Authenticate user and generate tokens.
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());
        
        // Resolve tenant ID
        String tenantId = request.getTenantId();
        if (tenantId == null) {
            tenantId = TenantContext.getTenantId();
        }
        
        if (tenantId == null) {
            throw new BusinessException("TENANT_REQUIRED", "Tenant ID is required for login");
        }
        
        // Find user
        User user = userRepository.findByUsernameAndTenantId(request.getUsername(), tenantId)
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
        
        // Check account status
        checkAccountStatus(user);
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user);
            throw new UnauthorizedException("Invalid username or password");
        }
        
        // Reset failed attempts on successful login
        user.setFailedLoginAttempts(0);
        user.setLastLogin(Instant.now());
        userRepository.save(user);
        
        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getTenantId(),
                new ArrayList<>(user.getRoles())
        );
        
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        log.info("User logged in successfully: {}", user.getUsername());
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L) // 1 hour
                .user(buildUserInfo(user))
                .build();
    }
    
    /**
     * Register a new user.
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());
        
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("USERNAME_EXISTS", "Username already exists");
        }
        
        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("EMAIL_EXISTS", "Email already exists");
        }
        
        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .tenantId(request.getTenantId())
                .status(User.UserStatus.ACTIVE)
                .roles(new HashSet<>())
                .failedLoginAttempts(0)
                .passwordChangedAt(Instant.now())
                .build();
        
        // Add default role
        user.addRole("ROLE_USER");
        
        user = userRepository.save(user);
        
        log.info("User registered successfully: {}", user.getUsername());
        
        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getTenantId(),
                new ArrayList<>(user.getRoles())
        );
        
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(buildUserInfo(user))
                .build();
    }
    
    /**
     * Refresh access token using refresh token.
     */
    @Transactional(readOnly = true)
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        log.debug("Token refresh attempt");
        
        String refreshToken = request.getRefreshToken();
        
        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        
        // Check token type
        String tokenType = jwtTokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new UnauthorizedException("Invalid token type");
        }
        
        // Get user
        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        
        // Check account status
        checkAccountStatus(user);
        
        // Generate new access token
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getTenantId(),
                new ArrayList<>(user.getRoles())
        );
        
        log.info("Token refreshed for user: {}", user.getUsername());
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken) // Return same refresh token
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(buildUserInfo(user))
                .build();
    }
    
    /**
     * Check account status and lock state.
     */
    private void checkAccountStatus(User user) {
        if (user.getStatus() == User.UserStatus.INACTIVE) {
            throw new UnauthorizedException("Account is inactive");
        }
        
        if (user.getStatus() == User.UserStatus.LOCKED) {
            throw new UnauthorizedException("Account is locked. Contact administrator.");
        }
        
        if (user.isAccountLocked()) {
            throw new UnauthorizedException("Account is temporarily locked. Try again later.");
        }
    }
    
    /**
     * Handle failed login attempt.
     */
    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLockedUntil(Instant.now().plus(LOCK_DURATION_MINUTES, ChronoUnit.MINUTES));
            log.warn("Account locked due to failed login attempts: {}", user.getUsername());
        }
        
        userRepository.save(user);
    }
    
    /**
     * Build user info response.
     */
    private LoginResponse.UserInfo buildUserInfo(User user) {
        return LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .tenantId(user.getTenantId())
                .roles(user.getRoles())
                .build();
    }
}
