package com.easybilling.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for entities with dynamic attributes.
 * Provides JSON column support for tenant-specific custom fields.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class DynamicAttributeEntity {
    
    /**
     * Dynamic attributes stored as JSON.
     * Allows tenants to add custom fields without schema changes.
     */
    @Column(name = "attributes", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> attributes = new HashMap<>();
    
    /**
     * Get a dynamic attribute value.
     * 
     * @param key The attribute key
     * @return The attribute value or null if not found
     */
    public Object getAttribute(String key) {
        return attributes != null ? attributes.get(key) : null;
    }
    
    /**
     * Get a typed dynamic attribute value.
     * 
     * @param key The attribute key
     * @param type The expected type
     * @return The attribute value cast to the specified type
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = getAttribute(key);
        return value != null ? (T) value : null;
    }
    
    /**
     * Set a dynamic attribute value.
     * 
     * @param key The attribute key
     * @param value The attribute value
     */
    public void setAttribute(String key, Object value) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(key, value);
    }
    
    /**
     * Remove a dynamic attribute.
     * 
     * @param key The attribute key
     */
    public void removeAttribute(String key) {
        if (attributes != null) {
            attributes.remove(key);
        }
    }
    
    /**
     * Check if an attribute exists.
     * 
     * @param key The attribute key
     * @return true if the attribute exists
     */
    public boolean hasAttribute(String key) {
        return attributes != null && attributes.containsKey(key);
    }
    
    /**
     * Get all attributes.
     * 
     * @return Map of all attributes
     */
    public Map<String, Object> getAllAttributes() {
        return attributes != null ? new HashMap<>(attributes) : new HashMap<>();
    }
    
    /**
     * Set all attributes at once.
     * 
     * @param attributes The attributes map
     */
    public void setAllAttributes(Map<String, Object> attributes) {
        this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
    }
    
    /**
     * Clear all attributes.
     */
    public void clearAttributes() {
        if (attributes != null) {
            attributes.clear();
        }
    }
}
