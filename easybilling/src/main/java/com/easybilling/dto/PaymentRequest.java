package com.easybilling.dto;

import com.easybilling.enums.PaymentMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    @NotNull
    private PaymentMode mode;
    
    @NotNull
    private BigDecimal amount;
    
    private String referenceNumber;
    private String cardLast4;
    private String upiId;
    private String notes;
}
