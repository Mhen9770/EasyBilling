package com.easybilling.entity.metadata;

import com.easybilling.entity.TenantAware;
import com.easybilling.listener.TenantEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

/**
 * Form Metadata Entity.
 * Defines form structure and layout for dynamic rendering.
 */
@Entity
@Table(name = "form_metadata", indexes = {
    @Index(name = "idx_form_tenant", columnList = "tenant_id"),
    @Index(name = "idx_form_entity", columnList = "entity_type"),
    @Index(name = "idx_form_active", columnList = "is_active")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_form_tenant_entity", 
                     columnNames = {"tenant_id", "entity_type", "form_name"})
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@EntityListeners(TenantEntityListener.class)
public class FormMetadata implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;  // 'product', 'customer', 'invoice', etc.
    
    @Column(name = "form_name", nullable = false, length = 100)
    private String formName;    // 'create_product', 'edit_customer'
    
    @Column(name = "display_label", nullable = false, length = 200)
    private String displayLabel;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 50)
    private String category;    // 'sales', 'inventory', 'customer'
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;
    
    /**
     * Layout configuration as JSON.
     * Example: {"type": "grid", "columns": 2, "gap": "1rem", "sections": [...]}
     */
    @Column(name = "layout_config", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> layoutConfig;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
