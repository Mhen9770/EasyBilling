package com.easybilling.service;

import com.easybilling.dto.LoginRequest;
import com.easybilling.dto.LoginResponse;
import com.easybilling.dto.RefreshTokenRequest;
import com.easybilling.dto.RegisterRequest;
import com.easybilling.dto.OnboardRequest;
import com.easybilling.dto.TenantRequest;
import com.easybilling.entity.User;
import com.easybilling.entity.Tenant;
import com.easybilling.repository.UserRepository;
import com.easybilling.exception.BusinessException;
import com.easybilling.exception.UnauthorizedException;
import com.easybilling.resolver.JwtTokenProvider;
import com.easybilling.service.TenantService;
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
    private final TenantService tenantService;
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;
    
    /**
     * Authenticate user and generate tokens.
     * Tenant ID is optional - if not provided, it will be resolved from the user record.
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());
        
        User user;
        
        // If tenant ID is provided, use it (for subdomain-based routing or explicit tenant selection)
        if (request.getTenantId() != null) {
            user = userRepository.findByUsernameAndTenantId(request.getUsername(), Integer.parseInt(request.getTenantId()))
                    .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
        } else {
            // Find user by username/email (tenant ID will be extracted from user record)
            // Try username first
            user = userRepository.findByUsername(request.getUsername())
                    .orElse(null);
            
            // If not found by username, try email
            if (user == null) {
                user = userRepository.findByEmail(request.getUsername())
                        .orElse(null);
            }
            
            if (user == null) {
                throw new UnauthorizedException("Invalid username or password");
            }
            
            // If multiple users with same username exist across tenants, we found the first one
            // In production, you might want to return a list of tenants for user to select
            log.debug("User found without tenant ID, using tenant: {}", user.getTenantId());
        }
        
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
        
        log.info("User logged in successfully: {} for tenant: {}", user.getUsername(), user.getTenantId());
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L) // 1 hour
                .user(buildUserInfo(user))
                .build();
    }
    
    /**
     * Tenant onboarding - creates tenant and first admin user.
     */
    @Transactional
    public LoginResponse onboard(OnboardRequest request) {
        log.info("Tenant onboarding attempt for business: {}", request.getBusinessName());
        
        // Step 1: Create tenant
        TenantRequest tenantRequest = TenantRequest.builder()
                .name(request.getBusinessName())
                .slug(generateSlug(request.getTenantName()))
                .description("Business Type: " + (request.getBusinessType() != null ? request.getBusinessType() : "Retail"))
                .plan(Tenant.SubscriptionPlan.BASIC) // Default to BASIC plan for new tenants
                .contactEmail(request.getTenantEmail())
                .contactPhone(request.getTenantPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry() != null ? request.getCountry() : "India")
                .postalCode(request.getPincode())
                .taxNumber(request.getGstin())
                .maxUsers(5) // Default limits for BASIC plan
                .maxStores(1)
                .build();
        
        var tenantResponse = tenantService.createTenant(tenantRequest);
        Integer tenantId = tenantResponse.getId();
        
        log.info("Tenant created successfully: {} with ID: {}", tenantResponse.getName(), tenantId);
        
        // Step 2: Create admin user for the tenant
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(request.getAdminUsername())
                .email(request.getAdminEmail())
                .password(request.getAdminPassword())
                .firstName(request.getAdminFirstName())
                .lastName(request.getAdminLastName())
                .phone(request.getAdminPhone())
                .tenantId(tenantId)
                .build();
        
        // Create admin user with ROLE_ADMIN
        User adminUser = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .phone(registerRequest.getPhone())
                .tenantId(tenantId)
                .status(User.UserStatus.ACTIVE)
                .roles(new HashSet<>())
                .failedLoginAttempts(0)
                .passwordChangedAt(Instant.now())
                .build();
        
        // Add admin role
        adminUser.addRole("ROLE_ADMIN");
        adminUser.addRole("ROLE_USER");
        
        adminUser = userRepository.save(adminUser);
        
        log.info("Admin user created successfully: {} for tenant: {}", adminUser.getUsername(), tenantId);
        
        // Step 3: Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                adminUser.getId(),
                adminUser.getTenantId(),
                new ArrayList<>(adminUser.getRoles())
        );
        
        String refreshToken = jwtTokenProvider.generateRefreshToken(adminUser.getId());
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(buildUserInfo(adminUser))
                .build();
    }
    
    /**
     * Generate a slug from tenant name.
     */
    private String generateSlug(String tenantName) {
        if (tenantName == null || tenantName.trim().isEmpty()) {
            throw new BusinessException("INVALID_TENANT_NAME", "Tenant name is required");
        }
        
        // Convert to lowercase, replace spaces with hyphens, remove special characters
        return tenantName.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
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
