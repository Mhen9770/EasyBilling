package com.easybilling.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * DEPRECATED: This class was used for schema-per-tenant multitenancy strategy.
 * The application now uses same-schema multitenancy with tenantId column filtering.
 * 
 * This class is kept for reference but is no longer used in the application.
 * 
 * @deprecated Use tenant filtering with Hibernate filters instead
 */
@Deprecated
@Slf4j
@Component
public class SchemaMultiTenantConnectionProvider<T> {
    
    // This class is deprecated and no longer used
    // Kept for reference only
}
