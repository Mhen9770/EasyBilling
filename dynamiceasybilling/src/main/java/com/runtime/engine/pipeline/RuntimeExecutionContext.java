package com.runtime.engine.pipeline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuntimeExecutionContext {
    
    private String tenantId;
    private String userId;
    private String sessionId;
    private String requestId;
    
    @Builder.Default
    private Map<String, Object> variables = new HashMap<>();
    
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    @Builder.Default
    private Map<String, Object> permissions = new HashMap<>();
    
    private String currentEntity;
    private String currentAction;
    private Long currentEntityId;
    
    @Builder.Default
    private Map<String, Object> validationErrors = new HashMap<>();
    
    @Builder.Default
    private boolean validationFailed = false;
    
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }
    
    public Object getVariable(String key) {
        return variables.get(key);
    }
    
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    public void addValidationError(String field, Object error) {
        validationErrors.put(field, error);
        validationFailed = true;
    }
    
    public void clearValidationErrors() {
        validationErrors.clear();
        validationFailed = false;
    }
    
    public boolean hasPermission(String permission) {
        return permissions.containsKey(permission) && 
               Boolean.TRUE.equals(permissions.get(permission));
    }
    
    public void grantPermission(String permission) {
        permissions.put(permission, true);
    }
    
    public void revokePermission(String permission) {
        permissions.put(permission, false);
    }
}
