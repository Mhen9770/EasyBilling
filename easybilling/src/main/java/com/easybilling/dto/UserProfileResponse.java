package com.easybilling.dto;

import com.easybilling.entity.User.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

/**
 * DTO for user profile response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {
    
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String tenantId;
    private UserStatus status;
    private Set<String> roles;
    private Instant lastLogin;
    private Instant createdAt;
    private Instant updatedAt;
}
