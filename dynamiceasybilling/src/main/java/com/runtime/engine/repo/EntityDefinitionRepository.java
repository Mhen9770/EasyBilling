package com.runtime.engine.repo;

import com.runtime.engine.entity.EntityDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntityDefinitionRepository extends JpaRepository<EntityDefinition, Long> {
    
    Optional<EntityDefinition> findByName(String name);
    
    Optional<EntityDefinition> findByNameAndTenantId(String name, String tenantId);
    
    List<EntityDefinition> findByTenantId(String tenantId);
    
    List<EntityDefinition> findByTenantIdAndActive(String tenantId, Boolean active);
    
    boolean existsByName(String name);
}
