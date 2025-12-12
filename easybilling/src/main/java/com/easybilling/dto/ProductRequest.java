package com.easybilling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    private String description;
    
    private String sku;

    @NotBlank(message = "Barcode is required")
    private String barcode;

    private Long categoryId;
    private Long brandId;

    @NotNull(message = "Cost price is required")
    @Positive(message = "Cost price must be positive")
    private BigDecimal costPrice;

    @NotNull(message = "Selling price is required")
    @Positive(message = "Selling price must be positive")
    private BigDecimal sellingPrice;

    @NotNull(message = "MRP is required")
    @Positive(message = "MRP must be positive")
    private BigDecimal mrp;

    private BigDecimal taxRate;

    @NotBlank(message = "Unit is required")
    private String unit;

    private Boolean trackStock = true;
    private Integer lowStockThreshold = 10;
    private String imageUrl;
    
    // Custom fields integration
    private Map<Long, String> customFields;
}
