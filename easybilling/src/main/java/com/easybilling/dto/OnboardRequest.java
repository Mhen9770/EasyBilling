package com.easybilling.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for tenant onboarding request.
 * Contains both tenant and admin user information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardRequest {
    
    // Tenant information
    @NotBlank(message = "Tenant name is required")
    private String tenantName;
    
    @NotBlank(message = "Business name is required")
    private String businessName;
    
    private String businessType;
    
    @NotBlank(message = "Tenant email is required")
    @Email(message = "Invalid email format")
    private String tenantEmail;
    
    @NotBlank(message = "Tenant phone is required")
    private String tenantPhone;
    
    // Optional business details
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private String gstin;
    
    // Admin user information
    @NotBlank(message = "Admin username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username can only contain letters, numbers, underscores and hyphens")
    private String adminUsername;
    
    @NotBlank(message = "Admin email is required")
    @Email(message = "Invalid email format")
    private String adminEmail;
    
    @NotBlank(message = "Admin password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String adminPassword;
    
    private String adminFirstName;
    private String adminLastName;
    private String adminPhone;
}

