package com.easybilling.repository.plugin;

import com.easybilling.entity.plugin.Plugin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Plugin entity.
 */
@Repository
public interface PluginRepository extends JpaRepository<Plugin, String> {
    
    /**
     * Find plugin by code.
     */
    Optional<Plugin> findByTenantIdAndPluginCode(Integer tenantId, String pluginCode);
    
    /**
     * Find all enabled plugins for a tenant.
     */
    List<Plugin> findByTenantIdAndIsEnabled(Integer tenantId, Boolean isEnabled);
    
    /**
     * Find all plugins of a specific type.
     */
    List<Plugin> findByTenantIdAndPluginType(Integer tenantId, String pluginType);
    
    /**
     * Find all plugins in a category.
     */
    List<Plugin> findByTenantIdAndCategory(Integer tenantId, String category);
    
    /**
     * Find all system plugins.
     */
    List<Plugin> findByIsSystemPlugin(Boolean isSystemPlugin);
}
