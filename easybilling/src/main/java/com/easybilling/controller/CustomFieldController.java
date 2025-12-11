package com.easybilling.controller;

import com.easybilling.context.TenantContext;
import com.easybilling.dto.CustomFieldDTO;
import com.easybilling.service.CustomFieldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customization/fields")
@RequiredArgsConstructor
@Tag(name = "Custom Fields", description = "APIs for managing custom fields")
public class CustomFieldController {
    
    private final CustomFieldService customFieldService;
    
    @GetMapping
    @Operation(summary = "Get all custom fields for current tenant")
    public ResponseEntity<List<CustomFieldDTO>> getAllCustomFields() {
        Integer tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(customFieldService.getAllCustomFields(tenantId));
    }
    
    @GetMapping("/entity/{entityType}")
    @Operation(summary = "Get custom fields for specific entity type")
    public ResponseEntity<List<CustomFieldDTO>> getCustomFieldsByEntity(@PathVariable String entityType) {
        Integer tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(customFieldService.getCustomFieldsByEntity(tenantId, entityType));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Create custom field")
    public ResponseEntity<CustomFieldDTO> createCustomField(@RequestBody CustomFieldDTO dto) {
        Integer tenantId = TenantContext.getTenantId();
        dto.setTenantId(tenantId);
        return ResponseEntity.ok(customFieldService.createCustomField(dto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Update custom field")
    public ResponseEntity<CustomFieldDTO> updateCustomField(@PathVariable Long id, @RequestBody CustomFieldDTO dto) {
        return ResponseEntity.ok(customFieldService.updateCustomField(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Delete custom field")
    public ResponseEntity<Void> deleteCustomField(@PathVariable Long id) {
        customFieldService.deleteCustomField(id);
        return ResponseEntity.noContent().build();
    }
    
    // Custom Field Values
    
    @GetMapping("/values/{entityType}/{entityId}")
    @Operation(summary = "Get custom field values for an entity")
    public ResponseEntity<Map<Long, String>> getCustomFieldValues(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        Integer tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(customFieldService.getCustomFieldValues(tenantId, entityType, entityId));
    }
    
    @PostMapping("/values/{entityType}/{entityId}")
    @Operation(summary = "Save custom field values for an entity")
    public ResponseEntity<Void> saveCustomFieldValues(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestBody Map<Long, String> fieldValues) {
        Integer tenantId = TenantContext.getTenantId();
        customFieldService.saveCustomFieldValues(tenantId, entityType, entityId, fieldValues);
        return ResponseEntity.ok().build();
    }
}
