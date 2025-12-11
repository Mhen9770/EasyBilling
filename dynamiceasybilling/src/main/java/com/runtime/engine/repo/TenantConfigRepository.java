package com.runtime.engine.repo;

import com.runtime.engine.tenant.TenantConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantConfigRepository extends JpaRepository<TenantConfig, Long> {
    
    Optional<TenantConfig> findByTenantId(String tenantId);
    
    List<TenantConfig> findByActive(Boolean active);
    
    boolean existsByTenantId(String tenantId);
}
