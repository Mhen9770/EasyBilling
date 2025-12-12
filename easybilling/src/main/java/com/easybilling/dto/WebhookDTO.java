package com.easybilling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookDTO {
    private Long id;
    private Integer tenantId;
    private String webhookName;
    private String description;
    private String eventType;
    private String targetUrl;
    private String httpMethod;
    private String headers;
    private String payloadTemplate;
    private String secretKey;
    private Integer retryCount;
    private Integer retryInterval;
    private Integer timeout;
    private Boolean isActive;
    private Instant lastTriggeredAt;
    private Long successCount;
    private Long failureCount;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
}
