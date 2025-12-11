package com.easybilling.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing custom theme/branding settings per tenant.
 * Allows complete white-label customization.
 */
@Entity
@Table(name = "custom_themes", indexes = {
        @Index(name = "idx_theme_tenant", columnList = "tenant_id", unique = true),
        @Index(name = "idx_theme_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CustomTheme implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "theme_name", nullable = false, length = 100)
    private String themeName;
    
    // Color Scheme
    @Column(name = "primary_color", length = 7)
    private String primaryColor; // e.g., "#3B82F6"
    
    @Column(name = "secondary_color", length = 7)
    private String secondaryColor;
    
    @Column(name = "accent_color", length = 7)
    private String accentColor;
    
    @Column(name = "background_color", length = 7)
    private String backgroundColor;
    
    @Column(name = "text_color", length = 7)
    private String textColor;
    
    @Column(name = "success_color", length = 7)
    private String successColor;
    
    @Column(name = "warning_color", length = 7)
    private String warningColor;
    
    @Column(name = "error_color", length = 7)
    private String errorColor;
    
    // Branding
    @Column(name = "logo_url", length = 500)
    private String logoUrl;
    
    @Column(name = "favicon_url", length = 500)
    private String faviconUrl;
    
    @Column(name = "company_name", length = 200)
    private String companyName;
    
    @Column(name = "tagline", length = 500)
    private String tagline;
    
    // Typography
    @Column(name = "font_family", length = 100)
    private String fontFamily; // e.g., "Inter", "Roboto", "Poppins"
    
    @Column(name = "heading_font", length = 100)
    private String headingFont;
    
    // Custom CSS
    @Column(name = "custom_css", columnDefinition = "TEXT")
    private String customCss;
    
    // Layout
    @Column(name = "sidebar_position", length = 20)
    private String sidebarPosition; // "left" or "right"
    
    @Column(name = "layout_mode", length = 20)
    private String layoutMode; // "light", "dark", "auto"
    
    @Column(name = "border_radius", length = 10)
    private String borderRadius; // e.g., "4px", "8px", "12px"
    
    // Invoice Branding
    @Column(name = "invoice_logo_url", length = 500)
    private String invoiceLogoUrl;
    
    @Column(name = "invoice_footer_text", columnDefinition = "TEXT")
    private String invoiceFooterText;
    
    @Column(name = "invoice_watermark", length = 500)
    private String invoiceWatermark;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
}
