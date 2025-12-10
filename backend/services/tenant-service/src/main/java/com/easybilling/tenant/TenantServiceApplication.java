package com.easybilling.tenant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for Tenant Service.
 */
@SpringBootApplication(scanBasePackages = {
        "com.easybilling.tenant",
        "com.easybilling.common",
        "com.easybilling.multitenancy"
})
@EnableJpaRepositories
@EnableCaching
public class TenantServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TenantServiceApplication.class, args);
    }
}
