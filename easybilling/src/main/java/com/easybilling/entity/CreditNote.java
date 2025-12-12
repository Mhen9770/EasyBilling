package com.easybilling.entity;

import com.easybilling.enums.CreditNoteReason;
import com.easybilling.enums.CreditNoteStatus;
import com.easybilling.listener.TenantEntityListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Credit Note entity for handling refunds, returns, and invoice corrections.
 * Reduces the amount owed by customer or provides store credit.
 */
@Entity
@Table(name = "credit_notes", indexes = {
        @Index(name = "idx_credit_tenant", columnList = "tenant_id"),
        @Index(name = "idx_credit_invoice", columnList = "invoice_id"),
        @Index(name = "idx_credit_customer", columnList = "customer_id"),
        @Index(name = "idx_credit_number", columnList = "credit_note_number")
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners({AuditingEntityListener.class, TenantEntityListener.class})
public class CreditNote implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String creditNoteNumber;

    @Column(nullable = false)
    private Integer tenantId;

    @Column(nullable = false)
    private String invoiceId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CreditNoteStatus status = CreditNoteStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditNoteReason reason;

    @OneToMany(mappedBy = "creditNote", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CreditNoteItem> items = new ArrayList<>();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // How credit should be applied
    @Builder.Default
    private Boolean appliedToInvoice = true;  // Reduce invoice amount

    @Builder.Default
    private Boolean refundIssued = false;     // Cash/payment refund

    @Builder.Default
    private Boolean storeCredit = false;      // Customer wallet credit

    @Column(precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal storeCreditAmount;

    private String refundReferenceNumber;
    private LocalDateTime refundedAt;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalTax = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String internalNotes;

    // Approval workflow
    @Builder.Default
    private Boolean requiresApproval = false;

    private String approvedBy;
    private LocalDateTime approvedAt;
    private String approvalNotes;

    @Builder.Default
    private Boolean restockItems = false;

    private String updatedBy;

    @Column(nullable = false)
    private String createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime issuedAt;
    private LocalDateTime approvedDate;
    private LocalDateTime issuedDate;
}
