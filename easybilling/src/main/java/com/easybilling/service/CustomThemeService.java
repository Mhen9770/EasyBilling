package com.easybilling.service;

import com.easybilling.dto.CustomThemeDTO;
import com.easybilling.entity.CustomTheme;
import com.easybilling.exception.ResourceNotFoundException;
import com.easybilling.repository.CustomThemeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing custom themes and branding.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomThemeService {
    
    private final CustomThemeRepository customThemeRepository;
    
    @Transactional(readOnly = true)
    @Cacheable(value = "theme", key = "#tenantId")
    public CustomThemeDTO getThemeByTenant(Integer tenantId) {
        return customThemeRepository.findByTenantIdAndIsActiveTrue(tenantId)
                .map(this::toDTO)
                .orElse(getDefaultTheme());
    }
    
    @Transactional
    @CacheEvict(value = "theme", key = "#dto.tenantId")
    public CustomThemeDTO createOrUpdateTheme(CustomThemeDTO dto) {
        CustomTheme theme = customThemeRepository.findByTenantId(dto.getTenantId())
                .orElse(CustomTheme.builder()
                        .tenantId(dto.getTenantId())
                        .build());
        
        theme.setThemeName(dto.getThemeName());
        theme.setPrimaryColor(dto.getPrimaryColor());
        theme.setSecondaryColor(dto.getSecondaryColor());
        theme.setAccentColor(dto.getAccentColor());
        theme.setBackgroundColor(dto.getBackgroundColor());
        theme.setTextColor(dto.getTextColor());
        theme.setSuccessColor(dto.getSuccessColor());
        theme.setWarningColor(dto.getWarningColor());
        theme.setErrorColor(dto.getErrorColor());
        theme.setLogoUrl(dto.getLogoUrl());
        theme.setFaviconUrl(dto.getFaviconUrl());
        theme.setCompanyName(dto.getCompanyName());
        theme.setTagline(dto.getTagline());
        theme.setFontFamily(dto.getFontFamily());
        theme.setHeadingFont(dto.getHeadingFont());
        theme.setCustomCss(dto.getCustomCss());
        theme.setSidebarPosition(dto.getSidebarPosition());
        theme.setLayoutMode(dto.getLayoutMode());
        theme.setBorderRadius(dto.getBorderRadius());
        theme.setInvoiceLogoUrl(dto.getInvoiceLogoUrl());
        theme.setInvoiceFooterText(dto.getInvoiceFooterText());
        theme.setInvoiceWatermark(dto.getInvoiceWatermark());
        theme.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        theme.setCreatedBy(dto.getCreatedBy());
        
        theme = customThemeRepository.save(theme);
        log.info("Created/Updated theme for tenant: {}", dto.getTenantId());
        return toDTO(theme);
    }
    
    @Transactional
    @CacheEvict(value = "theme", key = "#tenantId")
    public void deleteTheme(Integer tenantId) {
        CustomTheme theme = customThemeRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Theme not found for tenant: " + tenantId));
        customThemeRepository.delete(theme);
        log.info("Deleted theme for tenant: {}", tenantId);
    }
    
    private CustomThemeDTO getDefaultTheme() {
        return CustomThemeDTO.builder()
                .themeName("Default")
                .primaryColor("#3B82F6")
                .secondaryColor("#6B7280")
                .accentColor("#8B5CF6")
                .backgroundColor("#FFFFFF")
                .textColor("#1F2937")
                .successColor("#10B981")
                .warningColor("#F59E0B")
                .errorColor("#EF4444")
                .fontFamily("Inter")
                .headingFont("Inter")
                .sidebarPosition("left")
                .layoutMode("light")
                .borderRadius("8px")
                .isActive(true)
                .build();
    }
    
    private CustomThemeDTO toDTO(CustomTheme entity) {
        return CustomThemeDTO.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .themeName(entity.getThemeName())
                .primaryColor(entity.getPrimaryColor())
                .secondaryColor(entity.getSecondaryColor())
                .accentColor(entity.getAccentColor())
                .backgroundColor(entity.getBackgroundColor())
                .textColor(entity.getTextColor())
                .successColor(entity.getSuccessColor())
                .warningColor(entity.getWarningColor())
                .errorColor(entity.getErrorColor())
                .logoUrl(entity.getLogoUrl())
                .faviconUrl(entity.getFaviconUrl())
                .companyName(entity.getCompanyName())
                .tagline(entity.getTagline())
                .fontFamily(entity.getFontFamily())
                .headingFont(entity.getHeadingFont())
                .customCss(entity.getCustomCss())
                .sidebarPosition(entity.getSidebarPosition())
                .layoutMode(entity.getLayoutMode())
                .borderRadius(entity.getBorderRadius())
                .invoiceLogoUrl(entity.getInvoiceLogoUrl())
                .invoiceFooterText(entity.getInvoiceFooterText())
                .invoiceWatermark(entity.getInvoiceWatermark())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }
}
