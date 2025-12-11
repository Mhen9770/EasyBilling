package com.easybilling.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing system-wide configuration settings.
 * These settings apply to all tenants unless overridden at tenant level.
 */
@Entity
@Table(name = "system_configurations", indexes = {
        @Index(name = "idx_config_key", columnList = "config_key", unique = true),
        @Index(name = "idx_config_category", columnList = "category")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class SystemConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;
    
    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;
    
    @Column(name = "category", length = 50)
    private String category; // e.g., "billing", "tax", "notification", "ui", "security"
    
    @Column(name = "data_type", length = 20)
    private String dataType; // e.g., "STRING", "INTEGER", "BOOLEAN", "JSON", "DECIMAL"
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "is_editable")
    private Boolean isEditable = true; // Can this be modified by admins?
    
    @Column(name = "is_sensitive")
    private Boolean isSensitive = false; // Should this be encrypted/masked?
    
    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;
    
    @Column(name = "validation_rule", length = 500)
    private String validationRule; // Regex or custom validation
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
