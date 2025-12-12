package com.easybilling.entity;

import com.easybilling.enums.QuoteStatus;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Quote/Estimate entity for providing price estimates before conversion to invoice.
 * Can be converted to Invoice once accepted by customer.
 */
@Entity
@Table(name = "quotes", indexes = {
        @Index(name = "idx_quote_tenant", columnList = "tenant_id"),
        @Index(name = "idx_quote_customer", columnList = "customer_id"),
        @Index(name = "idx_quote_number", columnList = "quote_number"),
        @Index(name = "idx_quote_status", columnList = "status")
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners({AuditingEntityListener.class, TenantEntityListener.class})
public class Quote implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String quoteNumber;

    @Column(nullable = false)
    private Integer tenantId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String customerName;

    private String customerEmail;
    private String customerPhone;
    private String customerAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private QuoteStatus status = QuoteStatus.DRAFT;

    @Column(nullable = false)
    private LocalDate quoteDate;

    @Column(nullable = false)
    private LocalDate validUntil;

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QuoteItem> items = new ArrayList<>();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String terms;

    // Payment terms in days
    private Integer paymentTermDays;

    // Reference to converted invoice (if accepted)
    private String convertedInvoiceId;
    private LocalDateTime convertedAt;

    // Template to use for PDF generation
    private String documentTemplateId;

    @Column(nullable = false)
    private String createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime sentAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime rejectedAt;
    private String rejectionReason;
}
