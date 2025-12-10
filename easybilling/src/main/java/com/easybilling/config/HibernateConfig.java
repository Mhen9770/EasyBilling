package com.easybilling.config;

import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Hibernate configuration for multi-tenancy support.
 * Configures schema-per-tenant strategy.
 */
@Configuration
@RequiredArgsConstructor
public class HibernateConfig {
    
    private final DataSource dataSource;
    private final JpaProperties jpaProperties;
    private final MultiTenantConnectionProvider multiTenantConnectionProvider;
    private final CurrentTenantIdentifierResolver currentTenantIdentifierResolver;
    
    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }
    
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.easybilling.entity");
        em.setJpaVendorAdapter(jpaVendorAdapter());
        
        Map<String, Object> jpaPropertiesMap = new HashMap<>(jpaProperties.getProperties());
        
        // Multi-tenancy configuration
        jpaPropertiesMap.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);
        jpaPropertiesMap.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
        
        // Disable schema validation and auto-update in multi-tenant mode
        // Schemas are managed via Flyway migrations
        jpaPropertiesMap.put(AvailableSettings.HBM2DDL_AUTO, "none");
        
        em.setJpaPropertyMap(jpaPropertiesMap);
        
        return em;
    }
}
