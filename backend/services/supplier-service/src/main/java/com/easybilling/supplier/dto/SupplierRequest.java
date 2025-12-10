package com.easybilling.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Phone is required")
    private String phone;
    
    private String contactPerson;
    
    private String address;
    
    private String city;
    
    private String state;
    
    private String pincode;
    
    private String country;
    
    private String gstin;
    
    private String website;
    
    private String notes;
    
    private String bankName;
    
    private String accountNumber;
    
    private String ifscCode;
    
    private Integer creditDays;
}
