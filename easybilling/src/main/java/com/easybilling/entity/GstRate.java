package com.easybilling.entity;

import com.easybilling.listener.TenantEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * GST Rate master data for India.
 * Contains CGST, SGST, IGST, and CESS rates for different tax categories.
 */
@Entity
@Table(name = "gst_rates", indexes = {
        @Index(name = "idx_hsn_code", columnList = "hsn_code"),
        @Index(name = "idx_sac_code", columnList = "sac_code"),
        @Index(name = "idx_active", columnList = "is_active"),
        @Index(name = "idx_gst_rate_tenant", columnList = "tenant_id")
})
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId OR tenant_id IS NULL")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@EntityListeners(TenantEntityListener.class)
public class GstRate implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "hsn_code", length = 20)
    private String hsnCode; // Harmonized System of Nomenclature (for goods)
    
    @Column(name = "sac_code", length = 20)
    private String sacCode; // Service Accounting Code (for services)
    
    @Column(name = "tax_category", nullable = false, length = 50)
    private String taxCategory; // e.g., GST_5, GST_12, GST_18, GST_28
    
    @Column(name = "cgst_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal cgstRate; // Central GST (for intra-state)
    
    @Column(name = "sgst_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal sgstRate; // State GST (for intra-state)
    
    @Column(name = "igst_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal igstRate; // Integrated GST (for inter-state)
    
    @Column(name = "cess_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal cessRate = BigDecimal.ZERO; // Compensation Cess
    
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;
    
    @Column(name = "effective_to")
    private LocalDate effectiveTo;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "tenant_id")
    private String tenantId; // null for global GST rates, specific for tenant-specific overrides
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    /**
     * Get total GST rate (CGST + SGST = IGST)
     */
    public BigDecimal getTotalGstRate() {
        return igstRate != null ? igstRate : 
               (cgstRate != null && sgstRate != null ? cgstRate.add(sgstRate) : BigDecimal.ZERO);
    }
    
    /**
     * Check if rate is currently valid
     */
    public boolean isValidForDate(LocalDate date) {
        if (!isActive) return false;
        if (date.isBefore(effectiveFrom)) return false;
        if (effectiveTo != null && date.isAfter(effectiveTo)) return false;
        return true;
    }
}
