package com.easybilling.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Location ID is required")
    private String locationId;

    @NotNull(message = "Adjustment type is required")
    private AdjustmentType adjustmentType; // INCREASE, DECREASE, SET

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "Reason is required")
    private String reason; // DAMAGED, EXPIRED, FOUND, THEFT, COUNT_ERROR, RETURN, OTHER

    private String notes;
    private String referenceNumber; // Optional reference number

    public enum AdjustmentType {
        INCREASE, DECREASE, SET
    }
}

