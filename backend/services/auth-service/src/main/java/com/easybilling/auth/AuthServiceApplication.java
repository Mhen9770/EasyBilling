package com.easybilling.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for Auth Service.
 */
@SpringBootApplication(scanBasePackages = {
        "com.easybilling.auth",
        "com.easybilling.common",
        "com.easybilling.multitenancy",
        "com.easybilling.security"
})
@EnableJpaRepositories
@EnableCaching
public class AuthServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
