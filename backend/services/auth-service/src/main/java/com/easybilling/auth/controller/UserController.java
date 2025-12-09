package com.easybilling.auth.controller;

import com.easybilling.auth.dto.ChangePasswordRequest;
import com.easybilling.auth.dto.PasswordResetRequest;
import com.easybilling.auth.dto.UpdateProfileRequest;
import com.easybilling.auth.dto.UserProfileResponse;
import com.easybilling.auth.entity.User;
import com.easybilling.auth.service.UserService;
import com.easybilling.common.dto.ApiResponse;
import com.easybilling.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user management.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing user profiles and settings")
@SecurityRequirement(name = "bearer-jwt")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Get profile of the authenticated user")
    public ApiResponse<UserProfileResponse> getCurrentUserProfile(
            @RequestHeader("X-User-Id") String userId) {
        UserProfileResponse response = userService.getUserProfile(userId);
        return ApiResponse.success(response);
    }
    
    @PutMapping("/me")
    @Operation(summary = "Update current user profile", description = "Update profile of the authenticated user")
    public ApiResponse<UserProfileResponse> updateCurrentUserProfile(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse response = userService.updateUserProfile(userId, request);
        return ApiResponse.success("Profile updated successfully", response);
    }
    
    @PostMapping("/me/change-password")
    @Operation(summary = "Change password", description = "Change password for the authenticated user")
    public ApiResponse<Void> changePassword(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request);
        return ApiResponse.success("Password changed successfully", null);
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Request password reset", description = "Request password reset link via email")
    public ApiResponse<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        userService.requestPasswordReset(request.getEmail());
        return ApiResponse.success("Password reset link sent to email", null);
    }
    
    // Admin endpoints
    
    @GetMapping
    @Operation(summary = "Get all users", description = "Get all users with pagination (Admin only)")
    public ApiResponse<PageResponse<UserProfileResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        PageResponse<UserProfileResponse> response = userService.getAllUsers(pageable);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Get user profile by ID (Admin only)")
    public ApiResponse<UserProfileResponse> getUserById(@PathVariable String userId) {
        UserProfileResponse response = userService.getUserProfile(userId);
        return ApiResponse.success(response);
    }
    
    @PutMapping("/{userId}")
    @Operation(summary = "Update user", description = "Update user profile (Admin only)")
    public ApiResponse<UserProfileResponse> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse response = userService.updateUserProfile(userId, request);
        return ApiResponse.success("User updated successfully", response);
    }
    
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Delete user (Admin only)")
    public ApiResponse<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ApiResponse.success("User deleted successfully", null);
    }
    
    @PutMapping("/{userId}/status")
    @Operation(summary = "Update user status", description = "Activate/Deactivate user (Admin only)")
    public ApiResponse<UserProfileResponse> updateUserStatus(
            @PathVariable String userId,
            @RequestParam User.UserStatus status) {
        UserProfileResponse response = userService.updateUserStatus(userId, status);
        return ApiResponse.success("User status updated", response);
    }
    
    @PostMapping("/{userId}/roles/{role}")
    @Operation(summary = "Add role to user", description = "Add role to user (Admin only)")
    public ApiResponse<UserProfileResponse> addRole(
            @PathVariable String userId,
            @PathVariable String role) {
        UserProfileResponse response = userService.addRoleToUser(userId, role);
        return ApiResponse.success("Role added to user", response);
    }
    
    @DeleteMapping("/{userId}/roles/{role}")
    @Operation(summary = "Remove role from user", description = "Remove role from user (Admin only)")
    public ApiResponse<UserProfileResponse> removeRole(
            @PathVariable String userId,
            @PathVariable String role) {
        UserProfileResponse response = userService.removeRoleFromUser(userId, role);
        return ApiResponse.success("Role removed from user", response);
    }
    
    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get users by tenant", description = "Get all users for a tenant (Admin only)")
    public ApiResponse<List<UserProfileResponse>> getUsersByTenant(@PathVariable String tenantId) {
        List<UserProfileResponse> response = userService.getUsersByTenant(tenantId);
        return ApiResponse.success(response);
    }
}
