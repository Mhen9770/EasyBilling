package com.easybilling.entity;

import com.easybilling.enums.DiscountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItem {

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

    private String notes;

    public void calculateLineTotal() {
        BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal netAmount = gross.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        this.lineTotal = netAmount.add(taxAmount != null ? taxAmount : BigDecimal.ZERO);
    }
}
