package com.easybilling.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing values for custom fields.
 * Stores actual data for custom fields attached to entities.
 */
@Entity
@Table(name = "custom_field_values", indexes = {
        @Index(name = "idx_custom_field_value_entity", columnList = "tenant_id, entity_type, entity_id"),
        @Index(name = "idx_custom_field_value_field", columnList = "custom_field_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CustomFieldValue implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "custom_field_id", nullable = false)
    private Long customFieldId;
    
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // INVOICE, CUSTOMER, PRODUCT, SUPPLIER, etc.
    
    @Column(name = "entity_id", nullable = false, length = 100)
    private String entityId; // ID of the invoice, customer, product, etc. (supports UUID and Long)
    
    @Column(name = "field_value", columnDefinition = "TEXT")
    private String fieldValue;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
