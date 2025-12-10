package com.easybilling.context;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-local storage for user context.
 * This allows user information to be propagated through the request lifecycle.
 */
@Slf4j
public final class UserContext {
    
    private static final ThreadLocal<String> CURRENT_USER = new InheritableThreadLocal<>();
    
    private UserContext() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static void setUserId(String userId) {
        log.debug("Setting user context: {}", userId);
        CURRENT_USER.set(userId);
    }
    
    public static String getUserId() {
        String userId = CURRENT_USER.get();
        log.trace("Getting user context: {}", userId);
        return userId;
    }
    
    public static void clear() {
        log.debug("Clearing user context");
        CURRENT_USER.remove();
    }
    
    public static boolean isSet() {
        return CURRENT_USER.get() != null;
    }
}

