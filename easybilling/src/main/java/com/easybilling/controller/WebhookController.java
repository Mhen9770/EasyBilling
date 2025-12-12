package com.easybilling.controller;

import com.easybilling.context.TenantContext;
import com.easybilling.dto.WebhookDTO;
import com.easybilling.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing webhooks.
 */
@RestController
@RequestMapping("/api/v1/customization/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "APIs for managing webhooks and external integrations")
public class WebhookController {
    
    private final WebhookService webhookService;
    
    @GetMapping
    @Operation(summary = "Get all webhooks for current tenant")
    public ResponseEntity<List<WebhookDTO>> getAllWebhooks() {
        Integer tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(webhookService.getAllWebhooks(tenantId));
    }
    
    @GetMapping("/event/{eventType}")
    @Operation(summary = "Get webhooks by event type")
    public ResponseEntity<List<WebhookDTO>> getWebhooksByEvent(@PathVariable String eventType) {
        Integer tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(webhookService.getWebhooksByEvent(tenantId, eventType));
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get all active webhooks")
    public ResponseEntity<List<WebhookDTO>> getActiveWebhooks() {
        Integer tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(webhookService.getActiveWebhooks(tenantId));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get webhook by ID")
    public ResponseEntity<WebhookDTO> getWebhookById(@PathVariable Long id) {
        Integer tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(webhookService.getWebhookById(id, tenantId));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Create new webhook")
    public ResponseEntity<WebhookDTO> createWebhook(@RequestBody WebhookDTO dto) {
        Integer tenantId = TenantContext.getTenantId();
        dto.setTenantId(tenantId);
        return ResponseEntity.ok(webhookService.createWebhook(dto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Update webhook")
    public ResponseEntity<WebhookDTO> updateWebhook(
            @PathVariable Long id,
            @RequestBody WebhookDTO dto) {
        return ResponseEntity.ok(webhookService.updateWebhook(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Delete webhook")
    public ResponseEntity<Void> deleteWebhook(@PathVariable Long id) {
        Integer tenantId = TenantContext.getTenantId();
        webhookService.deleteWebhook(id, tenantId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Toggle webhook active status")
    public ResponseEntity<Void> toggleWebhookStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        Integer tenantId = TenantContext.getTenantId();
        webhookService.toggleWebhookStatus(id, tenantId, isActive);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/test")
    @PreAuthorize("hasAuthority('MANAGE_TENANT_SETTINGS')")
    @Operation(summary = "Test webhook connection")
    public ResponseEntity<Map<String, Boolean>> testWebhook(@PathVariable Long id) {
        Integer tenantId = TenantContext.getTenantId();
        boolean success = webhookService.testWebhook(id, tenantId);
        return ResponseEntity.ok(Map.of("success", success));
    }
}
