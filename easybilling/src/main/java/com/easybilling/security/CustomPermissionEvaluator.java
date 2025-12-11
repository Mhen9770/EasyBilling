package com.easybilling.security;

import com.easybilling.entity.User;
import com.easybilling.enums.Permission;
import com.easybilling.enums.UserRole;
import com.easybilling.repository.UserRepository;
import com.easybilling.service.SecurityGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Custom permission evaluator for runtime permission checks.
 * Supports both role-based (ADMIN) and permission-based (STAFF) access control.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {
    
    private final SecurityGroupService securityGroupService;
    private final UserRepository userRepository;
    
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }
        
        return evaluatePermission(authentication, permission);
    }
    
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }
        
        return evaluatePermission(authentication, permission);
    }
    
    /**
     * Evaluate if the authenticated user has the required permission.
     */
    private boolean evaluatePermission(Authentication authentication, Object permission) {
        String username = authentication.getName();
        
        try {
            // Get user from database
            User user = userRepository.findByUsername(username)
                    .orElse(null);
            
            if (user == null) {
                log.warn("User not found: {}", username);
                return false;
            }
            
            // Check if user is ADMIN - admins have all permissions
            if (user.getRoles().contains(UserRole.ADMIN.name())) {
                log.debug("User {} is ADMIN, granting permission: {}", username, permission);
                return true;
            }
            
            // Convert permission to enum
            Permission requiredPermission;
            if (permission instanceof Permission) {
                requiredPermission = (Permission) permission;
            } else if (permission instanceof String) {
                try {
                    requiredPermission = Permission.valueOf((String) permission);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid permission: {}", permission);
                    return false;
                }
            } else {
                log.warn("Unsupported permission type: {}", permission.getClass());
                return false;
            }
            
            // Check if STAFF user has the required permission through security groups
            boolean hasPermission = securityGroupService.hasPermission(user.getId(), requiredPermission);
            
            log.debug("User {} permission check for {}: {}", username, requiredPermission, hasPermission);
            return hasPermission;
            
        } catch (Exception e) {
            log.error("Error evaluating permission for user: {}", username, e);
            return false;
        }
    }
}
