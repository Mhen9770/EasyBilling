package com.runtime.engine.tenant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class TenantCache {
    
    private final Map<String, TenantConfig> cache = new ConcurrentHashMap<>();
    
    public TenantConfig get(String tenantId) {
        return cache.get(tenantId);
    }
    
    public void put(String tenantId, TenantConfig config) {
        cache.put(tenantId, config);
        log.debug("Cached tenant config for: {}", tenantId);
    }
    
    public void evict(String tenantId) {
        cache.remove(tenantId);
        log.debug("Evicted tenant config from cache: {}", tenantId);
    }
    
    public void clear() {
        cache.clear();
        log.info("Tenant cache cleared");
    }
    
    public int size() {
        return cache.size();
    }
    
    public boolean contains(String tenantId) {
        return cache.containsKey(tenantId);
    }
}
