package com.easybilling.dto;

import jakarta.validation.constraints.NotEmpty;
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
public class BulkProductUpdateRequest {
    @NotEmpty(message = "Product IDs are required")
    private List<Long> productIds;

    private BulkUpdateType updateType;
    private BigDecimal priceChange; // For PRICE_UPDATE
    private BigDecimal costChange; // For COST_UPDATE
    private Integer stockThresholdChange; // For THRESHOLD_UPDATE
    private Boolean activeStatus; // For STATUS_UPDATE
    private Long categoryId; // For CATEGORY_UPDATE
    private Long brandId; // For BRAND_UPDATE

    public enum BulkUpdateType {
        PRICE_UPDATE,
        COST_UPDATE,
        THRESHOLD_UPDATE,
        STATUS_UPDATE,
        CATEGORY_UPDATE,
        BRAND_UPDATE
    }
}

