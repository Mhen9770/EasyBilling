package com.easybilling.listener;

import com.easybilling.context.TenantContext;
import com.easybilling.entity.TenantAware;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;

/**
 * JPA Entity Listener that automatically sets the tenantId on TenantAware entities
 * before persisting or updating them.
 */
@Slf4j
public class TenantEntityListener {
    
    @PrePersist
    public void setTenantOnPersist(Object entity) {
        if (entity instanceof TenantAware) {
            TenantAware tenantAware = (TenantAware) entity;
            String currentTenantId = TenantContext.getTenantId();
            
            if (currentTenantId != null && !currentTenantId.isBlank()) {
                if (tenantAware.getTenantId() == null || tenantAware.getTenantId().isBlank()) {
                    log.debug("Setting tenantId {} on entity {}", currentTenantId, entity.getClass().getSimpleName());
                    tenantAware.setTenantId(currentTenantId);
                } else if (!tenantAware.getTenantId().equals(currentTenantId)) {
                    log.error("Attempted to persist entity with different tenantId. Current: {}, Entity: {}", 
                            currentTenantId, tenantAware.getTenantId());
                    throw new SecurityException("Cannot persist entity for different tenant");
                }
            }
        }
    }
    
    @PreUpdate
    public void setTenantOnUpdate(Object entity) {
        if (entity instanceof TenantAware) {
            TenantAware tenantAware = (TenantAware) entity;
            String currentTenantId = TenantContext.getTenantId();
            
            if (currentTenantId != null && !currentTenantId.isBlank()) {
                if (tenantAware.getTenantId() == null || tenantAware.getTenantId().isBlank()) {
                    log.debug("Setting tenantId {} on entity {}", currentTenantId, entity.getClass().getSimpleName());
                    tenantAware.setTenantId(currentTenantId);
                } else if (!tenantAware.getTenantId().equals(currentTenantId)) {
                    log.error("Attempted to update entity with different tenantId. Current: {}, Entity: {}", 
                            currentTenantId, tenantAware.getTenantId());
                    throw new SecurityException("Cannot update entity for different tenant");
                }
            }
        }
    }
}
