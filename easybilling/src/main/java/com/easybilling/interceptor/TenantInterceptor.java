package com.easybilling.interceptor;

import com.easybilling.context.TenantContext;
import com.easybilling.resolver.TenantResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Comparator;
import java.util.List;

/**
 * Interceptor to resolve and set tenant context for each request.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {
    
    private final List<TenantResolver> tenantResolvers;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Sort resolvers by priority
        List<TenantResolver> sortedResolvers = tenantResolvers.stream()
                .sorted(Comparator.comparingInt(TenantResolver::getPriority))
                .toList();
        
        // Try each resolver until one returns a tenant ID
        for (TenantResolver resolver : sortedResolvers) {
            String tenantIdStr = resolver.resolveTenantId(request);
            if (tenantIdStr != null && !tenantIdStr.isBlank()) {
                try {
                    Integer tenantId = Integer.parseInt(tenantIdStr);
                    TenantContext.setTenantId(tenantId);
                    log.debug("Tenant resolved: {} by {}", tenantId, resolver.getClass().getSimpleName());
                    return true;
                } catch (NumberFormatException e) {
                    log.warn("Invalid tenant ID format: {}", tenantIdStr);
                }
            }
        }
        
        log.warn("No tenant ID found in request: {} {}", request.getMethod(), request.getRequestURI());
        // For public endpoints, allow request without tenant
        // For protected endpoints, this should be handled by security layer
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        // Clear tenant context after request completion
        TenantContext.clear();
    }
}
