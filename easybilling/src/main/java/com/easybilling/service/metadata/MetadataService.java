package com.easybilling.service.metadata;

import com.easybilling.entity.metadata.FieldMetadata;
import com.easybilling.entity.metadata.FormMetadata;
import com.easybilling.exception.ResourceNotFoundException;
import com.easybilling.repository.metadata.FieldMetadataRepository;
import com.easybilling.repository.metadata.FormMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing form and field metadata.
 * Provides caching for performance optimization.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetadataService {
    
    private final FormMetadataRepository formMetadataRepository;
    private final FieldMetadataRepository fieldMetadataRepository;
    
    /**
     * Get form metadata by entity type and form name.
     * Result is cached per tenant for performance.
     */
    @Cacheable(value = "formMetadata", key = "#tenantId + '_' + #entityType + '_' + #formName")
    @Transactional(readOnly = true)
    public FormMetadata getFormMetadata(Integer tenantId, String entityType, String formName) {
        log.debug("Loading form metadata for tenant={}, entity={}, form={}", 
                  tenantId, entityType, formName);
        return formMetadataRepository.findByTenantIdAndEntityTypeAndFormName(
            tenantId, entityType, formName
        ).orElseThrow(() -> new ResourceNotFoundException(
            String.format("Form metadata not found for: %s/%s", entityType, formName)
        ));
    }
    
    /**
     * Get all field metadata for a specific entity type as a map.
     * Returns map of fieldName -> FieldMetadata for easy lookup.
     */
    @Cacheable(value = "fieldMetadataMap", key = "#tenantId + '_' + #entityType")
    @Transactional(readOnly = true)
    public Map<String, FieldMetadata> getFieldMetadataMap(Integer tenantId, String entityType) {
        log.debug("Loading field metadata map for tenant={}, entity={}", tenantId, entityType);
        List<FieldMetadata> fields = fieldMetadataRepository.findByTenantIdAndEntityType(
            tenantId, entityType
        );
        return fields.stream()
            .collect(Collectors.toMap(FieldMetadata::getFieldName, f -> f));
    }
    
    /**
     * Get all fields for a specific form, sorted by display order.
     */
    @Cacheable(value = "formFields", key = "#tenantId + '_' + #formId")
    @Transactional(readOnly = true)
    public List<FieldMetadata> getFormFields(Integer tenantId, String formId) {
        log.debug("Loading form fields for tenant={}, formId={}", tenantId, formId);
        return fieldMetadataRepository.findByTenantIdAndFormIdOrderByDisplayOrder(
            tenantId, formId
        );
    }
    
    /**
     * Get all forms for a tenant.
     */
    @Cacheable(value = "tenantForms", key = "#tenantId")
    @Transactional(readOnly = true)
    public List<FormMetadata> getTenantForms(Integer tenantId) {
        log.debug("Loading all forms for tenant={}", tenantId);
        return formMetadataRepository.findByTenantIdAndIsActive(tenantId, true);
    }
    
    /**
     * Get all forms for a specific entity type.
     */
    @Cacheable(value = "entityForms", key = "#tenantId + '_' + #entityType")
    @Transactional(readOnly = true)
    public List<FormMetadata> getEntityForms(Integer tenantId, String entityType) {
        log.debug("Loading forms for tenant={}, entity={}", tenantId, entityType);
        return formMetadataRepository.findByTenantIdAndEntityType(tenantId, entityType);
    }
    
    /**
     * Create or update form metadata.
     * Evicts cache after save.
     */
    @Transactional
    @CacheEvict(value = {"formMetadata", "entityForms", "tenantForms"}, allEntries = true)
    public FormMetadata saveFormMetadata(FormMetadata formMetadata) {
        log.info("Saving form metadata: {}", formMetadata.getFormName());
        FormMetadata saved = formMetadataRepository.save(formMetadata);
        log.info("Form metadata saved with id: {}", saved.getId());
        return saved;
    }
    
    /**
     * Create or update field metadata.
     * Evicts cache after save.
     */
    @Transactional
    @CacheEvict(value = {"fieldMetadataMap", "formFields"}, allEntries = true)
    public FieldMetadata saveFieldMetadata(FieldMetadata fieldMetadata) {
        log.info("Saving field metadata: {}", fieldMetadata.getFieldName());
        FieldMetadata saved = fieldMetadataRepository.save(fieldMetadata);
        log.info("Field metadata saved with id: {}", saved.getId());
        return saved;
    }
    
    /**
     * Delete form metadata.
     */
    @Transactional
    @CacheEvict(value = {"formMetadata", "entityForms", "tenantForms", "formFields"}, allEntries = true)
    public void deleteFormMetadata(String formId) {
        log.info("Deleting form metadata: {}", formId);
        formMetadataRepository.deleteById(formId);
    }
    
    /**
     * Delete field metadata.
     */
    @Transactional
    @CacheEvict(value = {"fieldMetadataMap", "formFields"}, allEntries = true)
    public void deleteFieldMetadata(String fieldId) {
        log.info("Deleting field metadata: {}", fieldId);
        fieldMetadataRepository.deleteById(fieldId);
    }
    
    /**
     * Evict all metadata caches for a tenant.
     * Useful when bulk updates are made.
     */
    @CacheEvict(value = {
        "formMetadata", "fieldMetadataMap", "formFields", 
        "entityForms", "tenantForms"
    }, allEntries = true)
    public void evictAllMetadataCache() {
        log.info("Evicting all metadata caches");
    }
}
