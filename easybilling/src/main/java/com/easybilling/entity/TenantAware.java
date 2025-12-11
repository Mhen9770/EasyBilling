package com.easybilling.entity;

/**
 * Interface to mark entities as tenant-aware.
 * Entities implementing this interface will have tenant isolation applied automatically.
 */
public interface TenantAware {
    Integer getTenantId();
    void setTenantId(Integer tenantId);
}
