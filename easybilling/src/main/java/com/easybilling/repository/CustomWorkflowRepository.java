package com.easybilling.repository;

import com.easybilling.entity.CustomWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomWorkflowRepository extends JpaRepository<CustomWorkflow, Long> {
    
    List<CustomWorkflow> findByTenantIdAndTriggerEventAndIsActiveTrue(Integer tenantId, String triggerEvent);
    
    List<CustomWorkflow> findByTenantIdAndIsActiveTrue(Integer tenantId);
    
    List<CustomWorkflow> findByTenantId(Integer tenantId);
}
