package com.runtime.engine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.runtime.engine.util.JsonAttributesConverter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@Entity
@Table(name = "dynamic_entities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String entityType;
    
    @Column(nullable = false)
    private String tenantId;
    
    @Convert(converter = JsonAttributesConverter.class)
    @Column(columnDefinition = "JSON", nullable = false)
    private Map<String, Object> attributes = new HashMap<>();
    
    @Convert(converter = JsonAttributesConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> metadata = new HashMap<>();
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Version
    private Long version;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    public void setAttribute(String key, Object value) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(key, value);
    }
    
    public void removeAttribute(String key) {
        if (attributes != null) {
            attributes.remove(key);
        }
    }
    
    public Object getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }
    
    public void setMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }
}
