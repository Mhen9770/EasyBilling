package com.easybilling.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing tenant-specific configuration settings.
 * These settings override system defaults for specific tenants.
 */
@Entity
@Table(name = "tenant_configurations", indexes = {
        @Index(name = "idx_tenant_config_key", columnList = "tenant_id, config_key", unique = true),
        @Index(name = "idx_tenant_config_category", columnList = "tenant_id, category")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TenantConfiguration implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;
    
    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;
    
    @Column(name = "category", length = 50)
    private String category; // e.g., "billing", "tax", "notification", "ui", "security"
    
    @Column(name = "data_type", length = 20)
    private String dataType; // e.g., "STRING", "INTEGER", "BOOLEAN", "JSON", "DECIMAL"
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "is_override")
    private Boolean isOverride = true; // Is this overriding system default?
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
