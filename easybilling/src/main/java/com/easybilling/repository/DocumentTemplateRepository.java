package com.easybilling.repository;

import com.easybilling.entity.DocumentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {
    
    List<DocumentTemplate> findByTenantIdAndTemplateType(Integer tenantId, String templateType);
    
    Optional<DocumentTemplate> findByTenantIdAndTemplateTypeAndIsDefaultTrue(Integer tenantId, String templateType);
    
    List<DocumentTemplate> findByTenantIdAndIsActiveTrue(Integer tenantId);
    
    List<DocumentTemplate> findByTenantId(Integer tenantId);
}
