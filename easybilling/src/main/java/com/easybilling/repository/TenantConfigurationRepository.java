package com.easybilling.repository;

import com.easybilling.entity.TenantConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantConfigurationRepository extends JpaRepository<TenantConfiguration, Long> {
    
    Optional<TenantConfiguration> findByTenantIdAndConfigKey(Integer tenantId, String configKey);
    
    List<TenantConfiguration> findByTenantId(Integer tenantId);
    
    List<TenantConfiguration> findByTenantIdAndCategory(Integer tenantId, String category);
    
    boolean existsByTenantIdAndConfigKey(Integer tenantId, String configKey);
    
    void deleteByTenantIdAndConfigKey(Integer tenantId, String configKey);
}
