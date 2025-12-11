package com.easybilling.dto;

import com.easybilling.enums.CustomerSegment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private String gstin;
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
