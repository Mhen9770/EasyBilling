package com.easybilling.controller;

import com.easybilling.context.TenantContext;
import com.easybilling.context.UserContext;
import com.easybilling.exception.UnauthorizedException;

/**
 * Base controller with helper methods for multi-tenancy.
 */
public abstract class BaseController {
    
    /**
     * Get current tenant ID from context (extracted from JWT token).
     * Throws exception if not set (should not happen for authenticated requests).
     */
    protected String getCurrentTenantId() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new UnauthorizedException("Tenant ID not found in token. Please login again.");
        }
        return tenantId;
    }
    
    /**
     * Get current user ID from context (extracted from JWT token).
     * Throws exception if not set (should not happen for authenticated requests).
     */
    protected String getCurrentUserId() {
        String userId = UserContext.getUserId();
        if (userId == null || userId.isBlank()) {
            throw new UnauthorizedException("User ID not found in token. Please login again.");
        }
        return userId;
    }
}

