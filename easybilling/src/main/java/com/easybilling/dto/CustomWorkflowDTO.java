package com.easybilling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for CustomWorkflow entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomWorkflowDTO {
    private Long id;
    private Integer tenantId;
    private String workflowName;
    private String description;
    private String triggerEvent;
    private String conditions; // JSON string
    private String actions; // JSON string
    private Integer executionOrder;
    private Boolean isActive;
    private Instant lastExecutedAt;
    private Long executionCount;
    private Long failureCount;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
}
