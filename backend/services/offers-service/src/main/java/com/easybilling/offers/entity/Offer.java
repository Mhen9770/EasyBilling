package com.easybilling.offers.entity;

import com.easybilling.offers.enums.OfferStatus;
import com.easybilling.offers.enums.OfferType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "offers", indexes = {
    @Index(name = "idx_offer_tenant", columnList = "tenant_id"),
    @Index(name = "idx_offer_status", columnList = "status"),
    @Index(name = "idx_offer_dates", columnList = "valid_from, valid_to")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Offer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String tenantId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status = OfferStatus.DRAFT;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal discountValue;
    
    private BigDecimal minimumPurchaseAmount;
    
    private BigDecimal maximumDiscountAmount;
    
    @Column(nullable = false)
    private LocalDateTime validFrom;
    
    @Column(nullable = false)
    private LocalDateTime validTo;
    
    private Integer usageLimit;
    
    private Integer usageCount = 0;
    
    private String applicableProducts; // JSON array of product IDs
    
    private String applicableCategories; // JSON array of category IDs
    
    private Boolean stackable = false;
    
    private Integer priority = 0;
    
    private String termsAndConditions;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
