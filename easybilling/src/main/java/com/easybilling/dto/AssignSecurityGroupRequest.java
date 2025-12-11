package com.easybilling.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Request DTO for assigning security groups to a user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignSecurityGroupRequest {
    
    @NotEmpty(message = "Security group IDs are required")
    private Set<String> securityGroupIds;
}
