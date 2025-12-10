package com.easybilling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {
    private String searchTerm; // Search in name, SKU, barcode
    private List<Long> categoryIds;
    private List<Long> brandIds;
    private Boolean activeOnly;
    private Boolean lowStockOnly;
    private Boolean outOfStockOnly;
    private Boolean trackStock;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String locationId; // Filter by stock availability at location
    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer size = 20;
    @Builder.Default
    private String sortBy = "name"; // name, price, stock, createdAt
    @Builder.Default
    private String sortDirection = "ASC"; // ASC, DESC
}

