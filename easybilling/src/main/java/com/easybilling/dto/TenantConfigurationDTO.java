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
public class TenantConfigurationDTO {
    private Long id;
    private Integer tenantId;
    private String configKey;
    private String configValue;
    private String category;
    private String dataType;
    private String description;
    private Boolean isOverride;
    private Instant createdAt;
    private Instant updatedAt;
    private String updatedBy;
}
