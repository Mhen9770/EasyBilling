package com.easybilling.dto;

import com.easybilling.enums.RecurringFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringInvoiceResponse {
    private String id;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String description;
    private RecurringFrequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextInvoiceDate;
    private LocalDate lastInvoiceDate;
    private Boolean isActive;
    private BigDecimal amount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private Integer paymentTermDays;
    private BigDecimal lateFeePercentage;
    private BigDecimal lateFeeFixedAmount;
    private Integer lateGracePeriodDays;
    private String notes;
    private String terms;
    private String documentTemplateId;
    private Integer invoicesGenerated;
    private Integer maxInvoices;
    private Boolean autoSendEmail;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
