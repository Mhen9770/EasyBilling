package com.easybilling.dto;

import com.easybilling.enums.OfferStatus;
import com.easybilling.enums.OfferType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OfferResponse {
    private String id;
    private String name;
    private String description;
    private OfferType type;
    private OfferStatus status;
    private BigDecimal discountValue;
    private BigDecimal minimumPurchaseAmount;
    private BigDecimal maximumDiscountAmount;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Integer usageLimit;
    private Integer usageCount;
    private String applicableProducts;
    private String applicableCategories;
    private Boolean stackable;
    private Integer priority;
    private String termsAndConditions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
