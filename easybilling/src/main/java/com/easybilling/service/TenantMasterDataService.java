package com.easybilling.service;

import com.easybilling.entity.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Service for initializing master data in tenant schemas.
 * Populates default categories, brands, and other reference data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantMasterDataService {
    
    private final DataSource dataSource;
    
    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    
    /**
     * Initialize master data for a tenant schema.
     * This is called after schema creation and migration.
     */
    public void initializeMasterData(Tenant tenant) {
        String schemaName = tenant.getSchemaName();
        log.info("Initializing master data for tenant schema: {}", schemaName);
        
        try (Connection connection = dataSource.getConnection()) {
            // Switch to tenant schema
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("USE " + schemaName);
                
                // Verify that master data was created by migrations
                // Check if categories exist
                var rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM categories WHERE id <= 10");
                if (rs.next()) {
                    int count = rs.getInt("cnt");
                    log.info("Found {} default categories in schema: {}", count, schemaName);
                    
                    if (count > 0) {
                        log.info("Master data already initialized for schema: {}", schemaName);
                    } else {
                        log.warn("Master data migration may not have run properly for schema: {}", schemaName);
                    }
                }
                
                // Switch back to default schema
                stmt.execute("USE easybilling");
            }
            
            log.info("Master data initialization completed for tenant: {}", tenant.getSlug());
        } catch (Exception e) {
            log.error("Failed to initialize master data for tenant: {}", tenant.getSlug(), e);
            throw new RuntimeException("Failed to initialize master data", e);
        }
    }
    
    /**
     * Verify that a tenant schema has the required master data.
     */
    public boolean verifyMasterData(String schemaName) {
        log.debug("Verifying master data for schema: {}", schemaName);
        
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            
            stmt.execute("USE " + schemaName);
            
            // Check for default categories
            var rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM categories WHERE id <= 10");
            if (rs.next() && rs.getInt("cnt") > 0) {
                log.debug("Master data verified for schema: {}", schemaName);
                stmt.execute("USE easybilling");
                return true;
            }
            
            stmt.execute("USE easybilling");
            return false;
        } catch (Exception e) {
            log.error("Failed to verify master data for schema: {}", schemaName, e);
            return false;
        }
    }
}
