package com.easybilling.customer.dto;

import com.easybilling.customer.enums.CustomerSegment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CustomerResponse {
    private String id;
    private String name;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private CustomerSegment segment;
    private Integer loyaltyPoints;
    private BigDecimal walletBalance;
    private BigDecimal totalSpent;
    private Integer visitCount;
    private LocalDateTime lastVisitDate;
    private Boolean active;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
