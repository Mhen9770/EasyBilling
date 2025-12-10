package com.easybilling.dto;

import com.easybilling.enums.MovementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockMovementRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Location ID is required")
    private String locationId;

    @NotNull(message = "Movement type is required")
    private MovementType movementType;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    private String referenceType;
    private String referenceId;
    private String notes;
}
