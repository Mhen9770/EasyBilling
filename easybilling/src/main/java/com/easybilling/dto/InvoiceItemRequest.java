package com.easybilling.dto;

import com.easybilling.enums.DiscountType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceItemRequest {
    @NotBlank
    private String productId;
    
    @NotBlank
    private String productName;
    
    private String productCode;
    private String barcode;
    
    @NotNull
    @Min(1)
    private Integer quantity;
    
    @NotNull
    private BigDecimal unitPrice;
    
    private BigDecimal discountAmount;
    private DiscountType discountType;
    private BigDecimal discountValue;
    
    private BigDecimal taxAmount;
    private BigDecimal taxRate;
    
    private String notes;
}
