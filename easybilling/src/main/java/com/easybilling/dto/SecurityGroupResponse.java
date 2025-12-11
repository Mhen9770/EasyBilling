package com.easybilling.dto;

import com.easybilling.enums.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

/**
 * Response DTO for security group.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityGroupResponse {
    
    private String id;
    private String name;
    private String description;
    private String tenantId;
    private Set<Permission> permissions;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Integer userCount; // Number of users assigned to this group
}
