package com.easybilling.dto;

import com.easybilling.enums.QuoteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponse {
    private String id;
    private String quoteNumber;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    private QuoteStatus status;
    private LocalDate quoteDate;
    private LocalDate validUntil;
    private List<QuoteItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private String notes;
    private String terms;
    private Integer paymentTermDays;
    private String convertedInvoiceId;
    private LocalDateTime convertedAt;
    private String documentTemplateId;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime sentAt;
    private LocalDateTime acceptedAt;
}
