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
 * Field Metadata Entity.
 * Defines individual field properties for dynamic forms.
 */
@Entity
@Table(name = "field_metadata", indexes = {
    @Index(name = "idx_field_tenant", columnList = "tenant_id"),
    @Index(name = "idx_field_form", columnList = "form_id"),
    @Index(name = "idx_field_entity", columnList = "entity_type"),
    @Index(name = "idx_field_name", columnList = "field_name")
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@EntityListeners(TenantEntityListener.class)
public class FieldMetadata implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "form_id", length = 36)
    private String formId;  // NULL for standalone fields
    
    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;
    
    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;  // Database column or JSON path
    
    @Column(name = "field_label", nullable = false, length = 200)
    private String fieldLabel;
    
    @Column(name = "field_type", nullable = false, length = 50)
    private String fieldType;  // 'text', 'number', 'select', 'date', etc.
    
    @Column(name = "data_type", nullable = false, length = 50)
    private String dataType;   // 'string', 'integer', 'decimal', 'boolean'
    
    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;
    
    @Column(length = 200)
    private String placeholder;
    
    @Column(name = "help_text", columnDefinition = "TEXT")
    private String helpText;
    
    /**
     * Validation rules as JSON.
     * Example: {"required": true, "min": 0, "max": 100, "pattern": "^[0-9]+$"}
     */
    @Column(name = "validation_rules", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> validationRules;
    
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;
    
    @Column(name = "is_required")
    @Builder.Default
    private Boolean isRequired = false;
    
    @Column(name = "is_readonly")
    @Builder.Default
    private Boolean isReadonly = false;
    
    @Column(name = "is_visible")
    @Builder.Default
    private Boolean isVisible = true;
    
    @Column(name = "is_searchable")
    @Builder.Default
    private Boolean isSearchable = false;
    
    @Column(name = "is_sortable")
    @Builder.Default
    private Boolean isSortable = false;
    
    /**
     * Options for select/radio/checkbox fields as JSON.
     * Example: [{"value": "option1", "label": "Option 1"}, ...]
     */
    @Column(columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> options;
    
    /**
     * Conditional logic for showing/hiding field as JSON.
     * Example: {"showWhen": {"field": "has_warranty", "operator": "equals", "value": true}}
     */
    @Column(name = "conditional_logic", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> conditionalLogic;
    
    /**
     * Additional UI attributes as JSON.
     * Example: {"min": "0", "max": "100", "step": "5", "suffix": "kg"}
     */
    @Column(name = "ui_attributes", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> uiAttributes;
    
    @Column(name = "grid_column", length = 50)
    private String gridColumn;  // CSS grid column position
    
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
