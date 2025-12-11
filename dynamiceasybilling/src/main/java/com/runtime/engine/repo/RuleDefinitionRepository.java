package com.runtime.engine.repo;

import com.runtime.engine.rule.RuleDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleDefinitionRepository extends JpaRepository<RuleDefinition, Long> {
    
    List<RuleDefinition> findByEntityTypeAndTenantId(String entityType, String tenantId);
    
    List<RuleDefinition> findByEntityTypeAndTenantIdAndActive(String entityType, String tenantId, Boolean active);
    
    List<RuleDefinition> findByTriggerAndEntityTypeAndTenantId(String trigger, String entityType, String tenantId);
    
    List<RuleDefinition> findByTriggerAndEntityTypeAndTenantIdAndActive(String trigger, String entityType, String tenantId, Boolean active);
}
