package com.runtime.engine.tenant;

import com.runtime.engine.repo.TenantConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantConfigService {
    
    private final TenantConfigRepository tenantConfigRepository;
    private final TenantCache tenantCache;
    
    @Transactional(readOnly = true)
    public Optional<TenantConfig> getTenantConfig(String tenantId) {
        TenantConfig cached = tenantCache.get(tenantId);
        if (cached != null) {
            return Optional.of(cached);
        }
        
        Optional<TenantConfig> config = tenantConfigRepository.findByTenantId(tenantId);
        config.ifPresent(c -> tenantCache.put(tenantId, c));
        
        return config;
    }
    
    @Transactional
    public TenantConfig createTenantConfig(TenantConfig config) {
        log.info("Creating tenant config for: {}", config.getTenantId());
        TenantConfig saved = tenantConfigRepository.save(config);
        tenantCache.put(saved.getTenantId(), saved);
        return saved;
    }
    
    @Transactional
    public TenantConfig updateTenantConfig(String tenantId, TenantConfig config) {
        log.info("Updating tenant config for: {}", tenantId);
        
        TenantConfig existing = tenantConfigRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant config not found: " + tenantId));
        
        if (config.getSettings() != null) {
            existing.setSettings(config.getSettings());
        }
        if (config.getFeatures() != null) {
            existing.setFeatures(config.getFeatures());
        }
        if (config.getLimits() != null) {
            existing.setLimits(config.getLimits());
        }
        if (config.getBranding() != null) {
            existing.setBranding(config.getBranding());
        }
        if (config.getMetadata() != null) {
            existing.setMetadata(config.getMetadata());
        }
        
        TenantConfig updated = tenantConfigRepository.save(existing);
        tenantCache.put(tenantId, updated);
        
        return updated;
    }
    
    @Transactional
    public void deleteTenantConfig(String tenantId) {
        log.info("Deleting tenant config for: {}", tenantId);
        tenantConfigRepository.findByTenantId(tenantId)
            .ifPresent(config -> {
                tenantConfigRepository.delete(config);
                tenantCache.evict(tenantId);
            });
    }
    
    public Object getSetting(String tenantId, String key) {
        return getTenantConfig(tenantId)
            .map(config -> config.getSetting(key))
            .orElse(null);
    }
    
    @Transactional
    public void setSetting(String tenantId, String key, Object value) {
        TenantConfig config = getTenantConfig(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant config not found: " + tenantId));
        
        config.setSetting(key, value);
        TenantConfig updated = tenantConfigRepository.save(config);
        tenantCache.put(tenantId, updated);
    }
    
    public boolean hasFeature(String tenantId, String feature) {
        return getTenantConfig(tenantId)
            .map(config -> config.hasFeature(feature))
            .orElse(false);
    }
    
    public void invalidateCache(String tenantId) {
        tenantCache.evict(tenantId);
        log.info("Invalidated cache for tenant: {}", tenantId);
    }
}
