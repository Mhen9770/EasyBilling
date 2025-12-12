package com.easybilling.repository;

import com.easybilling.entity.CustomField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomFieldRepository extends JpaRepository<CustomField, Long> {
    
    List<CustomField> findByTenantIdAndEntityType(Integer tenantId, String entityType);
    
    List<CustomField> findByTenantIdAndEntityTypeAndIsActiveTrue(Integer tenantId, String entityType);
    
    List<CustomField> findByTenantId(Integer tenantId);
    
    boolean existsByTenantIdAndEntityTypeAndFieldName(Integer tenantId, String entityType, String fieldName);
}
