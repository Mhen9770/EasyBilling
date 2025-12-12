package com.easybilling.entity;

import com.easybilling.enums.RecurringFrequency;
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

/**
 * Recurring Invoice entity for subscription-based or periodic billing.
 * Automatically generates invoices based on configured frequency.
 */
@Entity
@Table(name = "recurring_invoices", indexes = {
        @Index(name = "idx_recurring_tenant", columnList = "tenant_id"),
        @Index(name = "idx_recurring_customer", columnList = "customer_id"),
        @Index(name = "idx_recurring_next_date", columnList = "next_invoice_date"),
        @Index(name = "idx_recurring_active", columnList = "is_active")
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners({AuditingEntityListener.class, TenantEntityListener.class})
public class RecurringInvoice implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private Integer tenantId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String customerName;

    private String customerEmail;
    private String customerPhone;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurringFrequency frequency;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate; // null for indefinite

    @Column(nullable = false)
    private LocalDate nextInvoiceDate;

    private LocalDate lastInvoiceDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    private BigDecimal taxRate;

    @Column(precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Payment terms in days (e.g., Net 30, Net 15)
    @Column(nullable = false)
    @Builder.Default
    private Integer paymentTermDays = 30;

    // Late fee configuration
    @Column(precision = 5, scale = 2)
    private BigDecimal lateFeePercentage;

    @Column(precision = 10, scale = 2)
    private BigDecimal lateFeeFixedAmount;

    // Grace period before late fee applies (in days)
    @Builder.Default
    private Integer lateGracePeriodDays = 0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String terms;

    // Template to use for generated invoices
    private String documentTemplateId;

    // Counter for generated invoices
    @Builder.Default
    private Integer invoicesGenerated = 0;

    // Maximum invoices to generate (null for unlimited)
    private Integer maxInvoices;

    // Auto-send email when invoice generated
    @Builder.Default
    private Boolean autoSendEmail = false;

    @Column(nullable = false)
    private String createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime pausedAt;
    private String pausedBy;
    private String pauseReason;
}
