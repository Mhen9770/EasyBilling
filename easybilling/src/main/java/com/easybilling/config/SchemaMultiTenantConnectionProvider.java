package com.easybilling.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Hibernate multi-tenant connection provider for schema-per-tenant strategy.
 * Provides connections with the appropriate schema context.
 */
@Slf4j
@Component
public class SchemaMultiTenantConnectionProvider<T> implements MultiTenantConnectionProvider<T> {
    
    private static final long serialVersionUID = 1L;
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Connection getAnyConnection() throws SQLException {
        log.trace("Getting any connection");
        return dataSource.getConnection();
    }
    
    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        log.trace("Releasing any connection");
        connection.close();
    }
    
    @Override
    public Connection getConnection(T tenantIdentifier) throws SQLException {
        String schemaName = tenantIdentifier != null ? tenantIdentifier.toString() : "easybilling";
        log.debug("Getting connection for tenant schema: {}", schemaName);
        
        final Connection connection = getAnyConnection();
        
        try {
            // Set the schema for this connection
            // For MySQL, we use the database (schema) switching
            if (schemaName != null && !schemaName.equals("easybilling") && !schemaName.equals("public")) {
                log.debug("Switching to schema: {}", schemaName);
                connection.createStatement().execute("USE " + schemaName);
            }
        } catch (SQLException e) {
            log.error("Failed to switch to tenant schema: {}", schemaName, e);
            throw e;
        }
        
        return connection;
    }
    
    @Override
    public void releaseConnection(T tenantIdentifier, Connection connection) throws SQLException {
        String schemaName = tenantIdentifier != null ? tenantIdentifier.toString() : "easybilling";
        log.trace("Releasing connection for tenant: {}", schemaName);
        
        try {
            // Reset to default schema before releasing
            connection.createStatement().execute("USE easybilling");
        } catch (SQLException e) {
            log.warn("Failed to reset to default schema", e);
        }
        
        connection.close();
    }
    
    @Override
    public boolean supportsAggressiveRelease() {
        // Return false to indicate that we don't support aggressive connection release
        return false;
    }
    
    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }
    
    @Override
    public <U> U unwrap(Class<U> unwrapType) {
        return null;
    }
}
