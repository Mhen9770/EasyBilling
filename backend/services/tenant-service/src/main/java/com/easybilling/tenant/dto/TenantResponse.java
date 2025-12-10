package com.easybilling.tenant.dto;

import com.easybilling.tenant.entity.Tenant.SubscriptionPlan;
import com.easybilling.tenant.entity.Tenant.TenantStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for tenant response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TenantResponse {
    
    private String id;
    private String name;
    private String slug;
    private String description;
    private TenantStatus status;
    private SubscriptionPlan plan;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String taxNumber;
    private Instant subscriptionStartDate;
    private Instant subscriptionEndDate;
    private Instant trialEndDate;
    private Integer maxUsers;
    private Integer maxStores;
    private Instant createdAt;
    private Instant updatedAt;
}
