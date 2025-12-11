package com.easybilling.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Context for rule evaluation.
 * Contains all data needed to evaluate rule conditions and execute actions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleContext {
    
    /**
     * Context data map.
     * Contains all values that rules can access.
     */
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();
    
    /**
     * Metadata about the context.
     * Can include tenant info, user info, etc.
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    /**
     * Get a value from the context.
     * Supports nested paths using dot notation (e.g., "customer.name").
     */
    public Object getValue(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        
        String[] parts = path.split("\\.");
        Object current = data;
        
        for (String part : parts) {
            if (current == null) {
                return null;
            }
            
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                // Try to get field value using reflection
                try {
                    current = current.getClass()
                        .getDeclaredField(part)
                        .get(current);
                } catch (Exception e) {
                    return null;
                }
            }
        }
        
        return current;
    }
    
    /**
     * Set a value in the context.
     */
    public void setValue(String key, Object value) {
        data.put(key, value);
    }
    
    /**
     * Set metadata value.
     */
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    /**
     * Get metadata value.
     */
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
}
