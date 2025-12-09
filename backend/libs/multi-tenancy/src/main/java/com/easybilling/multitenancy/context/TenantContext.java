package com.easybilling.multitenancy.context;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-local storage for tenant context.
 * This allows tenant information to be propagated through the request lifecycle.
 */
@Slf4j
public final class TenantContext {
    
    private static final ThreadLocal<String> CURRENT_TENANT = new InheritableThreadLocal<>();
    
    private TenantContext() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static void setTenantId(String tenantId) {
        log.debug("Setting tenant context: {}", tenantId);
        CURRENT_TENANT.set(tenantId);
    }
    
    public static String getTenantId() {
        String tenantId = CURRENT_TENANT.get();
        log.trace("Getting tenant context: {}", tenantId);
        return tenantId;
    }
    
    public static void clear() {
        log.debug("Clearing tenant context");
        CURRENT_TENANT.remove();
    }
    
    public static boolean isSet() {
        return CURRENT_TENANT.get() != null;
    }
}
