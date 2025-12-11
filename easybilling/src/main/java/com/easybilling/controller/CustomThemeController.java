package com.easybilling.controller;

import com.easybilling.context.TenantContext;
import com.easybilling.dto.CustomThemeDTO;
import com.easybilling.service.CustomThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customization/theme")
@RequiredArgsConstructor
@Tag(name = "Theme Customization", description = "APIs for managing custom themes and branding")
public class CustomThemeController {
    
    private final CustomThemeService customThemeService;
    
    @GetMapping
    @Operation(summary = "Get theme for current tenant")
    public ResponseEntity<CustomThemeDTO> getTheme() {
        Integer tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(customThemeService.getThemeByTenant(tenantId));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Create or update theme")
    public ResponseEntity<CustomThemeDTO> createOrUpdateTheme(@RequestBody CustomThemeDTO dto) {
        Integer tenantId = TenantContext.getTenantId();
        dto.setTenantId(tenantId);
        return ResponseEntity.ok(customThemeService.createOrUpdateTheme(dto));
    }
    
    @DeleteMapping
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Delete theme (revert to default)")
    public ResponseEntity<Void> deleteTheme() {
        Integer tenantId = TenantContext.getTenantId();
        customThemeService.deleteTheme(tenantId);
        return ResponseEntity.noContent().build();
    }
}
