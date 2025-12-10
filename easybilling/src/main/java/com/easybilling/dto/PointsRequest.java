package com.easybilling.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PointsRequest {
    
    @NotNull(message = "Points are required")
    @Min(value = 1, message = "Points must be at least 1")
    private Integer points;
    
    private BigDecimal amount;
    
    private String invoiceId;
    
    private String description;
}
