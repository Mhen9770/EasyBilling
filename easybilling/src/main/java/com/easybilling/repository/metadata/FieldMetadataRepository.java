package com.easybilling.repository.metadata;

import com.easybilling.entity.metadata.FieldMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for FieldMetadata entity.
 */
@Repository
public interface FieldMetadataRepository extends JpaRepository<FieldMetadata, String> {
    
    /**
     * Find field metadata by tenant and entity type.
     */
    List<FieldMetadata> findByTenantIdAndEntityType(Integer tenantId, String entityType);
    
    /**
     * Find all fields for a specific form, ordered by display order.
     */
    List<FieldMetadata> findByTenantIdAndFormIdOrderByDisplayOrder(
        Integer tenantId, String formId
    );
    
    /**
     * Find a specific field by entity type and field name.
     */
    Optional<FieldMetadata> findByTenantIdAndEntityTypeAndFieldName(
        Integer tenantId, String entityType, String fieldName
    );
    
    /**
     * Find all searchable fields for an entity.
     */
    List<FieldMetadata> findByTenantIdAndEntityTypeAndIsSearchable(
        Integer tenantId, String entityType, Boolean isSearchable
    );
    
    /**
     * Find all visible fields for an entity.
     */
    List<FieldMetadata> findByTenantIdAndEntityTypeAndIsVisible(
        Integer tenantId, String entityType, Boolean isVisible
    );
}
