package com.easybilling.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing customizable document templates.
 * Allows tenants to create custom invoice, receipt, report templates.
 */
@Entity
@Table(name = "document_templates", indexes = {
        @Index(name = "idx_template_tenant_type", columnList = "tenant_id, template_type"),
        @Index(name = "idx_template_default", columnList = "tenant_id, is_default")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class DocumentTemplate implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;
    
    @Column(name = "template_type", nullable = false, length = 50)
    private String templateType; // INVOICE, RECEIPT, QUOTATION, DELIVERY_NOTE, CREDIT_NOTE, STATEMENT
    
    @Column(name = "template_format", length = 20)
    private String templateFormat; // HTML, PDF, EXCEL, CSV
    
    @Column(name = "template_content", columnDefinition = "LONGTEXT")
    private String templateContent; // HTML/template markup
    
    @Column(name = "template_css", columnDefinition = "TEXT")
    private String templateCss; // Custom CSS for the template
    
    @Column(name = "header_content", columnDefinition = "TEXT")
    private String headerContent;
    
    @Column(name = "footer_content", columnDefinition = "TEXT")
    private String footerContent;
    
    @Column(name = "page_size", length = 20)
    private String pageSize; // A4, LETTER, THERMAL_80MM, THERMAL_58MM
    
    @Column(name = "page_orientation", length = 20)
    private String pageOrientation; // PORTRAIT, LANDSCAPE
    
    @Column(name = "margin_top")
    private Integer marginTop; // in mm
    
    @Column(name = "margin_bottom")
    private Integer marginBottom; // in mm
    
    @Column(name = "margin_left")
    private Integer marginLeft; // in mm
    
    @Column(name = "margin_right")
    private Integer marginRight; // in mm
    
    @Column(name = "show_logo")
    private Boolean showLogo = true;
    
    @Column(name = "show_company_details")
    private Boolean showCompanyDetails = true;
    
    @Column(name = "show_tax_details")
    private Boolean showTaxDetails = true;
    
    @Column(name = "show_terms")
    private Boolean showTerms = true;
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
}
