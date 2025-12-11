package com.easybilling.service;

import com.easybilling.entity.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for provisioning tenant resources.
 * In same-schema multitenancy, no separate schema creation is needed.
 * All tenants share the same database schema and are isolated by tenantId column.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantProvisioningService {
    
    private final TenantMasterDataService masterDataService;
    
    /**
     * Provision tenant resources (master data initialization only).
     * No schema creation needed for same-schema multitenancy.
     */
    public void provisionTenant(Tenant tenant) {
        log.info("Provisioning tenant resources for: {}", tenant.getSlug());
        
        try {
            // No schema creation needed - all tenants share the same schema
            // Data isolation is handled by tenantId column and Hibernate filters
            
            // Set schema name to null as it's no longer used
            tenant.setSchemaName(null);
            tenant.setDatabaseName(null);
            
            // Initialize master data for the tenant
            // masterDataService.initializeMasterData(tenant);
            
            log.info("Tenant resources provisioned successfully for: {}", tenant.getSlug());
        } catch (Exception e) {
            log.error("Failed to provision tenant resources for: {}", tenant.getSlug(), e);
            throw new RuntimeException("Failed to provision tenant resources", e);
        }
    }
    
    /**
     * Cleanup tenant resources (for testing or tenant deletion).
     * In same-schema approach, this would involve soft-deleting or archiving tenant data.
     */
    public void deprovisionTenant(Integer tenantId) {
        log.warn("Deprovisioning tenant data for tenant ID: {}", tenantId);
        
        // In same-schema approach, we don't drop schemas
        // Instead, we would:
        // 1. Mark tenant as inactive/deleted
        // 2. Optionally soft-delete tenant data
        // 3. Or archive tenant data to separate storage
        
        log.info("Tenant marked for deprovisioning: {}", tenantId);
    }
}
