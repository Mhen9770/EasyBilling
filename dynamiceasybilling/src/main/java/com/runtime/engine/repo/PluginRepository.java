package com.runtime.engine.repo;

import com.runtime.engine.plugin.PluginDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PluginRepository extends JpaRepository<PluginDefinition, Long> {
    
    List<PluginDefinition> findByTriggerAndTenantId(String trigger, String tenantId);
    
    List<PluginDefinition> findByTriggerAndTenantIdAndActive(String trigger, String tenantId, Boolean active);
    
    List<PluginDefinition> findByPluginTypeAndTenantId(String pluginType, String tenantId);
    
    List<PluginDefinition> findByTenantId(String tenantId);
}
