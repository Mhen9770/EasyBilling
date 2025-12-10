package com.easybilling.service;

import com.easybilling.entity.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Service responsible for provisioning tenant infrastructure.
 * Creates database schemas and runs migrations for new tenants.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantProvisioningService {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    
    @Value("${spring.datasource.username}")
    private String datasourceUsername;
    
    @Value("${spring.datasource.password}")
    private String datasourcePassword;
    
    /**
     * Provision tenant infrastructure (schema creation and migrations).
     * For modular monolith, we use shared database with tenant_id filtering.
     * Database/schema creation is optional and can be skipped.
     */
    public void provisionTenant(Tenant tenant) {
        log.info("Provisioning tenant infrastructure for: {}", tenant.getSlug());
        
        try {
            // For modular monolith with shared database, we can skip schema creation
            // All tables are in the same database, filtered by tenant_id
            // If you need separate schemas/databases per tenant, uncomment below:
            
            /*
            // Create schema for tenant
            String schemaName = "tenant_" + tenant.getSlug();
            createSchema(schemaName);
            
            // Update tenant with schema name
            tenant.setSchemaName(schemaName);
            
            // Run Flyway migrations for tenant schema
            runMigrations(schemaName);
            */
            
            // For shared database approach, just set schema name to main database
            tenant.setSchemaName("public"); // or the main database name
            
            log.info("Tenant infrastructure provisioned successfully for: {} (using shared database)", tenant.getSlug());
        } catch (Exception e) {
            log.error("Failed to provision tenant infrastructure for: {}", tenant.getSlug(), e);
            throw new RuntimeException("Failed to provision tenant infrastructure", e);
        }
    }
    
    /**
     * Create database schema for tenant.
     */
    private void createSchema(String schemaName) {
        log.info("Creating schema/database: {}", schemaName);
        
        try {
            // MySQL: CREATE DATABASE is equivalent to CREATE SCHEMA
            // Using CREATE DATABASE for MySQL compatibility
            jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS " + schemaName);
            log.info("Schema/database created successfully: {}", schemaName);
        } catch (Exception e) {
            log.error("Failed to create schema/database: {}", schemaName, e);
            throw e;
        }
    }
    
    /**
     * Run Flyway migrations for tenant schema.
     */
    private void runMigrations(String schemaName) {
        log.info("Running migrations for schema: {}", schemaName);
        
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(datasourceUrl, datasourceUsername, datasourcePassword)
                    .schemas(schemaName)
                    .locations("classpath:db/migration/tenant")
                    .baselineOnMigrate(true)
                    .load();
            
            flyway.migrate();
            log.info("Migrations completed successfully for schema: {}", schemaName);
        } catch (Exception e) {
            log.error("Failed to run migrations for schema: {}", schemaName, e);
            throw e;
        }
    }
    
    /**
     * Cleanup tenant infrastructure (for testing or tenant deletion).
     */
    public void deprovisionTenant(String schemaName) {
        log.warn("Deprovisioning tenant schema/database: {}", schemaName);
        
        try {
            // MySQL: DROP DATABASE is equivalent to DROP SCHEMA
            // CASCADE is not needed in MySQL for DROP DATABASE
            jdbcTemplate.execute("DROP DATABASE IF EXISTS " + schemaName);
            log.info("Schema/database dropped successfully: {}", schemaName);
        } catch (Exception e) {
            log.error("Failed to drop schema/database: {}", schemaName, e);
            throw e;
        }
    }
}
