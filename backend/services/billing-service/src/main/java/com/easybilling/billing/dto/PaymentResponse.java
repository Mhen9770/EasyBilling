package com.easybilling.billing.dto;

import com.easybilling.billing.enums.PaymentMode;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private String id;
    private PaymentMode mode;
    private BigDecimal amount;
    private String referenceNumber;
    private LocalDateTime paidAt;
}
