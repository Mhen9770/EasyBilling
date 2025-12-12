package com.easybilling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteRequest {
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    private LocalDate quoteDate;
    private LocalDate validUntil;
    private List<QuoteItemRequest> items;
    private BigDecimal discount;
    private String notes;
    private String terms;
    private Integer paymentTermDays;
    private String documentTemplateId;
}
