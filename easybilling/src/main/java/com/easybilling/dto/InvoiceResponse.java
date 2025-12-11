package com.easybilling.dto;

import com.easybilling.enums.InvoiceStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class InvoiceResponse {
    private String id;
    private String invoiceNumber;
    private InvoiceStatus status;
    private String storeId;
    private String counterId;
    private String counterNumber;
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;
    private List<InvoiceItemResponse> items;
    private List<PaymentResponse> payments;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime completedAt;
    private String completedBy;
    private String notes;
    
    // GST fields (India)
    private String customerGstin;
    private String supplierGstin;
    private String placeOfSupply;
    private Boolean reverseCharge;
    private Boolean isInterstate;
    private BigDecimal totalCgst;
    private BigDecimal totalSgst;
    private BigDecimal totalIgst;
    private BigDecimal totalCess;
    
    // Custom fields integration
    // Map of customFieldId -> value
    private Map<Long, String> customFields;
}
