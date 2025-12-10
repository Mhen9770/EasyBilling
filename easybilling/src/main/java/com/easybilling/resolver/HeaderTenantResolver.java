package com.easybilling.resolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves tenant ID from HTTP header (X-Tenant-Id).
 */
@Slf4j
@Component
public class HeaderTenantResolver implements TenantResolver {
    
    private static final String TENANT_HEADER = "X-Tenant-Id";
    
    @Override
    public String resolveTenantId(HttpServletRequest request) {
        String tenantId = request.getHeader(TENANT_HEADER);
        if (tenantId != null && !tenantId.isBlank()) {
            log.debug("Resolved tenant from header: {}", tenantId);
            return tenantId;
        }
        return null;
    }
    
    @Override
    public int getPriority() {
        return 10; // High priority
    }
}
