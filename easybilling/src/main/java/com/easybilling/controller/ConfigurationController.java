package com.easybilling.controller;

import com.easybilling.context.TenantContext;
import com.easybilling.dto.SystemConfigurationDTO;
import com.easybilling.dto.TenantConfigurationDTO;
import com.easybilling.service.ConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/configurations")
@RequiredArgsConstructor
@Tag(name = "Configuration Management", description = "APIs for managing system and tenant configurations")
public class ConfigurationController {
    
    private final ConfigurationService configurationService;
    
    // System Configuration Endpoints
    
    @GetMapping("/system")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM_SETTINGS')")
    @Operation(summary = "Get all system configurations")
    public ResponseEntity<List<SystemConfigurationDTO>> getAllSystemConfigurations() {
        return ResponseEntity.ok(configurationService.getAllSystemConfigurations());
    }
    
    @GetMapping("/system/category/{category}")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM_SETTINGS')")
    @Operation(summary = "Get system configurations by category")
    public ResponseEntity<List<SystemConfigurationDTO>> getSystemConfigurationsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(configurationService.getSystemConfigurationsByCategory(category));
    }
    
    @PostMapping("/system")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM_SETTINGS')")
    @Operation(summary = "Create system configuration")
    public ResponseEntity<SystemConfigurationDTO> createSystemConfiguration(@RequestBody SystemConfigurationDTO dto) {
        return ResponseEntity.ok(configurationService.createSystemConfiguration(dto));
    }
    
    @PutMapping("/system/{configKey}")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM_SETTINGS')")
    @Operation(summary = "Update system configuration")
    public ResponseEntity<SystemConfigurationDTO> updateSystemConfiguration(
            @PathVariable String configKey, 
            @RequestBody SystemConfigurationDTO dto) {
        return ResponseEntity.ok(configurationService.updateSystemConfiguration(configKey, dto));
    }
    
    // Tenant Configuration Endpoints
    
    @GetMapping("/tenant")
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Get all tenant configurations for current tenant")
    public ResponseEntity<List<TenantConfigurationDTO>> getTenantConfigurations() {
        Integer tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(configurationService.getTenantConfigurations(tenantId));
    }
    
    @GetMapping("/tenant/category/{category}")
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Get tenant configurations by category")
    public ResponseEntity<List<TenantConfigurationDTO>> getTenantConfigurationsByCategory(@PathVariable String category) {
        Integer tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(configurationService.getTenantConfigurationsByCategory(tenantId, category));
    }
    
    @PostMapping("/tenant")
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Create or update tenant configuration")
    public ResponseEntity<TenantConfigurationDTO> createOrUpdateTenantConfiguration(@RequestBody TenantConfigurationDTO dto) {
        Integer tenantId = TenantContext.getTenantId();
        dto.setTenantId(tenantId);
        return ResponseEntity.ok(configurationService.createOrUpdateTenantConfiguration(dto));
    }
    
    @DeleteMapping("/tenant/{configKey}")
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Delete tenant configuration")
    public ResponseEntity<Void> deleteTenantConfiguration(@PathVariable String configKey) {
        Integer tenantId = TenantContext.getTenantId();
        configurationService.deleteTenantConfiguration(tenantId, configKey);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/value/{configKey}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get configuration value (supports tenant override)")
    public ResponseEntity<String> getConfigValue(@PathVariable String configKey) {
        String value = configurationService.getConfigValue(configKey);
        return ResponseEntity.ok(value);
    }
}
