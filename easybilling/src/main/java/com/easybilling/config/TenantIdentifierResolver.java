package com.easybilling.config;

import com.easybilling.context.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * DEPRECATED: This class was used for schema-per-tenant multitenancy strategy.
 * The application now uses same-schema multitenancy with tenantId column filtering.
 * 
 * Tenant identification is still resolved from context, but schema switching is no longer performed.
 * 
 * @deprecated Tenant filtering is now handled by Hibernate filters on entities
 */
@Deprecated
@Slf4j
@Component
public class TenantIdentifierResolver {
    
    // This class is deprecated for schema resolution
    // TenantContext is still used for tenant identification
}
