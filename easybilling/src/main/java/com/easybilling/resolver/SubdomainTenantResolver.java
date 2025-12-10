package com.easybilling.resolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Resolves tenant ID from subdomain (e.g., tenant1.easybilling.com).
 */
@Slf4j
@Component
public class SubdomainTenantResolver implements TenantResolver {
    
    @Value("${app.multi-tenancy.domain:easybilling.com}")
    private String baseDomain;
    
    @Override
    public String resolveTenantId(HttpServletRequest request) {
        String serverName = request.getServerName();
        
        if (serverName.contains(baseDomain)) {
            // Extract subdomain
            String subdomain = serverName.replace("." + baseDomain, "");
            
            // Ignore 'www' subdomain
            if (!subdomain.equals("www") && !subdomain.equals(baseDomain)) {
                log.debug("Resolved tenant from subdomain: {}", subdomain);
                return subdomain;
            }
        }
        
        return null;
    }
    
    @Override
    public int getPriority() {
        return 20; // Medium priority
    }
}
