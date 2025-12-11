package com.easybilling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class InvoiceRequest {
    @NotBlank
    private String storeId;
    
    @NotBlank
    private String counterId;
    
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    
    @NotNull
    private List<InvoiceItemRequest> items;
    
    private String notes;
    
    // GST fields (India)
    private String customerGstin;
    private String placeOfSupply;
    private String supplierGstin;
    private Boolean reverseCharge;
    
    // Custom fields integration
    // Map of customFieldId -> value
    private Map<Long, String> customFields;
}
