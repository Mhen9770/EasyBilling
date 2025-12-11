package com.easybilling.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InvoiceItemResponse {
    private String id;
    private String productId;
    private String productName;
    private String productCode;
    private String barcode;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal lineTotal;
    
    // GST fields (India)
    private String hsnCode;
    private String sacCode;
    private BigDecimal cgstRate;
    private BigDecimal sgstRate;
    private BigDecimal igstRate;
    private BigDecimal cessRate;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal cessAmount;
}
