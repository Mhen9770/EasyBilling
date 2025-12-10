package com.easybilling.controller;

import com.easybilling.dto.ApiResponse;
import com.easybilling.dto.PageResponse;
import com.easybilling.dto.TenantRequest;
import com.easybilling.dto.TenantResponse;
import com.easybilling.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for tenant management.
 */
@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant Management", description = "APIs for managing tenants")
public class TenantController {
    
    private final TenantService tenantService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new tenant", description = "Creates a new tenant with the provided details")
    public ApiResponse<TenantResponse> createTenant(@Valid @RequestBody TenantRequest request) {
        TenantResponse response = tenantService.createTenant(request);
        return ApiResponse.success("Tenant created successfully", response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get tenant by ID", description = "Retrieves tenant details by ID")
    public ApiResponse<TenantResponse> getTenantById(@PathVariable String id) {
        TenantResponse response = tenantService.getTenantById(id);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get tenant by slug", description = "Retrieves tenant details by slug")
    public ApiResponse<TenantResponse> getTenantBySlug(@PathVariable String slug) {
        TenantResponse response = tenantService.getTenantBySlug(slug);
        return ApiResponse.success(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update tenant", description = "Updates tenant details")
    public ApiResponse<TenantResponse> updateTenant(
            @PathVariable String id,
            @Valid @RequestBody TenantRequest request) {
        TenantResponse response = tenantService.updateTenant(id, request);
        return ApiResponse.success("Tenant updated successfully", response);
    }
    
    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate tenant", description = "Activates a tenant subscription")
    public ApiResponse<TenantResponse> activateTenant(@PathVariable String id) {
        TenantResponse response = tenantService.activateTenant(id);
        return ApiResponse.success("Tenant activated successfully", response);
    }
    
    @PostMapping("/{id}/suspend")
    @Operation(summary = "Suspend tenant", description = "Suspends a tenant")
    public ApiResponse<TenantResponse> suspendTenant(@PathVariable String id) {
        TenantResponse response = tenantService.suspendTenant(id);
        return ApiResponse.success("Tenant suspended successfully", response);
    }
    
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel tenant", description = "Cancels a tenant subscription")
    public ApiResponse<TenantResponse> cancelTenant(@PathVariable String id) {
        TenantResponse response = tenantService.cancelTenant(id);
        return ApiResponse.success("Tenant cancelled successfully", response);
    }
    
    @GetMapping
    @Operation(summary = "Get all tenants", description = "Retrieves all tenants with pagination")
    public ApiResponse<PageResponse<TenantResponse>> getAllTenants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        PageResponse<TenantResponse> response = tenantService.getAllTenants(pageable);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search tenants", description = "Searches tenants by name")
    public ApiResponse<PageResponse<TenantResponse>> searchTenants(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<TenantResponse> response = tenantService.searchTenants(name, pageable);
        return ApiResponse.success(response);
    }
}
