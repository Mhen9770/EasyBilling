package com.easybilling.customer.dto;

import com.easybilling.customer.enums.WalletTransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WalletResponse {
    private String id;
    private String customerId;
    private WalletTransactionType transactionType;
    private BigDecimal amount;
    private String invoiceId;
    private String description;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;
}
