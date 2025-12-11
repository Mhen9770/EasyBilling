package com.easybilling.repository.workflow;

import com.easybilling.entity.workflow.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for WorkflowDefinition entity.
 */
@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, String> {
    
    /**
     * Find workflow by code.
     */
    Optional<WorkflowDefinition> findByTenantIdAndWorkflowCode(Integer tenantId, String workflowCode);
    
    /**
     * Find all workflows of a specific type.
     */
    List<WorkflowDefinition> findByTenantIdAndWorkflowType(Integer tenantId, String workflowType);
    
    /**
     * Find all active workflows.
     */
    List<WorkflowDefinition> findByTenantIdAndIsActive(Integer tenantId, Boolean isActive);
}
