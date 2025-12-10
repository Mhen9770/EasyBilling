package com.easybilling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_wallet", indexes = {
    @Index(name = "idx_wallet_customer", columnList = "customer_id"),
    @Index(name = "idx_wallet_tenant", columnList = "tenant_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CustomerWallet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, unique = true)
    private String customerId;
    
    @Column(nullable = false)
    private String tenantId;
    
    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalTopup = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
