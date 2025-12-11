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
public class DocumentTemplateDTO {
    private Long id;
    private Integer tenantId;
    private String templateName;
    private String templateType;
    private String templateFormat;
    private String templateContent;
    private String templateCss;
    private String headerContent;
    private String footerContent;
    private String pageSize;
    private String pageOrientation;
    private Integer marginTop;
    private Integer marginBottom;
    private Integer marginLeft;
    private Integer marginRight;
    private Boolean showLogo;
    private Boolean showCompanyDetails;
    private Boolean showTaxDetails;
    private Boolean showTerms;
    private Boolean isDefault;
    private Boolean isActive;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
}
