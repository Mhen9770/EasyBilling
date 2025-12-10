package com.easybilling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private String barcode;
    private CategoryResponse category;
    private BrandResponse brand;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private BigDecimal mrp;
    private BigDecimal taxRate;
    private String unit;
    private Boolean isActive;
    private Boolean trackStock;
    private Integer lowStockThreshold;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
