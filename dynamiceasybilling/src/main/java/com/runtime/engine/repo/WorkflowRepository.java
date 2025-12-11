package com.runtime.engine.repo;

import com.runtime.engine.workflow.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowRepository extends JpaRepository<WorkflowDefinition, Long> {
    
    List<WorkflowDefinition> findByEntityTypeAndTenantId(String entityType, String tenantId);
    
    List<WorkflowDefinition> findByEntityTypeAndTenantIdAndActive(String entityType, String tenantId, Boolean active);
    
    List<WorkflowDefinition> findByTriggerAndEntityTypeAndTenantId(String trigger, String entityType, String tenantId);
}
