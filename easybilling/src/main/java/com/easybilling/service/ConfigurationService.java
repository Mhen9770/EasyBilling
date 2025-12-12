package com.easybilling.service;

import com.easybilling.context.TenantContext;
import com.easybilling.dto.SystemConfigurationDTO;
import com.easybilling.dto.TenantConfigurationDTO;
import com.easybilling.entity.SystemConfiguration;
import com.easybilling.entity.TenantConfiguration;
import com.easybilling.exception.ResourceNotFoundException;
import com.easybilling.repository.SystemConfigurationRepository;
import com.easybilling.repository.TenantConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing system and tenant configurations.
 * Provides hierarchical configuration lookup: tenant config overrides system config.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigurationService {
    
    private final SystemConfigurationRepository systemConfigRepository;
    private final TenantConfigurationRepository tenantConfigRepository;
    
    /**
     * Get configuration value with tenant override support.
     * First checks tenant-specific config, then falls back to system config.
     */
    @Cacheable(value = "config", key = "#configKey + '_' + #tenantId")
    public String getConfigValue(String configKey, Integer tenantId) {
        if (tenantId != null) {
            return tenantConfigRepository.findByTenantIdAndConfigKey(tenantId, configKey)
                    .map(TenantConfiguration::getConfigValue)
                    .orElseGet(() -> getSystemConfigValue(configKey));
        }
        return getSystemConfigValue(configKey);
    }
    
    /**
     * Get configuration value for current tenant from context.
     */
    public String getConfigValue(String configKey) {
        Integer tenantId = TenantContext.getTenantId();
        return getConfigValue(configKey, tenantId);
    }
    
    /**
     * Get system configuration value.
     */
    @Cacheable(value = "systemConfig", key = "#configKey")
    public String getSystemConfigValue(String configKey) {
        return systemConfigRepository.findByConfigKey(configKey)
                .map(SystemConfiguration::getConfigValue)
                .orElse(null);
    }
    
    /**
     * Get configuration as integer.
     */
    public Integer getConfigValueAsInt(String configKey, Integer defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid integer config value for key {}, using default value", configKey);
            return defaultValue;
        }
    }
    
    /**
     * Get configuration as boolean.
     */
    public Boolean getConfigValueAsBoolean(String configKey, Boolean defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Get configuration as double.
     */
    public Double getConfigValueAsDouble(String configKey, Double defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid double config value for key {}, using default value", configKey);
            return defaultValue;
        }
    }
    
    // System Configuration CRUD
    
    @Transactional(readOnly = true)
    public List<SystemConfigurationDTO> getAllSystemConfigurations() {
        return systemConfigRepository.findAll().stream()
                .map(this::toSystemConfigDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<SystemConfigurationDTO> getSystemConfigurationsByCategory(String category) {
        return systemConfigRepository.findByCategory(category).stream()
                .map(this::toSystemConfigDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    @CacheEvict(value = "systemConfig", key = "#dto.configKey")
    public SystemConfigurationDTO createSystemConfiguration(SystemConfigurationDTO dto) {
        if (systemConfigRepository.existsByConfigKey(dto.getConfigKey())) {
            throw new IllegalArgumentException("Configuration key already exists: " + dto.getConfigKey());
        }
        
        SystemConfiguration config = SystemConfiguration.builder()
                .configKey(dto.getConfigKey())
                .configValue(dto.getConfigValue())
                .category(dto.getCategory())
                .dataType(dto.getDataType())
                .description(dto.getDescription())
                .isEditable(dto.getIsEditable())
                .isSensitive(dto.getIsSensitive())
                .defaultValue(dto.getDefaultValue())
                .validationRule(dto.getValidationRule())
                .updatedBy(dto.getUpdatedBy())
                .build();
        
        config = systemConfigRepository.save(config);
        log.info("Created system configuration: {}", config.getConfigKey());
        return toSystemConfigDTO(config);
    }
    
    @Transactional
    @CacheEvict(value = "systemConfig", key = "#configKey")
    public SystemConfigurationDTO updateSystemConfiguration(String configKey, SystemConfigurationDTO dto) {
        SystemConfiguration config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("System configuration not found: " + configKey));
        
        if (!config.getIsEditable()) {
            throw new IllegalArgumentException("Configuration is not editable: " + configKey);
        }
        
        config.setConfigValue(dto.getConfigValue());
        config.setDescription(dto.getDescription());
        config.setUpdatedBy(dto.getUpdatedBy());
        
        config = systemConfigRepository.save(config);
        log.info("Updated system configuration: {}", configKey);
        return toSystemConfigDTO(config);
    }
    
    // Tenant Configuration CRUD
    
    @Transactional(readOnly = true)
    public List<TenantConfigurationDTO> getTenantConfigurations(Integer tenantId) {
        return tenantConfigRepository.findByTenantId(tenantId).stream()
                .map(this::toTenantConfigDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TenantConfigurationDTO> getTenantConfigurationsByCategory(Integer tenantId, String category) {
        return tenantConfigRepository.findByTenantIdAndCategory(tenantId, category).stream()
                .map(this::toTenantConfigDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    @CacheEvict(value = "config", key = "#dto.configKey + '_' + #dto.tenantId")
    public TenantConfigurationDTO createOrUpdateTenantConfiguration(TenantConfigurationDTO dto) {
        TenantConfiguration config = tenantConfigRepository
                .findByTenantIdAndConfigKey(dto.getTenantId(), dto.getConfigKey())
                .orElse(TenantConfiguration.builder()
                        .tenantId(dto.getTenantId())
                        .configKey(dto.getConfigKey())
                        .build());
        
        config.setConfigValue(dto.getConfigValue());
        config.setCategory(dto.getCategory());
        config.setDataType(dto.getDataType());
        config.setDescription(dto.getDescription());
        config.setIsOverride(dto.getIsOverride());
        config.setUpdatedBy(dto.getUpdatedBy());
        
        config = tenantConfigRepository.save(config);
        log.info("Created/Updated tenant configuration: {} for tenant: {}", config.getConfigKey(), config.getTenantId());
        return toTenantConfigDTO(config);
    }
    
    @Transactional
    @CacheEvict(value = "config", key = "#configKey + '_' + #tenantId")
    public void deleteTenantConfiguration(Integer tenantId, String configKey) {
        tenantConfigRepository.deleteByTenantIdAndConfigKey(tenantId, configKey);
        log.info("Deleted tenant configuration: {} for tenant: {}", configKey, tenantId);
    }
    
    // Helper methods
    
    private SystemConfigurationDTO toSystemConfigDTO(SystemConfiguration entity) {
        return SystemConfigurationDTO.builder()
                .id(entity.getId())
                .configKey(entity.getConfigKey())
                .configValue(entity.getIsSensitive() ? "***" : entity.getConfigValue())
                .category(entity.getCategory())
                .dataType(entity.getDataType())
                .description(entity.getDescription())
                .isEditable(entity.getIsEditable())
                .isSensitive(entity.getIsSensitive())
                .defaultValue(entity.getDefaultValue())
                .validationRule(entity.getValidationRule())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
    
    private TenantConfigurationDTO toTenantConfigDTO(TenantConfiguration entity) {
        return TenantConfigurationDTO.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .configKey(entity.getConfigKey())
                .configValue(entity.getConfigValue())
                .category(entity.getCategory())
                .dataType(entity.getDataType())
                .description(entity.getDescription())
                .isOverride(entity.getIsOverride())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
