package com.easybilling.repository.metadata;

import com.easybilling.entity.metadata.FormMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for FormMetadata entity.
 */
@Repository
public interface FormMetadataRepository extends JpaRepository<FormMetadata, String> {
    
    /**
     * Find form metadata by tenant, entity type, and form name.
     */
    Optional<FormMetadata> findByTenantIdAndEntityTypeAndFormName(
        Integer tenantId, String entityType, String formName
    );
    
    /**
     * Find all forms for a specific entity type.
     */
    List<FormMetadata> findByTenantIdAndEntityType(Integer tenantId, String entityType);
    
    /**
     * Find all active forms for a tenant.
     */
    List<FormMetadata> findByTenantIdAndIsActive(Integer tenantId, Boolean isActive);
    
    /**
     * Find all forms in a category.
     */
    List<FormMetadata> findByTenantIdAndCategory(Integer tenantId, String category);
}
