package com.easybilling.controller;

import com.easybilling.dto.ApiResponse;
import com.easybilling.dto.AssignSecurityGroupRequest;
import com.easybilling.dto.SecurityGroupRequest;
import com.easybilling.dto.SecurityGroupResponse;
import com.easybilling.enums.Permission;
import com.easybilling.service.SecurityGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * REST controller for security group management.
 * All endpoints require ADMIN role or specific permissions.
 */
@RestController
@RequestMapping("/api/v1/security-groups")
@RequiredArgsConstructor
@Tag(name = "Security Management", description = "APIs for managing security groups and permissions")
@SecurityRequirement(name = "bearer-jwt")
public class SecurityGroupController extends BaseController {
    
    private final SecurityGroupService securityGroupService;
    
    @PostMapping
    @Operation(summary = "Create security group", description = "Create a new security group (Admin only)")
    @PreAuthorize("hasPermission(null, 'SECURITY_GROUP_CREATE')")
    public ApiResponse<SecurityGroupResponse> createSecurityGroup(@Valid @RequestBody SecurityGroupRequest request) {
        String tenantId = getCurrentTenantId();
        String createdBy = getCurrentUsername();
        
        SecurityGroupResponse response = securityGroupService.createSecurityGroup(tenantId, request, createdBy);
        return ApiResponse.success("Security group created successfully", response);
    }
    
    @PutMapping("/{securityGroupId}")
    @Operation(summary = "Update security group", description = "Update an existing security group (Admin only)")
    @PreAuthorize("hasPermission(null, 'SECURITY_GROUP_UPDATE')")
    public ApiResponse<SecurityGroupResponse> updateSecurityGroup(
            @PathVariable String securityGroupId,
            @Valid @RequestBody SecurityGroupRequest request) {
        
        String updatedBy = getCurrentUsername();
        SecurityGroupResponse response = securityGroupService.updateSecurityGroup(securityGroupId, request, updatedBy);
        return ApiResponse.success("Security group updated successfully", response);
    }
    
    @GetMapping("/{securityGroupId}")
    @Operation(summary = "Get security group", description = "Get security group by ID (Admin only)")
    @PreAuthorize("hasPermission(null, 'SECURITY_GROUP_READ')")
    public ApiResponse<SecurityGroupResponse> getSecurityGroup(@PathVariable String securityGroupId) {
        SecurityGroupResponse response = securityGroupService.getSecurityGroup(securityGroupId);
        return ApiResponse.success(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all security groups", description = "Get all security groups for current tenant (Admin only)")
    @PreAuthorize("hasPermission(null, 'SECURITY_GROUP_LIST')")
    public ApiResponse<List<SecurityGroupResponse>> getSecurityGroups() {
        String tenantId = getCurrentTenantId();
        List<SecurityGroupResponse> response = securityGroupService.getSecurityGroupsByTenant(tenantId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get active security groups", description = "Get active security groups for current tenant (Admin only)")
    @PreAuthorize("hasPermission(null, 'SECURITY_GROUP_LIST')")
    public ApiResponse<List<SecurityGroupResponse>> getActiveSecurityGroups() {
        String tenantId = getCurrentTenantId();
        List<SecurityGroupResponse> response = securityGroupService.getActiveSecurityGroups(tenantId);
        return ApiResponse.success(response);
    }
    
    @DeleteMapping("/{securityGroupId}")
    @Operation(summary = "Delete security group", description = "Delete a security group (Admin only)")
    @PreAuthorize("hasPermission(null, 'SECURITY_GROUP_DELETE')")
    public ApiResponse<Void> deleteSecurityGroup(@PathVariable String securityGroupId) {
        securityGroupService.deleteSecurityGroup(securityGroupId);
        return ApiResponse.success("Security group deleted successfully", null);
    }
    
    @PostMapping("/users/{userId}/assign")
    @Operation(summary = "Assign security groups to user", description = "Assign security groups to a user (Admin only)")
    @PreAuthorize("hasPermission(null, 'USER_UPDATE')")
    public ApiResponse<Void> assignSecurityGroups(
            @PathVariable String userId,
            @Valid @RequestBody AssignSecurityGroupRequest request) {
        
        String assignedBy = getCurrentUsername();
        securityGroupService.assignSecurityGroupsToUser(userId, request, assignedBy);
        return ApiResponse.success("Security groups assigned successfully", null);
    }
    
    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user security groups", description = "Get security groups assigned to a user (Admin only)")
    @PreAuthorize("hasPermission(null, 'USER_READ')")
    public ApiResponse<List<SecurityGroupResponse>> getUserSecurityGroups(@PathVariable String userId) {
        List<SecurityGroupResponse> response = securityGroupService.getSecurityGroupsForUser(userId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/permissions")
    @Operation(summary = "Get all available permissions", description = "Get list of all available permissions")
    @PreAuthorize("hasPermission(null, 'SECURITY_GROUP_READ')")
    public ApiResponse<List<Permission>> getAllPermissions() {
        List<Permission> permissions = Arrays.asList(Permission.values());
        return ApiResponse.success(permissions);
    }
}
