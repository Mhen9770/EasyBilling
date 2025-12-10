package com.easybilling.dto;

import com.easybilling.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoyaltyResponse {
    private String id;
    private String customerId;
    private TransactionType transactionType;
    private Integer points;
    private BigDecimal amount;
    private String invoiceId;
    private String description;
    private Integer balanceAfter;
    private LocalDateTime createdAt;
}
