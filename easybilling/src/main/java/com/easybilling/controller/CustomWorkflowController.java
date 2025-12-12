package com.easybilling.controller;

import com.easybilling.context.TenantContext;
import com.easybilling.dto.CustomWorkflowDTO;
import com.easybilling.service.CustomWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing custom workflows.
 */
@RestController
@RequestMapping("/api/v1/customization/workflows")
@RequiredArgsConstructor
@Tag(name = "Custom Workflows", description = "APIs for managing business workflows")
public class CustomWorkflowController {
    
    private final CustomWorkflowService customWorkflowService;
    
    @GetMapping
    @Operation(summary = "Get all workflows for current tenant")
    public ResponseEntity<List<CustomWorkflowDTO>> getAllWorkflows() {
        Integer tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(customWorkflowService.getAllWorkflows(tenantId));
    }
    
    @GetMapping("/trigger/{triggerEvent}")
    @Operation(summary = "Get workflows by trigger event")
    public ResponseEntity<List<CustomWorkflowDTO>> getWorkflowsByTrigger(@PathVariable String triggerEvent) {
        Integer tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(customWorkflowService.getWorkflowsByTrigger(tenantId, triggerEvent));
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get all active workflows")
    public ResponseEntity<List<CustomWorkflowDTO>> getActiveWorkflows() {
        Integer tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(customWorkflowService.getActiveWorkflows(tenantId));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get workflow by ID")
    public ResponseEntity<CustomWorkflowDTO> getWorkflowById(@PathVariable Long id) {
        Integer tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(customWorkflowService.getWorkflowById(id, tenantId));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Create new workflow")
    public ResponseEntity<CustomWorkflowDTO> createWorkflow(@RequestBody CustomWorkflowDTO dto) {
        Integer tenantId = TenantContext.getTenantId();
        dto.setTenantId(tenantId);
        return ResponseEntity.ok(customWorkflowService.createWorkflow(dto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Update workflow")
    public ResponseEntity<CustomWorkflowDTO> updateWorkflow(
            @PathVariable Long id,
            @RequestBody CustomWorkflowDTO dto) {
        return ResponseEntity.ok(customWorkflowService.updateWorkflow(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Delete workflow")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable Long id) {
        Integer tenantId = TenantContext.getTenantId();
        customWorkflowService.deleteWorkflow(id, tenantId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Toggle workflow active status")
    public ResponseEntity<Void> toggleWorkflowStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        Integer tenantId = TenantContext.getTenantId();
        customWorkflowService.toggleWorkflowStatus(id, tenantId, isActive);
        return ResponseEntity.ok().build();
    }
}
