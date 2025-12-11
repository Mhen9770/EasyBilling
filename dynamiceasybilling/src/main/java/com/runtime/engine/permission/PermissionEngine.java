package com.runtime.engine.permission;

import com.runtime.engine.pipeline.RuntimeExecutionContext;
import com.runtime.engine.rule.ConditionEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionEngine {
    
    private final ConditionEvaluator conditionEvaluator;
    
    public boolean hasPermission(String entityType, String action, RuntimeExecutionContext context) {
        String permission = buildPermissionKey(entityType, action);
        return context.hasPermission(permission);
    }
    
    public boolean checkPermission(String entityType, String action, Map<String, Object> data, RuntimeExecutionContext context) {
        log.debug("Checking permission for entity: {}, action: {}", entityType, action);
        
        String permission = buildPermissionKey(entityType, action);
        
        if (context.hasPermission(permission)) {
            return true;
        }
        
        return false;
    }
    
    public void enforcePermission(String entityType, String action, RuntimeExecutionContext context) {
        if (!hasPermission(entityType, action, context)) {
            throw new PermissionDeniedException(
                "Permission denied for entity: " + entityType + ", action: " + action
            );
        }
    }
    
    public void grantPermission(String entityType, String action, RuntimeExecutionContext context) {
        String permission = buildPermissionKey(entityType, action);
        context.grantPermission(permission);
        log.debug("Granted permission: {}", permission);
    }
    
    public void revokePermission(String entityType, String action, RuntimeExecutionContext context) {
        String permission = buildPermissionKey(entityType, action);
        context.revokePermission(permission);
        log.debug("Revoked permission: {}", permission);
    }
    
    public void evaluatePermissions(List<PermissionDefinition> permissions, Map<String, Object> data, RuntimeExecutionContext context) {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }
        
        for (PermissionDefinition permission : permissions) {
            evaluatePermission(permission, data, context);
        }
    }
    
    public void evaluatePermission(PermissionDefinition permission, Map<String, Object> data, RuntimeExecutionContext context) {
        if (!permission.getActive()) {
            return;
        }
        
        boolean conditionMet = true;
        if (permission.getCondition() != null && !permission.getCondition().isEmpty()) {
            conditionMet = conditionEvaluator.evaluate(permission.getCondition(), data, context);
        }
        
        if (conditionMet) {
            String permissionKey = buildPermissionKey(permission.getEntityType(), permission.getAction());
            
            if (permission.getAllowed()) {
                context.grantPermission(permissionKey);
            } else {
                context.revokePermission(permissionKey);
            }
        }
    }
    
    public void loadSecurityGroupPermissions(SecurityGroup group, RuntimeExecutionContext context) {
        if (group == null || !group.getActive()) {
            return;
        }
        
        if (!group.getMembers().contains(context.getUserId())) {
            return;
        }
        
        for (String permission : group.getPermissions()) {
            context.grantPermission(permission);
        }
        
        log.debug("Loaded {} permissions from security group: {}", group.getPermissions().size(), group.getName());
    }
    
    private String buildPermissionKey(String entityType, String action) {
        return entityType + ":" + action;
    }
    
    public static class PermissionDeniedException extends RuntimeException {
        public PermissionDeniedException(String message) {
            super(message);
        }
    }
}
