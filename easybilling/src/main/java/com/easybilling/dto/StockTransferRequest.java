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
public class StockTransferRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "From location is required")
    private String fromLocationId;

    @NotNull(message = "To location is required")
    private String toLocationId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    private String notes;
    private String referenceNumber; // Optional transfer reference
}

