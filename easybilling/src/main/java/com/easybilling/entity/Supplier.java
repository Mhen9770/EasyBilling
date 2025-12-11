package com.easybilling.entity;

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

@Entity
@Table(name = "suppliers", indexes = {
    @Index(name = "idx_supplier_phone", columnList = "phone"),
    @Index(name = "idx_supplier_email", columnList = "email"),
    @Index(name = "idx_supplier_tenant", columnList = "tenant_id"),
    @Index(name = "idx_supplier_gstin", columnList = "gstin")
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class, TenantEntityListener.class})
public class Supplier implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private Integer tenantId;
    
    @Column(nullable = false)
    private String name;
    
    private String email;
    
    @Column(nullable = false)
    private String phone;
    
    private String contactPerson;
    
    private String address;
    
    private String city;
    
    private String state;
    
    private String pincode;
    
    private String country;
    
    private String gstin;
    
    private String website;
    
    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalPurchases = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal outstandingBalance = BigDecimal.ZERO;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer purchaseCount = 0;
    
    private LocalDateTime lastPurchaseDate;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    private String notes;
    
    private String bankName;
    
    private String accountNumber;
    
    private String ifscCode;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer creditDays = 0;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
