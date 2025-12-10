package com.easybilling.tenant.dto;

import com.easybilling.tenant.entity.Tenant.SubscriptionPlan;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a tenant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRequest {
    
    @NotBlank(message = "Tenant name is required")
    private String name;
    
    @NotBlank(message = "Slug is required")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    private String slug;
    
    private String description;
    
    @NotNull(message = "Subscription plan is required")
    private SubscriptionPlan plan;
    
    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    private String contactEmail;
    
    private String contactPhone;
    
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String taxNumber;
    
    private Integer maxUsers;
    private Integer maxStores;
}
