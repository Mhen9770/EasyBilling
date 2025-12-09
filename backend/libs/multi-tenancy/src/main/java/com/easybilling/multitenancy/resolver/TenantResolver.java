package com.easybilling.multitenancy.resolver;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Interface for resolving tenant ID from HTTP requests.
 * Implementations can resolve from subdomain, header, or token.
 */
public interface TenantResolver {
    
    /**
     * Resolve tenant ID from the request.
     * 
     * @param request HTTP request
     * @return tenant ID or null if not found
     */
    String resolveTenantId(HttpServletRequest request);
    
    /**
     * Priority of this resolver. Lower values have higher priority.
     * 
     * @return priority value
     */
    default int getPriority() {
        return 100;
    }
}
