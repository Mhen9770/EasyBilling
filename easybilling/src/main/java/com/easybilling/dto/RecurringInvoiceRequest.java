package com.easybilling.dto;

import com.easybilling.enums.RecurringFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringInvoiceRequest {
    private String customerId;
    private String description;
    private RecurringFrequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal amount;
    private BigDecimal taxRate;
    private Integer paymentTermDays;
    private BigDecimal lateFeePercentage;
    private BigDecimal lateFeeFixedAmount;
    private Integer lateGracePeriodDays;
    private String notes;
    private String terms;
    private String documentTemplateId;
    private Integer maxInvoices;
    private Boolean autoSendEmail;
}
