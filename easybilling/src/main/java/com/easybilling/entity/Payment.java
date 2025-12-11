package com.easybilling.entity;

import com.easybilling.enums.PaymentMode;
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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_tenant", columnList = "tenant_id"),
        @Index(name = "idx_payment_invoice", columnList = "invoice_id")
})
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = Integer.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners({AuditingEntityListener.class, TenantEntityListener.class})
public class Payment implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMode mode;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, name = "tenant_id")
    private Integer tenantId;

    private String referenceNumber;
    
    private String cardLast4;
    
    private String upiId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime paidAt;

    private String notes;
}
