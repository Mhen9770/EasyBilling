package com.easybilling.context;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-local storage for tenant context.
 * This allows tenant information to be propagated through the request lifecycle.
 */
@Slf4j
public final class TenantContext {
    
    private static final ThreadLocal<Integer> CURRENT_TENANT = new InheritableThreadLocal<>();
    
    private TenantContext() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static void setTenantId(Integer tenantId) {
        log.debug("Setting tenant context: {}", tenantId);
        CURRENT_TENANT.set(tenantId);
    }
    
    public static Integer getTenantId() {
        Integer tenantId = CURRENT_TENANT.get();
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
