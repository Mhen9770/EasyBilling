package com.easybilling.config;

import com.easybilling.context.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * Hibernate tenant identifier resolver for schema-per-tenant strategy.
 * Resolves the current tenant's schema name from TenantContext.
 */
@Slf4j
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {
    
    private static final String DEFAULT_TENANT = "public";
    
    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        
        if (tenantId == null || tenantId.isBlank()) {
            log.trace("No tenant context found, using default: {}", DEFAULT_TENANT);
            return DEFAULT_TENANT;
        }
        
        // Convert tenant ID to schema name (e.g., "tenant1" -> "tenant_tenant1")
        String schemaName = "tenant_" + tenantId;
        log.trace("Resolved tenant schema: {}", schemaName);
        return schemaName;
    }
    
    @Override
    public boolean validateExistingCurrentSessions() {
        // Return true to validate that the schema exists for each session
        return true;
    }
}
