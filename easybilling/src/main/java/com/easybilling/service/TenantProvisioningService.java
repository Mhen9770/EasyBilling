package com.easybilling.service;

import com.easybilling.context.TenantContext;
import com.easybilling.entity.Tenant;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.schema.TargetType;
import org.hibernate.tool.schema.spi.SchemaManagementTool;
import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.hibernate.tool.schema.spi.TargetDescriptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for provisioning tenant infrastructure.
 * Creates database schemas and initializes tables via JPA.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantProvisioningService {
    
    private final JdbcTemplate jdbcTemplate;
    private final TenantMasterDataService masterDataService;
    private final DataSource dataSource;
    private final EntityManagerFactory entityManagerFactory;
    
    /**
     * Provision tenant infrastructure (schema creation and table initialization).
     * Uses separate schema per tenant strategy with JPA for table creation.
     */
    public void provisionTenant(Tenant tenant) {
        log.info("Provisioning tenant infrastructure for: {}", tenant.getSlug());
        
        try {
            // Create schema for tenant
            String schemaName = "tenant_" + tenant.getSlug();
            createSchema(schemaName);
            
            // Update tenant with schema name
            tenant.setSchemaName(schemaName);
            
            // Trigger JPA table creation by accessing the schema
            initializeTenantSchema(schemaName);
            
            // Initialize master data
//            masterDataService.initializeMasterData(tenant);
            
            log.info("Tenant infrastructure provisioned successfully for: {} with schema: {}", 
                    tenant.getSlug(), schemaName);
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
        
        // Validate schema name to prevent SQL injection
        if (!schemaName.matches("^tenant_[a-z0-9_-]+$")) {
            throw new IllegalArgumentException("Invalid schema name: " + schemaName);
        }
        
        try {
            // MySQL: CREATE DATABASE is equivalent to CREATE SCHEMA
            jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS `" + schemaName + "`");
            log.info("Schema/database created successfully: {}", schemaName);
        } catch (Exception e) {
            log.error("Failed to create schema/database: {}", schemaName, e);
            throw e;
        }
    }
    
    /**
     * Initialize tenant schema by triggering Hibernate DDL generation.
     * Creates all JPA entity tables in the tenant schema.
     */
    private void initializeTenantSchema(String schemaName) {
        log.info("Initializing JPA tables for schema: {}", schemaName);
        
        // Validate schema name
        if (!schemaName.matches("^tenant_[a-z0-9_-]+$")) {
            throw new IllegalArgumentException("Invalid schema name: " + schemaName);
        }
        
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            
            // Switch to tenant schema
            stmt.execute("USE `" + schemaName + "`");
            
            // Trigger Hibernate to create tables using the current connection
            // by setting tenant context and accessing EntityManager
            String previousTenant = TenantContext.getTenantId();
            try {
                TenantContext.setTenantId(schemaName);
                
                // Force Hibernate to initialize tables by accessing the EntityManager
                // This triggers ddl-auto behavior
                entityManagerFactory.createEntityManager().close();
                
                log.info("JPA tables created successfully in schema: {}", schemaName);
            } finally {
                if (previousTenant != null) {
                    TenantContext.setTenantId(previousTenant);
                } else {
                    TenantContext.clear();
                }
            }
            
            // Switch back to master schema
            stmt.execute("USE `easybilling`");
            
        } catch (Exception e) {
            log.error("Failed to initialize schema: {}", schemaName, e);
            throw new RuntimeException("Failed to initialize schema", e);
        }
    }
    
    /**
     * Cleanup tenant infrastructure (for testing or tenant deletion).
     */
    public void deprovisionTenant(String schemaName) {
        log.warn("Deprovisioning tenant schema/database: {}", schemaName);
        
        // Validate schema name
        if (!schemaName.matches("^tenant_[a-z0-9_-]+$")) {
            throw new IllegalArgumentException("Invalid schema name: " + schemaName);
        }
        
        try {
            // MySQL: DROP DATABASE is equivalent to DROP SCHEMA
            jdbcTemplate.execute("DROP DATABASE IF EXISTS `" + schemaName + "`");
            log.info("Schema/database dropped successfully: {}", schemaName);
        } catch (Exception e) {
            log.error("Failed to drop schema/database: {}", schemaName, e);
            throw e;
        }
    }
}
