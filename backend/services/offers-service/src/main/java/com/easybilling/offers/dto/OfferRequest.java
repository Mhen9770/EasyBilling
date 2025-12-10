package com.easybilling.offers.dto;

import com.easybilling.offers.enums.OfferType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OfferRequest {
    
    @NotBlank(message = "Offer name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Offer type is required")
    private OfferType type;
    
    @NotNull(message = "Discount value is required")
    private BigDecimal discountValue;
    
    private BigDecimal minimumPurchaseAmount;
    
    private BigDecimal maximumDiscountAmount;
    
    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;
    
    @NotNull(message = "Valid to date is required")
    private LocalDateTime validTo;
    
    private Integer usageLimit;
    
    private List<String> applicableProducts;
    
    private List<String> applicableCategories;
    
    private Boolean stackable;
    
    private Integer priority;
    
    private String termsAndConditions;
}
