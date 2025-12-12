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
public class CustomFieldDTO {
    private Long id;
    private Integer tenantId;
    private String entityType;
    private String fieldName;
    private String fieldLabel;
    private String fieldType;
    private String fieldDescription;
    private String defaultValue;
    private String dropdownOptions;
    private String validationRules;
    private Boolean isRequired;
    private Boolean isSearchable;
    private Boolean isDisplayedInList;
    private Integer displayOrder;
    private Boolean isActive;
    private String placeholderText;
    private String helpText;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
}
