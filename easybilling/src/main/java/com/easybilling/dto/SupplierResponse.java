package com.easybilling.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class SupplierResponse {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String contactPerson;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private String gstin;
    private String website;
    private BigDecimal totalPurchases;
    private BigDecimal outstandingBalance;
    private Integer purchaseCount;
    private LocalDateTime lastPurchaseDate;
    private Boolean active;
    private String notes;
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private Integer creditDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Custom fields integration
    private Map<Long, String> customFields;
}
