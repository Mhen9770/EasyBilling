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
public class SystemConfigurationDTO {
    private Long id;
    private String configKey;
    private String configValue;
    private String category;
    private String dataType;
    private String description;
    private Boolean isEditable;
    private Boolean isSensitive;
    private String defaultValue;
    private String validationRule;
    private Instant createdAt;
    private Instant updatedAt;
    private String updatedBy;
}
