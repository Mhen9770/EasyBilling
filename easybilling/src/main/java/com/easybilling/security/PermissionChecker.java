package com.easybilling.security;

import com.easybilling.entity.User;
import com.easybilling.enums.Permission;
import com.easybilling.enums.UserRole;
import com.easybilling.repository.UserRepository;
import com.easybilling.service.SecurityGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Helper component for permission checking in annotations.
 */
@Slf4j
@Component("permissionChecker")
@RequiredArgsConstructor
public class PermissionChecker {
    
    private final SecurityGroupService securityGroupService;
    private final UserRepository userRepository;
    
    /**
     * Check if user has any of the specified permissions.
     */
    public boolean hasAnyPermission(Authentication authentication, Permission... permissions) {
        if (authentication == null || permissions == null || permissions.length == 0) {
            return false;
        }
        
        String username = authentication.getName();
        
        try {
            User user = userRepository.findByUsername(username)
                    .orElse(null);
            
            if (user == null) {
                return false;
            }
            
            // Admins have all permissions
            if (user.getRoles().contains(UserRole.ADMIN.name())) {
                return true;
            }
            
            // Check if STAFF user has any of the required permissions
            return securityGroupService.hasAnyPermission(user.getId(), permissions);
            
        } catch (Exception e) {
            log.error("Error checking permissions for user: {}", username, e);
            return false;
        }
    }
    
    /**
     * Check if user has all of the specified permissions.
     */
    public boolean hasAllPermissions(Authentication authentication, Permission... permissions) {
        if (authentication == null || permissions == null || permissions.length == 0) {
            return false;
        }
        
        String username = authentication.getName();
        
        try {
            User user = userRepository.findByUsername(username)
                    .orElse(null);
            
            if (user == null) {
                return false;
            }
            
            // Admins have all permissions
            if (user.getRoles().contains(UserRole.ADMIN.name())) {
                return true;
            }
            
            // Check if STAFF user has all of the required permissions
            return securityGroupService.hasAllPermissions(user.getId(), permissions);
            
        } catch (Exception e) {
            log.error("Error checking permissions for user: {}", username, e);
            return false;
        }
    }
}
