package com.easybilling.entity;

import com.easybilling.enums.DiscountType;
import com.easybilling.listener.TenantEntityListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items", indexes = {
        @Index(name = "idx_invoice_item_tenant", columnList = "tenant_id"),
        @Index(name = "idx_invoice_item_invoice", columnList = "invoice_id")
})
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(TenantEntityListener.class)
public class InvoiceItem implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private String productName;

    private String productCode;
    private String barcode;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @Column(precision = 5, scale = 2)
    private BigDecimal discountValue;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;
    
    @Column(nullable = false, name = "tenant_id")
    private String tenantId;
    
    // GST fields for India
    @Column(name = "hsn_code", length = 20)
    private String hsnCode; // Harmonized System of Nomenclature (for goods)
    
    @Column(name = "sac_code", length = 20)
    private String sacCode; // Service Accounting Code (for services)
    
    @Column(name = "cgst_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal cgstRate = BigDecimal.ZERO;
    
    @Column(name = "sgst_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal sgstRate = BigDecimal.ZERO;
    
    @Column(name = "igst_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal igstRate = BigDecimal.ZERO;
    
    @Column(name = "cess_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal cessRate = BigDecimal.ZERO;
    
    @Column(name = "cgst_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal cgstAmount = BigDecimal.ZERO;
    
    @Column(name = "sgst_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal sgstAmount = BigDecimal.ZERO;
    
    @Column(name = "igst_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal igstAmount = BigDecimal.ZERO;
    
    @Column(name = "cess_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal cessAmount = BigDecimal.ZERO;

    private String notes;

    public void calculateLineTotal() {
        BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal netAmount = gross.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        
        // Calculate total tax (legacy tax + GST)
        BigDecimal gstTotal = (cgstAmount != null ? cgstAmount : BigDecimal.ZERO)
                .add(sgstAmount != null ? sgstAmount : BigDecimal.ZERO)
                .add(igstAmount != null ? igstAmount : BigDecimal.ZERO)
                .add(cessAmount != null ? cessAmount : BigDecimal.ZERO);
        
        this.taxAmount = gstTotal;
        this.lineTotal = netAmount.add(this.taxAmount);
    }
    
    /**
     * Calculate GST amounts based on rates and taxable amount
     * @param isInterstate true if inter-state transaction (use IGST), false for intra-state (use CGST+SGST)
     */
    public void calculateGst(boolean isInterstate) {
        BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal taxableAmount = gross.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        
        if (isInterstate) {
            // Inter-state: use IGST
            this.igstAmount = taxableAmount.multiply(igstRate != null ? igstRate : BigDecimal.ZERO)
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            this.cgstAmount = BigDecimal.ZERO;
            this.sgstAmount = BigDecimal.ZERO;
        } else {
            // Intra-state: use CGST + SGST
            this.cgstAmount = taxableAmount.multiply(cgstRate != null ? cgstRate : BigDecimal.ZERO)
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            this.sgstAmount = taxableAmount.multiply(sgstRate != null ? sgstRate : BigDecimal.ZERO)
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            this.igstAmount = BigDecimal.ZERO;
        }
        
        // Calculate CESS
        this.cessAmount = taxableAmount.multiply(cessRate != null ? cessRate : BigDecimal.ZERO)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        
        // Recalculate line total
        calculateLineTotal();
    }
}
