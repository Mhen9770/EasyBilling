package com.easybilling.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Phone is required")
    private String phone;
    
    private LocalDate dateOfBirth;
    
    private String address;
    
    private String city;
    
    private String state;
    
    private String pincode;
    
    private String gstin;
    
    private String notes;
}
