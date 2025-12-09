package com.easybilling.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for API Gateway Service.
 */
@SpringBootApplication(scanBasePackages = {
        "com.easybilling.gateway",
        "com.easybilling.common",
        "com.easybilling.security"
})
public class GatewayServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
