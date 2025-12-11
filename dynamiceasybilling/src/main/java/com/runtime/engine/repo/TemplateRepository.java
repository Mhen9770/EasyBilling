package com.runtime.engine.repo;

import com.runtime.engine.template.TemplateDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<TemplateDefinition, Long> {
    
    Optional<TemplateDefinition> findByName(String name);
    
    List<TemplateDefinition> findByTemplateTypeAndTenantId(String templateType, String tenantId);
    
    List<TemplateDefinition> findByTenantId(String tenantId);
    
    List<TemplateDefinition> findByTenantIdAndActive(String tenantId, Boolean active);
}
