package com.easybilling.controller;

import com.easybilling.context.TenantContext;
import com.easybilling.dto.DocumentTemplateDTO;
import com.easybilling.service.DocumentTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing document templates.
 */
@RestController
@RequestMapping("/api/v1/customization/templates")
@RequiredArgsConstructor
@Tag(name = "Document Templates", description = "APIs for managing document templates")
public class DocumentTemplateController {
    
    private final DocumentTemplateService documentTemplateService;
    
    @GetMapping
    @Operation(summary = "Get all templates for current tenant")
    public ResponseEntity<List<DocumentTemplateDTO>> getAllTemplates() {
        Integer tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(documentTemplateService.getAllTemplates(tenantId));
    }
    
    @GetMapping("/type/{templateType}")
    @Operation(summary = "Get templates by type")
    public ResponseEntity<List<DocumentTemplateDTO>> getTemplatesByType(@PathVariable String templateType) {
        Integer tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(documentTemplateService.getTemplatesByType(tenantId, templateType));
    }
    
    @GetMapping("/type/{templateType}/default")
    @Operation(summary = "Get default template for a type")
    public ResponseEntity<DocumentTemplateDTO> getDefaultTemplate(@PathVariable String templateType) {
        Integer tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(documentTemplateService.getDefaultTemplate(tenantId, templateType));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get template by ID")
    public ResponseEntity<DocumentTemplateDTO> getTemplateById(@PathVariable Long id) {
        Integer tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(documentTemplateService.getTemplateById(id, tenantId));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Create new template")
    public ResponseEntity<DocumentTemplateDTO> createTemplate(@RequestBody DocumentTemplateDTO dto) {
        Integer tenantId = TenantContext.getTenantId();
        dto.setTenantId(tenantId);
        return ResponseEntity.ok(documentTemplateService.createTemplate(dto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Update template")
    public ResponseEntity<DocumentTemplateDTO> updateTemplate(
            @PathVariable Long id,
            @RequestBody DocumentTemplateDTO dto) {
        return ResponseEntity.ok(documentTemplateService.updateTemplate(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Delete template")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        Integer tenantId = TenantContext.getTenantId();
        documentTemplateService.deleteTemplate(id, tenantId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/set-default")
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Set template as default")
    public ResponseEntity<Void> setDefaultTemplate(@PathVariable Long id) {
        Integer tenantId = TenantContext.getTenantId();
        documentTemplateService.setDefaultTemplate(id, tenantId);
        return ResponseEntity.ok().build();
    }
}
