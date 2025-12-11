package com.easybilling.service;

import com.easybilling.dto.AssignSecurityGroupRequest;
import com.easybilling.dto.SecurityGroupRequest;
import com.easybilling.dto.SecurityGroupResponse;
import com.easybilling.entity.SecurityGroup;
import com.easybilling.entity.User;
import com.easybilling.entity.UserSecurityGroup;
import com.easybilling.enums.Permission;
import com.easybilling.exception.BusinessException;
import com.easybilling.exception.ResourceNotFoundException;
import com.easybilling.repository.SecurityGroupRepository;
import com.easybilling.repository.UserRepository;
import com.easybilling.repository.UserSecurityGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing security groups and permissions.
 * Implements caching for performance optimization.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityGroupService {
    
    private final SecurityGroupRepository securityGroupRepository;
    private final UserSecurityGroupRepository userSecurityGroupRepository;
    private final UserRepository userRepository;
    
    /**
     * Create a new security group.
     */
    @Transactional
    @CacheEvict(value = "securityGroups", key = "#tenantId")
    public SecurityGroupResponse createSecurityGroup(String tenantId, SecurityGroupRequest request, String createdBy) {
        log.info("Creating security group: {} for tenant: {}", request.getName(), tenantId);
        
        // Check if security group with same name exists for this tenant
        if (securityGroupRepository.existsByNameAndTenantId(request.getName(), tenantId)) {
            throw new BusinessException("SECURITY_GROUP_EXISTS", 
                "Security group with name '" + request.getName() + "' already exists");
        }
        
        // Create security group
        SecurityGroup securityGroup = SecurityGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .tenantId(tenantId)
                .permissions(request.getPermissions() != null ? request.getPermissions() : new HashSet<>())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdBy(createdBy)
                .build();
        
        securityGroup = securityGroupRepository.save(securityGroup);
        
        log.info("Security group created: {} with ID: {}", request.getName(), securityGroup.getId());
        return mapToResponse(securityGroup, 0);
    }
    
    /**
     * Update an existing security group.
     */
    @Transactional
    @CacheEvict(value = {"securityGroups", "userPermissions"}, allEntries = true)
    public SecurityGroupResponse updateSecurityGroup(String securityGroupId, SecurityGroupRequest request, String updatedBy) {
        log.info("Updating security group: {}", securityGroupId);
        
        SecurityGroup securityGroup = findSecurityGroupById(securityGroupId);
        
        // Check if name is being changed and if it conflicts
        if (!securityGroup.getName().equals(request.getName())) {
            if (securityGroupRepository.existsByNameAndTenantId(request.getName(), securityGroup.getTenantId())) {
                throw new BusinessException("SECURITY_GROUP_EXISTS", 
                    "Security group with name '" + request.getName() + "' already exists");
            }
        }
        
        // Update fields
        securityGroup.setName(request.getName());
        securityGroup.setDescription(request.getDescription());
        if (request.getPermissions() != null) {
            securityGroup.setPermissions(request.getPermissions());
        }
        if (request.getIsActive() != null) {
            securityGroup.setIsActive(request.getIsActive());
        }
        securityGroup.setUpdatedBy(updatedBy);
        
        securityGroup = securityGroupRepository.save(securityGroup);
        
        log.info("Security group updated: {}", securityGroupId);
        int userCount = userSecurityGroupRepository.findBySecurityGroupId(securityGroupId).size();
        return mapToResponse(securityGroup, userCount);
    }
    
    /**
     * Get security group by ID.
     */
    @Transactional(readOnly = true)
    public SecurityGroupResponse getSecurityGroup(String securityGroupId) {
        log.debug("Getting security group: {}", securityGroupId);
        
        SecurityGroup securityGroup = findSecurityGroupById(securityGroupId);
        int userCount = userSecurityGroupRepository.findBySecurityGroupId(securityGroupId).size();
        
        return mapToResponse(securityGroup, userCount);
    }
    
    /**
     * Get all security groups for a tenant.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "securityGroups", key = "#tenantId")
    public List<SecurityGroupResponse> getSecurityGroupsByTenant(String tenantId) {
        log.debug("Getting security groups for tenant: {}", tenantId);
        
        List<SecurityGroup> securityGroups = securityGroupRepository.findByTenantId(tenantId);
        
        return securityGroups.stream()
                .map(sg -> {
                    int userCount = userSecurityGroupRepository.findBySecurityGroupId(sg.getId()).size();
                    return mapToResponse(sg, userCount);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get active security groups for a tenant.
     */
    @Transactional(readOnly = true)
    public List<SecurityGroupResponse> getActiveSecurityGroups(String tenantId) {
        log.debug("Getting active security groups for tenant: {}", tenantId);
        
        List<SecurityGroup> securityGroups = securityGroupRepository.findByTenantIdAndIsActiveTrue(tenantId);
        
        return securityGroups.stream()
                .map(sg -> {
                    int userCount = userSecurityGroupRepository.findBySecurityGroupId(sg.getId()).size();
                    return mapToResponse(sg, userCount);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Delete a security group.
     */
    @Transactional
    @CacheEvict(value = {"securityGroups", "userPermissions"}, allEntries = true)
    public void deleteSecurityGroup(String securityGroupId) {
        log.warn("Deleting security group: {}", securityGroupId);
        
        SecurityGroup securityGroup = findSecurityGroupById(securityGroupId);
        
        // Check if any users are assigned to this group
        List<UserSecurityGroup> assignments = userSecurityGroupRepository.findBySecurityGroupId(securityGroupId);
        if (!assignments.isEmpty()) {
            throw new BusinessException("SECURITY_GROUP_IN_USE", 
                "Cannot delete security group that has users assigned. Please remove all users first.");
        }
        
        securityGroupRepository.delete(securityGroup);
        
        log.info("Security group deleted: {}", securityGroupId);
    }
    
    /**
     * Assign security groups to a user.
     */
    @Transactional
    @CacheEvict(value = "userPermissions", key = "#userId")
    public void assignSecurityGroupsToUser(String userId, AssignSecurityGroupRequest request, String assignedBy) {
        log.info("Assigning security groups to user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        // Remove existing assignments
        userSecurityGroupRepository.deleteByUserId(userId);
        
        // Add new assignments
        for (String securityGroupId : request.getSecurityGroupIds()) {
            SecurityGroup securityGroup = findSecurityGroupById(securityGroupId);
            
            // Verify security group belongs to same tenant as user
            if (!securityGroup.getTenantId().equals(user.getTenantId())) {
                throw new BusinessException("TENANT_MISMATCH", 
                    "Security group does not belong to the same tenant as the user");
            }
            
            UserSecurityGroup assignment = UserSecurityGroup.builder()
                    .userId(userId)
                    .securityGroupId(securityGroupId)
                    .assignedBy(assignedBy)
                    .build();
            
            userSecurityGroupRepository.save(assignment);
        }
        
        log.info("Security groups assigned to user: {}", userId);
    }
    
    /**
     * Get security groups for a user.
     */
    @Transactional(readOnly = true)
    public List<SecurityGroupResponse> getSecurityGroupsForUser(String userId) {
        log.debug("Getting security groups for user: {}", userId);
        
        List<SecurityGroup> securityGroups = securityGroupRepository.findByUserId(userId);
        
        return securityGroups.stream()
                .map(sg -> mapToResponse(sg, 0))
                .collect(Collectors.toList());
    }
    
    /**
     * Get all permissions for a user (aggregated from all assigned security groups).
     * This method is cached for performance.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userPermissions", key = "#userId")
    public Set<Permission> getUserPermissions(String userId) {
        log.debug("Getting permissions for user: {}", userId);
        
        List<SecurityGroup> securityGroups = securityGroupRepository.findByUserId(userId);
        
        Set<Permission> permissions = new HashSet<>();
        for (SecurityGroup securityGroup : securityGroups) {
            if (securityGroup.getIsActive() && securityGroup.getPermissions() != null) {
                permissions.addAll(securityGroup.getPermissions());
            }
        }
        
        log.debug("User {} has {} permissions", userId, permissions.size());
        return permissions;
    }
    
    /**
     * Check if user has a specific permission.
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(String userId, Permission permission) {
        Set<Permission> permissions = getUserPermissions(userId);
        return permissions.contains(permission);
    }
    
    /**
     * Check if user has any of the specified permissions.
     */
    @Transactional(readOnly = true)
    public boolean hasAnyPermission(String userId, Permission... permissions) {
        Set<Permission> userPermissions = getUserPermissions(userId);
        for (Permission permission : permissions) {
            if (userPermissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if user has all of the specified permissions.
     */
    @Transactional(readOnly = true)
    public boolean hasAllPermissions(String userId, Permission... permissions) {
        Set<Permission> userPermissions = getUserPermissions(userId);
        for (Permission permission : permissions) {
            if (!userPermissions.contains(permission)) {
                return false;
            }
        }
        return true;
    }
    
    private SecurityGroup findSecurityGroupById(String securityGroupId) {
        return securityGroupRepository.findById(securityGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("SecurityGroup", securityGroupId));
    }
    
    private SecurityGroupResponse mapToResponse(SecurityGroup securityGroup, int userCount) {
        return SecurityGroupResponse.builder()
                .id(securityGroup.getId())
                .name(securityGroup.getName())
                .description(securityGroup.getDescription())
                .tenantId(securityGroup.getTenantId())
                .permissions(securityGroup.getPermissions())
                .isActive(securityGroup.getIsActive())
                .createdAt(securityGroup.getCreatedAt())
                .updatedAt(securityGroup.getUpdatedAt())
                .createdBy(securityGroup.getCreatedBy())
                .updatedBy(securityGroup.getUpdatedBy())
                .userCount(userCount)
                .build();
    }
}
