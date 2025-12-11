package com.easybilling.repository;

import com.easybilling.entity.CustomFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomFieldValueRepository extends JpaRepository<CustomFieldValue, Long> {
    
    List<CustomFieldValue> findByTenantIdAndEntityTypeAndEntityId(Integer tenantId, String entityType, String entityId);
    
    Optional<CustomFieldValue> findByTenantIdAndCustomFieldIdAndEntityId(Integer tenantId, Long customFieldId, String entityId);
    
    void deleteByTenantIdAndEntityTypeAndEntityId(Integer tenantId, String entityType, String entityId);
    
    void deleteByCustomFieldId(Long customFieldId);
}
