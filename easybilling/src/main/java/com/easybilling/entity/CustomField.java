package com.easybilling.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing custom fields that can be added to entities dynamically.
 * Allows tenants to add custom data fields to invoices, customers, products, etc.
 */
@Entity
@Table(name = "custom_fields", indexes = {
        @Index(name = "idx_custom_field_entity", columnList = "tenant_id, entity_type"),
        @Index(name = "idx_custom_field_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CustomField implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // e.g., "INVOICE", "CUSTOMER", "PRODUCT", "SUPPLIER"
    
    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;
    
    @Column(name = "field_label", nullable = false, length = 200)
    private String fieldLabel;
    
    @Column(name = "field_type", nullable = false, length = 20)
    private String fieldType; // TEXT, NUMBER, DATE, BOOLEAN, DROPDOWN, MULTI_SELECT, EMAIL, PHONE, URL
    
    @Column(name = "field_description", length = 500)
    private String fieldDescription;
    
    @Column(name = "default_value", length = 500)
    private String defaultValue;
    
    @Column(name = "dropdown_options", columnDefinition = "TEXT")
    private String dropdownOptions; // JSON array for dropdown/multi-select options
    
    @Column(name = "validation_rules", columnDefinition = "TEXT")
    private String validationRules; // JSON object: {required: true, min: 0, max: 100, pattern: "regex"}
    
    @Column(name = "is_required")
    private Boolean isRequired = false;
    
    @Column(name = "is_searchable")
    private Boolean isSearchable = false;
    
    @Column(name = "is_displayed_in_list")
    private Boolean isDisplayedInList = false;
    
    @Column(name = "display_order")
    private Integer displayOrder = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "placeholder_text", length = 200)
    private String placeholderText;
    
    @Column(name = "help_text", length = 500)
    private String helpText;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
}
