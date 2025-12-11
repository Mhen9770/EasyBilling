package com.easybilling.config;

import lombok.RequiredArgsConstructor;
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
 * Hibernate configuration for same-schema multitenancy.
 * Uses tenant filtering instead of schema-per-tenant strategy.
 */
//@Configuration
@RequiredArgsConstructor
public class HibernateConfig {
    
    private final DataSource dataSource;
    private final JpaProperties jpaProperties;
    
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
        
        // No longer using schema-based multitenancy
        // Tenant isolation is now handled via Hibernate filters and tenant_id column
        jpaPropertiesMap.put("hibernate.hbm2ddl.auto", "update");
        
        em.setJpaPropertyMap(jpaPropertiesMap);
        
        return em;
    }
}
