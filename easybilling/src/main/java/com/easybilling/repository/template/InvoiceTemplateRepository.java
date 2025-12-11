package com.easybilling.repository.template;

import com.easybilling.entity.template.InvoiceTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for InvoiceTemplate entity.
 */
@Repository
public interface InvoiceTemplateRepository extends JpaRepository<InvoiceTemplate, String> {
    
    /**
     * Find template by code.
     */
    Optional<InvoiceTemplate> findByTenantIdAndTemplateCode(Integer tenantId, String templateCode);
    
    /**
     * Find default template for a type.
     */
    Optional<InvoiceTemplate> findByTenantIdAndTemplateTypeAndIsDefault(
        Integer tenantId, String templateType, Boolean isDefault
    );
    
    /**
     * Find all templates of a specific type.
     */
    List<InvoiceTemplate> findByTenantIdAndTemplateType(Integer tenantId, String templateType);
    
    /**
     * Find all active templates.
     */
    List<InvoiceTemplate> findByTenantIdAndIsActive(Integer tenantId, Boolean isActive);
}
