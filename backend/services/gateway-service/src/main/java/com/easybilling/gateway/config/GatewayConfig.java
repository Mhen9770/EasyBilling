package com.easybilling.gateway.config;

import com.easybilling.gateway.filter.AuthenticationFilter;
import com.easybilling.gateway.filter.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Gateway routing configuration.
 */
@Configuration
@RequiredArgsConstructor
public class GatewayConfig {
    
    private final AuthenticationFilter authenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service routes - PUBLIC endpoints
                .route("auth-public", r -> r
                        .path("/auth-service/api/v1/auth/login",
                              "/auth-service/api/v1/auth/register",
                              "/auth-service/api/v1/auth/refresh")
                        .filters(f -> f
                                .filter(rateLimitFilter.apply(new RateLimitFilter.Config()))
                                .rewritePath("/auth-service/(?<segment>.*)", "/${segment}")
                        )
                        .uri("http://localhost:8081"))
                
                // Auth Service routes - PROTECTED endpoints
                .route("auth-protected", r -> r
                        .path("/auth-service/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitFilter.apply(new RateLimitFilter.Config()))
                                .rewritePath("/auth-service/(?<segment>.*)", "/${segment}")
                        )
                        .uri("http://localhost:8081"))
                
                // Tenant Service routes - PROTECTED
                .route("tenant-service", r -> r
                        .path("/tenant-service/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitFilter.apply(new RateLimitFilter.Config()))
                                .rewritePath("/tenant-service/(?<segment>.*)", "/${segment}")
                        )
                        .uri("http://localhost:8082"))
                
                // Actuator endpoints
                .route("actuator", r -> r
                        .path("/actuator/**")
                        .uri("forward:/"))
                
                .build();
    }
    
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3001"));
        corsConfig.setMaxAge(3600L);
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        corsConfig.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
}
