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
public class CustomThemeDTO {
    private Long id;
    private Integer tenantId;
    private String themeName;
    private String primaryColor;
    private String secondaryColor;
    private String accentColor;
    private String backgroundColor;
    private String textColor;
    private String successColor;
    private String warningColor;
    private String errorColor;
    private String logoUrl;
    private String faviconUrl;
    private String companyName;
    private String tagline;
    private String fontFamily;
    private String headingFont;
    private String customCss;
    private String sidebarPosition;
    private String layoutMode;
    private String borderRadius;
    private String invoiceLogoUrl;
    private String invoiceFooterText;
    private String invoiceWatermark;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
}
