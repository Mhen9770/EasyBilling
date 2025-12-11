package com.easybilling.service;

import com.easybilling.entity.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    
    /**
     * Initialize master data for a tenant schema.
     * This is called after schema creation.
     */
    public void initializeMasterData(Tenant tenant) {
        String schemaName = tenant.getSchemaName();
        Integer tenantId = tenant.getId(); // Use tenant ID for tenantId field
        log.info("Initializing master data for tenant schema: {}", schemaName);
        
        // Validate schema name to prevent SQL injection
        if (!schemaName.matches("^tenant_[a-z0-9_-]+$")) {
            throw new IllegalArgumentException("Invalid schema name: " + schemaName);
        }
        
        try (Connection connection = dataSource.getConnection()) {
            // Switch to tenant schema
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("USE `" + schemaName + "`");
                
                // Insert default categories
                insertDefaultCategories(connection, tenantId);
                
                // Insert default brands
                insertDefaultBrands(connection, tenantId);
                
                // Switch back to master schema
                stmt.execute("USE `easybilling`");
            }
            
            log.info("Master data initialization completed for tenant: {}", tenant.getSlug());
        } catch (Exception e) {
            log.error("Failed to initialize master data for tenant: {}", tenant.getSlug(), e);
            throw new RuntimeException("Failed to initialize master data", e);
        }
    }
    
    /**
     * Insert default categories.
     */
    private void insertDefaultCategories(Connection connection, String tenantId) throws Exception {
        log.debug("Inserting default categories");
        
        String insertSQL = "INSERT INTO categories (id, name, description, parent_id, is_active, tenant_id, created_at, updated_at) " +
                          "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
                          "ON DUPLICATE KEY UPDATE name = name"; // Prevent duplicate insertion
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            // Main categories
            insertCategory(pstmt, 1L, "Electronics", "Electronic devices and accessories", null, tenantId);
            insertCategory(pstmt, 2L, "Clothing", "Apparel and fashion items", null, tenantId);
            insertCategory(pstmt, 3L, "Food & Beverages", "Food items and drinks", null, tenantId);
            insertCategory(pstmt, 4L, "Home & Kitchen", "Home appliances and kitchen items", null, tenantId);
            insertCategory(pstmt, 5L, "Health & Beauty", "Health care and beauty products", null, tenantId);
            insertCategory(pstmt, 6L, "Sports & Outdoors", "Sports equipment and outdoor gear", null, tenantId);
            insertCategory(pstmt, 7L, "Books & Stationery", "Books, office supplies, and stationery", null, tenantId);
            insertCategory(pstmt, 8L, "Toys & Games", "Toys, games, and entertainment", null, tenantId);
            
            // Sub-categories for Electronics
            insertCategory(pstmt, 101L, "Mobile Phones", "Smartphones and feature phones", 1L, tenantId);
            insertCategory(pstmt, 102L, "Laptops & Computers", "Laptops, desktops, and accessories", 1L, tenantId);
            insertCategory(pstmt, 103L, "Audio & Video", "Headphones, speakers, cameras", 1L, tenantId);
            insertCategory(pstmt, 104L, "Smart Devices", "Smartwatches, fitness trackers", 1L, tenantId);
            
            // Sub-categories for Clothing
            insertCategory(pstmt, 201L, "Men's Clothing", "Men's apparel", 2L, tenantId);
            insertCategory(pstmt, 202L, "Women's Clothing", "Women's apparel", 2L, tenantId);
            insertCategory(pstmt, 203L, "Kids Clothing", "Children's apparel", 2L, tenantId);
            insertCategory(pstmt, 204L, "Footwear", "Shoes and sandals", 2L, tenantId);
            
            // Sub-categories for Food & Beverages
            insertCategory(pstmt, 301L, "Snacks", "Chips, biscuits, and snacks", 3L, tenantId);
            insertCategory(pstmt, 302L, "Beverages", "Soft drinks, juices, water", 3L, tenantId);
            insertCategory(pstmt, 303L, "Groceries", "Daily grocery items", 3L, tenantId);
            insertCategory(pstmt, 304L, "Dairy Products", "Milk, cheese, yogurt", 3L, tenantId);
        }
        
        log.debug("Default categories inserted successfully");
    }
    
    /**
     * Helper method to insert a single category.
     */
    private void insertCategory(PreparedStatement pstmt, Long id, String name, String description, Long parentId, String tenantId) throws Exception {
        pstmt.setLong(1, id);
        pstmt.setString(2, name);
        pstmt.setString(3, description);
        if (parentId != null) {
            pstmt.setLong(4, parentId);
        } else {
            pstmt.setNull(4, java.sql.Types.BIGINT);
        }
        pstmt.setBoolean(5, true);
        pstmt.setString(6, tenantId);
        pstmt.executeUpdate();
    }
    
    /**
     * Insert default brands.
     */
    private void insertDefaultBrands(Connection connection, String tenantId) throws Exception {
        log.debug("Inserting default brands");
        
        String insertSQL = "INSERT INTO brands (id, name, description, is_active, tenant_id, created_at, updated_at) " +
                          "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
                          "ON DUPLICATE KEY UPDATE name = name"; // Prevent duplicate insertion
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            insertBrand(pstmt, 1L, "Generic", "Generic/Unbranded products", tenantId);
            insertBrand(pstmt, 2L, "House Brand", "Store's own brand", tenantId);
        }
        
        log.debug("Default brands inserted successfully");
    }
    
    /**
     * Helper method to insert a single brand.
     */
    private void insertBrand(PreparedStatement pstmt, Long id, String name, String description, String tenantId) throws Exception {
        pstmt.setLong(1, id);
        pstmt.setString(2, name);
        pstmt.setString(3, description);
        pstmt.setBoolean(4, true);
        pstmt.setString(5, tenantId);
        pstmt.executeUpdate();
    }
    
    /**
     * Verify that a tenant schema has the required master data.
     */
    public boolean verifyMasterData(String schemaName) {
        log.debug("Verifying master data for schema: {}", schemaName);
        
        // Validate schema name
        if (!schemaName.matches("^tenant_[a-z0-9_-]+$")) {
            throw new IllegalArgumentException("Invalid schema name: " + schemaName);
        }
        
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            
            stmt.execute("USE `" + schemaName + "`");
            
            // Check for default categories
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM categories WHERE id <= 10");
            if (rs.next() && rs.getInt("cnt") > 0) {
                log.debug("Master data verified for schema: {}", schemaName);
                stmt.execute("USE `easybilling`");
                return true;
            }
            
            stmt.execute("USE `easybilling`");
            return false;
        } catch (Exception e) {
            log.error("Failed to verify master data for schema: {}", schemaName, e);
            return false;
        }
    }
}
