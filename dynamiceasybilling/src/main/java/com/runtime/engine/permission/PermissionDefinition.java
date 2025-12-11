package com.runtime.engine.permission;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.runtime.engine.util.JsonAttributesConverter;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "permission_definitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDefinition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String entityType;
    
    @Column(nullable = false)
    private String action;
    
    @Column(nullable = false)
    private String tenantId;
    
    @Column(nullable = false)
    private String resourceId;
    
    @Column(nullable = false)
    private String principalType;
    
    @Column(nullable = false)
    private String principalId;
    
    @Column(nullable = false)
    private Boolean allowed = true;
    
    @Column(columnDefinition = "TEXT")
    private String condition;
    
    @Convert(converter = JsonAttributesConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> attributes;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Convert(converter = JsonAttributesConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> metadata;
    
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
}
