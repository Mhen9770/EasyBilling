package com.easybilling.service;

import com.easybilling.dto.CustomFieldDTO;
import com.easybilling.entity.CustomField;
import com.easybilling.entity.CustomFieldValue;
import com.easybilling.exception.ResourceNotFoundException;
import com.easybilling.repository.CustomFieldRepository;
import com.easybilling.repository.CustomFieldValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing custom fields and their values.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomFieldService {
    
    private final CustomFieldRepository customFieldRepository;
    private final CustomFieldValueRepository customFieldValueRepository;
    
    @Transactional(readOnly = true)
    public List<CustomFieldDTO> getCustomFieldsByEntity(Integer tenantId, String entityType) {
        return customFieldRepository.findByTenantIdAndEntityTypeAndIsActiveTrue(tenantId, entityType)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CustomFieldDTO> getAllCustomFields(Integer tenantId) {
        return customFieldRepository.findByTenantId(tenantId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CustomFieldDTO createCustomField(CustomFieldDTO dto) {
        if (customFieldRepository.existsByTenantIdAndEntityTypeAndFieldName(
                dto.getTenantId(), dto.getEntityType(), dto.getFieldName())) {
            throw new IllegalArgumentException("Custom field already exists: " + dto.getFieldName());
        }
        
        CustomField field = CustomField.builder()
                .tenantId(dto.getTenantId())
                .entityType(dto.getEntityType())
                .fieldName(dto.getFieldName())
                .fieldLabel(dto.getFieldLabel())
                .fieldType(dto.getFieldType())
                .fieldDescription(dto.getFieldDescription())
                .defaultValue(dto.getDefaultValue())
                .dropdownOptions(dto.getDropdownOptions())
                .validationRules(dto.getValidationRules())
                .isRequired(dto.getIsRequired() != null ? dto.getIsRequired() : false)
                .isSearchable(dto.getIsSearchable() != null ? dto.getIsSearchable() : false)
                .isDisplayedInList(dto.getIsDisplayedInList() != null ? dto.getIsDisplayedInList() : false)
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .placeholderText(dto.getPlaceholderText())
                .helpText(dto.getHelpText())
                .createdBy(dto.getCreatedBy())
                .build();
        
        field = customFieldRepository.save(field);
        log.info("Created custom field: {} for entity: {} in tenant: {}", 
                field.getFieldName(), field.getEntityType(), field.getTenantId());
        return toDTO(field);
    }
    
    @Transactional
    public CustomFieldDTO updateCustomField(Long id, CustomFieldDTO dto) {
        CustomField field = customFieldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Custom field not found: " + id));
        
        field.setFieldLabel(dto.getFieldLabel());
        field.setFieldDescription(dto.getFieldDescription());
        field.setDefaultValue(dto.getDefaultValue());
        field.setDropdownOptions(dto.getDropdownOptions());
        field.setValidationRules(dto.getValidationRules());
        field.setIsRequired(dto.getIsRequired());
        field.setIsSearchable(dto.getIsSearchable());
        field.setIsDisplayedInList(dto.getIsDisplayedInList());
        field.setDisplayOrder(dto.getDisplayOrder());
        field.setIsActive(dto.getIsActive());
        field.setPlaceholderText(dto.getPlaceholderText());
        field.setHelpText(dto.getHelpText());
        
        field = customFieldRepository.save(field);
        log.info("Updated custom field: {}", id);
        return toDTO(field);
    }
    
    @Transactional
    public void deleteCustomField(Long id) {
        CustomField field = customFieldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Custom field not found: " + id));
        
        // Delete all values associated with this field
        customFieldValueRepository.deleteByCustomFieldId(id);
        customFieldRepository.delete(field);
        log.info("Deleted custom field: {}", id);
    }
    
    // Custom Field Value Management
    
    @Transactional
    public void saveCustomFieldValues(Integer tenantId, String entityType, Long entityId, Map<Long, String> fieldValues) {
        for (Map.Entry<Long, String> entry : fieldValues.entrySet()) {
            Long customFieldId = entry.getKey();
            String value = entry.getValue();
            
            CustomFieldValue fieldValue = customFieldValueRepository
                    .findByTenantIdAndCustomFieldIdAndEntityId(tenantId, customFieldId, entityId)
                    .orElse(CustomFieldValue.builder()
                            .tenantId(tenantId)
                            .customFieldId(customFieldId)
                            .entityType(entityType)
                            .entityId(entityId)
                            .build());
            
            fieldValue.setFieldValue(value);
            customFieldValueRepository.save(fieldValue);
        }
        log.info("Saved custom field values for entity: {} with ID: {}", entityType, entityId);
    }
    
    @Transactional(readOnly = true)
    public Map<Long, String> getCustomFieldValues(Integer tenantId, String entityType, Long entityId) {
        List<CustomFieldValue> values = customFieldValueRepository
                .findByTenantIdAndEntityTypeAndEntityId(tenantId, entityType, entityId);
        
        Map<Long, String> fieldValues = new HashMap<>();
        for (CustomFieldValue value : values) {
            fieldValues.put(value.getCustomFieldId(), value.getFieldValue());
        }
        return fieldValues;
    }
    
    @Transactional
    public void deleteCustomFieldValues(Integer tenantId, String entityType, Long entityId) {
        customFieldValueRepository.deleteByTenantIdAndEntityTypeAndEntityId(tenantId, entityType, entityId);
        log.info("Deleted custom field values for entity: {} with ID: {}", entityType, entityId);
    }
    
    private CustomFieldDTO toDTO(CustomField entity) {
        return CustomFieldDTO.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .entityType(entity.getEntityType())
                .fieldName(entity.getFieldName())
                .fieldLabel(entity.getFieldLabel())
                .fieldType(entity.getFieldType())
                .fieldDescription(entity.getFieldDescription())
                .defaultValue(entity.getDefaultValue())
                .dropdownOptions(entity.getDropdownOptions())
                .validationRules(entity.getValidationRules())
                .isRequired(entity.getIsRequired())
                .isSearchable(entity.getIsSearchable())
                .isDisplayedInList(entity.getIsDisplayedInList())
                .displayOrder(entity.getDisplayOrder())
                .isActive(entity.getIsActive())
                .placeholderText(entity.getPlaceholderText())
                .helpText(entity.getHelpText())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }
}
