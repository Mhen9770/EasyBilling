package com.easybilling.entity;

import com.easybilling.enums.InvoiceStatus;
import com.easybilling.listener.TenantEntityListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = Integer.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners({AuditingEntityListener.class, TenantEntityListener.class})
public class Invoice implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    @Column(nullable = false)
    private Integer tenantId;

    @Column(nullable = false)
    private String storeId;

    @Column(nullable = false)
    private String counterId;

    private String customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    @Column(nullable = false)
    private String createdBy;

    private String completedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal paidAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal balanceAmount;

    // GST fields for India
    @Column(name = "total_cgst", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalCgst = BigDecimal.ZERO;
    
    @Column(name = "total_sgst", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalSgst = BigDecimal.ZERO;
    
    @Column(name = "total_igst", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalIgst = BigDecimal.ZERO;
    
    @Column(name = "total_cess", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalCess = BigDecimal.ZERO;
    
    @Column(name = "place_of_supply", length = 50)
    private String placeOfSupply; // State name or code
    
    @Column(name = "supplier_gstin", length = 15)
    private String supplierGstin; // Tenant's GSTIN
    
    @Column(name = "customer_gstin", length = 15)
    private String customerGstin; // Customer's GSTIN (if B2B)
    
    @Column(name = "reverse_charge")
    @Builder.Default
    private Boolean reverseCharge = false;
    
    @Column(name = "is_interstate")
    @Builder.Default
    private Boolean isInterstate = false;

    private String notes;

    // Helper methods
    public void addItem(InvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
    }

    public void removeItem(InvoiceItem item) {
        items.remove(item);
        item.setInvoice(null);
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setInvoice(this);
    }

    public void calculateTotals() {
        this.subtotal = items.stream()
                .map(InvoiceItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.taxAmount = items.stream()
                .map(InvoiceItem::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.discountAmount = items.stream()
                .map(InvoiceItem::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate GST totals
        this.totalCgst = items.stream()
                .map(InvoiceItem::getCgstAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.totalSgst = items.stream()
                .map(InvoiceItem::getSgstAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.totalIgst = items.stream()
                .map(InvoiceItem::getIgstAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.totalCess = items.stream()
                .map(InvoiceItem::getCessAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = subtotal.add(taxAmount).subtract(discountAmount);

        this.paidAmount = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.balanceAmount = totalAmount.subtract(paidAmount);
    }
}
