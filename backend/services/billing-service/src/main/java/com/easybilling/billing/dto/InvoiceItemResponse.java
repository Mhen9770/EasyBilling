package com.easybilling.billing.dto;

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
}
