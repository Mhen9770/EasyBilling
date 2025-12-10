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
public class HeldInvoiceResponse {
    private String holdReference;
    private String storeId;
    private String counterId;
    private String customerName;
    private String customerPhone;
    private Integer itemCount;
    private BigDecimal totalAmount;
    private LocalDateTime heldAt;
    private String heldBy;
    private String notes;
}

