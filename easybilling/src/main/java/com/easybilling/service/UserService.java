package com.easybilling.service;

import com.easybilling.dto.AssignSecurityGroupRequest;
import com.easybilling.dto.ChangePasswordRequest;
import com.easybilling.dto.CreateUserRequest;
import com.easybilling.dto.UpdateProfileRequest;
import com.easybilling.dto.UserProfileResponse;
import com.easybilling.entity.User;
import com.easybilling.enums.UserRole;
import com.easybilling.repository.UserRepository;
import com.easybilling.dto.PageResponse;
import com.easybilling.exception.BusinessException;
import com.easybilling.exception.ResourceNotFoundException;
import com.easybilling.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.HashSet;

/**
 * Service for user management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityGroupService securityGroupService;
    
    /**
     * Create a new user.
     */
    @Transactional
    public UserProfileResponse createUser(Integer tenantId, CreateUserRequest request, String createdBy) {
        log.info("Creating new user: {} for tenant: {}", request.getUsername(), tenantId);
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("USERNAME_EXISTS", "Username already exists");
        }
        
        // Check if email already exists
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
                .tenantId(tenantId)
                .status(User.UserStatus.ACTIVE)
                .roles(new HashSet<>())
                .createdBy(createdBy)
                .build();
        
        // Add role
        user.addRole(request.getRole().name());
        
        user = userRepository.save(user);
        
        // Assign security groups for STAFF users
        if (request.getRole() == UserRole.STAFF && 
            request.getSecurityGroupIds() != null && 
            !request.getSecurityGroupIds().isEmpty()) {
            
            AssignSecurityGroupRequest assignRequest = AssignSecurityGroupRequest.builder()
                    .securityGroupIds(request.getSecurityGroupIds())
                    .build();
            
            securityGroupService.assignSecurityGroupsToUser(user.getId(), assignRequest, createdBy);
        }
        
        log.info("User created: {} with ID: {}", request.getUsername(), user.getId());
        return mapToProfileResponse(user);
    }
    
    /**
     * Get user profile by ID.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String userId) {
        log.debug("Getting user profile: {}", userId);
        User user = findUserById(userId);
        return mapToProfileResponse(user);
    }
    
    /**
     * Update user profile.
     */
    @Transactional
    public UserProfileResponse updateUserProfile(String userId, UpdateProfileRequest request) {
        log.info("Updating user profile: {}", userId);
        
        User user = findUserById(userId);
        
        // Check email uniqueness if changed
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("EMAIL_EXISTS", "Email already exists");
            }
            user.setEmail(request.getEmail());
        }
        
        // Update fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        user = userRepository.save(user);
        
        log.info("User profile updated: {}", userId);
        return mapToProfileResponse(user);
    }
    
    /**
     * Change user password.
     */
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", userId);
        
        User user = findUserById(userId);
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(Instant.now());
        
        userRepository.save(user);
        
        log.info("Password changed for user: {}", userId);
    }
    
    /**
     * Request password reset (generates token and sends email).
     */
    @Transactional
    public void requestPasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
        
        // In production, generate reset token and send email
        // For now, just log
        log.info("Password reset token would be sent to: {}", email);
        
        // TODO: Implement email service integration
        // String resetToken = UUID.randomUUID().toString();
        // Save token to database or Redis with expiration
        // Send email with reset link
    }
    
    /**
     * Get all users (admin only).
     */
    @Transactional(readOnly = true)
    public PageResponse<UserProfileResponse> getAllUsers(Pageable pageable) {
        log.debug("Getting all users with pagination");
        
        Page<User> page = userRepository.findAll(pageable);
        List<UserProfileResponse> users = page.getContent().stream()
                .map(this::mapToProfileResponse)
                .toList();
        
        return PageResponse.of(users, page.getNumber(), page.getSize(), page.getTotalElements());
    }
    
    /**
     * Get users by tenant.
     */
    @Transactional(readOnly = true)
    public List<UserProfileResponse> getUsersByTenant(Integer tenantId) {
        log.debug("Getting users for tenant: {}", tenantId);
        
        return userRepository.findByTenantId(tenantId).stream()
                .map(this::mapToProfileResponse)
                .toList();
    }
    
    /**
     * Delete user (admin only).
     */
    @Transactional
    public void deleteUser(String userId) {
        log.warn("Deleting user: {}", userId);
        
        User user = findUserById(userId);
        userRepository.delete(user);
        
        log.info("User deleted: {}", userId);
    }
    
    /**
     * Activate/Deactivate user.
     */
    @Transactional
    public UserProfileResponse updateUserStatus(String userId, User.UserStatus status) {
        log.info("Updating user status: {} to {}", userId, status);
        
        User user = findUserById(userId);
        user.setStatus(status);
        user = userRepository.save(user);
        
        log.info("User status updated: {}", userId);
        return mapToProfileResponse(user);
    }
    
    /**
     * Add role to user.
     */
    @Transactional
    public UserProfileResponse addRoleToUser(String userId, String role) {
        log.info("Adding role {} to user: {}", role, userId);
        
        User user = findUserById(userId);
        user.addRole(role);
        user = userRepository.save(user);
        
        log.info("Role added to user: {}", userId);
        return mapToProfileResponse(user);
    }
    
    /**
     * Remove role from user.
     */
    @Transactional
    public UserProfileResponse removeRoleFromUser(String userId, String role) {
        log.info("Removing role {} from user: {}", role, userId);
        
        User user = findUserById(userId);
        user.getRoles().remove(role);
        user = userRepository.save(user);
        
        log.info("Role removed from user: {}", userId);
        return mapToProfileResponse(user);
    }
    
    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
    
    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .tenantId(user.getTenantId())
                .status(user.getStatus())
                .roles(user.getRoles())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
