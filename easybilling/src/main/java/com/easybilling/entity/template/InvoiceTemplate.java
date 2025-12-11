package com.easybilling.entity.template;

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
 * Invoice Template Entity.
 * Stores customizable invoice templates for different formats (thermal, A4).
 */
@Entity
@Table(name = "invoice_templates", indexes = {
    @Index(name = "idx_template_tenant", columnList = "tenant_id"),
    @Index(name = "idx_template_code", columnList = "template_code"),
    @Index(name = "idx_template_default", columnList = "is_default")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_template_tenant_code", 
                     columnNames = {"tenant_id", "template_code"})
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@EntityListeners(TenantEntityListener.class)
public class InvoiceTemplate implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "template_code", nullable = false, length = 100)
    private String templateCode;  // 'thermal_default', 'a4_standard', 'a4_detailed'
    
    @Column(name = "template_name", nullable = false, length = 200)
    private String templateName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "template_type", nullable = false, length = 50)
    private String templateType;  // 'thermal', 'a4', 'email'
    
    @Column(name = "paper_size", length = 50)
    private String paperSize;  // '80mm', 'A4', 'A5'
    
    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * Template layout configuration as JSON.
     * Defines sections, fields, styling, and positioning.
     * Example: {
     *   "header": {...},
     *   "body": {...},
     *   "footer": {...},
     *   "styles": {...}
     * }
     */
    @Column(name = "layout_config", nullable = false, columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> layoutConfig;
    
    /**
     * Data bindings as JSON.
     * Maps template fields to invoice data fields.
     * Example: {
     *   "invoiceNumber": "invoice.number",
     *   "customerName": "customer.name",
     *   "items": "invoice.items"
     * }
     */
    @Column(name = "data_bindings", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> dataBindings;
    
    /**
     * Template HTML/template content.
     * Can be Thymeleaf template or custom HTML.
     */
    @Column(name = "template_content", columnDefinition = "TEXT")
    private String templateContent;
    
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
