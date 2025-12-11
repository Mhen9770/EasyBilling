package com.runtime.engine.repo;

import com.runtime.engine.entity.DynamicEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DynamicEntityRepository extends JpaRepository<DynamicEntity, Long> {
    
    List<DynamicEntity> findByEntityTypeAndTenantId(String entityType, String tenantId);
    
    Page<DynamicEntity> findByEntityTypeAndTenantId(String entityType, String tenantId, Pageable pageable);
    
    List<DynamicEntity> findByTenantId(String tenantId);
    
    List<DynamicEntity> findByEntityTypeAndTenantIdAndActive(String entityType, String tenantId, Boolean active);
}
