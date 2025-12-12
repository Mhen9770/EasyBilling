package com.easybilling.controller;

import com.easybilling.dto.*;
import com.easybilling.service.RecurringInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Recurring Invoice operations
 */
@RestController
@RequestMapping("/api/v1/recurring-invoices")
@RequiredArgsConstructor
@Tag(name = "Recurring Invoices", description = "Subscription and periodic billing operations")
@PreAuthorize("isAuthenticated()")
public class RecurringInvoiceController extends BaseController {

    private final RecurringInvoiceService recurringInvoiceService;

    @PostMapping
    @Operation(summary = "Create new recurring invoice schedule")
    @PreAuthorize("hasAnyAuthority('CREATE_RECURRING_INVOICE', 'ADMIN')")
    public ApiResponse<RecurringInvoiceResponse> createRecurringInvoice(@Valid @RequestBody RecurringInvoiceRequest request) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        RecurringInvoiceResponse response = recurringInvoiceService.createRecurringInvoice(tenantId, userId, request);
        return ApiResponse.success("Recurring invoice created successfully", response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update existing recurring invoice")
    @PreAuthorize("hasAnyAuthority('UPDATE_RECURRING_INVOICE', 'ADMIN')")
    public ApiResponse<RecurringInvoiceResponse> updateRecurringInvoice(
            @PathVariable Long id,
            @Valid @RequestBody RecurringInvoiceRequest request) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        RecurringInvoiceResponse response = recurringInvoiceService.updateRecurringInvoice(id, tenantId, userId, request);
        return ApiResponse.success("Recurring invoice updated successfully", response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get recurring invoice by ID")
    @PreAuthorize("hasAnyAuthority('VIEW_RECURRING_INVOICE', 'ADMIN')")
    public ApiResponse<RecurringInvoiceResponse> getRecurringInvoice(@PathVariable Long id) {
        Integer tenantId = getCurrentTenantId();
        RecurringInvoiceResponse response = recurringInvoiceService.getRecurringInvoiceById(id, tenantId);
        return ApiResponse.success(response);
    }

    @GetMapping
    @Operation(summary = "List all recurring invoices")
    @PreAuthorize("hasAnyAuthority('VIEW_RECURRING_INVOICE', 'ADMIN')")
    public ApiResponse<PageResponse<RecurringInvoiceResponse>> getAllRecurringInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Integer tenantId = getCurrentTenantId();
        Pageable pageable = PageRequest.of(page, size);
        Page<RecurringInvoiceResponse> result = recurringInvoiceService.getAllRecurringInvoices(tenantId, pageable);
        return ApiResponse.success(PageResponse.of(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        ));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active recurring invoices")
    @PreAuthorize("hasAnyAuthority('VIEW_RECURRING_INVOICE', 'ADMIN')")
    public ApiResponse<List<RecurringInvoiceResponse>> getActiveRecurringInvoices() {
        Integer tenantId = getCurrentTenantId();
        List<RecurringInvoiceResponse> response = recurringInvoiceService.getActiveRecurringInvoices(tenantId);
        return ApiResponse.success(response);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get recurring invoices by customer")
    @PreAuthorize("hasAnyAuthority('VIEW_RECURRING_INVOICE', 'ADMIN')")
    public ApiResponse<List<RecurringInvoiceResponse>> getRecurringInvoicesByCustomer(@PathVariable Long customerId) {
        Integer tenantId = getCurrentTenantId();
        List<RecurringInvoiceResponse> response = recurringInvoiceService.getRecurringInvoicesByCustomer(tenantId, customerId);
        return ApiResponse.success(response);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate recurring invoice")
    @PreAuthorize("hasAnyAuthority('UPDATE_RECURRING_INVOICE', 'ADMIN')")
    public ApiResponse<RecurringInvoiceResponse> activateRecurringInvoice(@PathVariable Long id) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        RecurringInvoiceResponse response = recurringInvoiceService.toggleActive(id, tenantId, userId, true);
        return ApiResponse.success("Recurring invoice activated successfully", response);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate recurring invoice")
    @PreAuthorize("hasAnyAuthority('UPDATE_RECURRING_INVOICE', 'ADMIN')")
    public ApiResponse<RecurringInvoiceResponse> deactivateRecurringInvoice(@PathVariable Long id) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        RecurringInvoiceResponse response = recurringInvoiceService.toggleActive(id, tenantId, userId, false);
        return ApiResponse.success("Recurring invoice deactivated successfully", response);
    }

    @PostMapping("/{id}/generate")
    @Operation(summary = "Manually generate invoice from recurring schedule")
    @PreAuthorize("hasAnyAuthority('CREATE_INVOICE', 'ADMIN')")
    public ApiResponse<InvoiceResponse> generateInvoice(@PathVariable Long id) {
        Integer tenantId = getCurrentTenantId();
        RecurringInvoiceResponse recurring = recurringInvoiceService.getRecurringInvoiceById(id, tenantId);
        
        // Convert response back to entity for generation (simplified)
        // In real implementation, we'd fetch the entity directly
        InvoiceResponse invoice = recurringInvoiceService.generateInvoiceFromRecurring(
                convertToEntity(recurring));
        
        return ApiResponse.success("Invoice generated successfully from recurring schedule", invoice);
    }

    /**
     * Helper method to convert response to entity (simplified)
     */
    private com.easybilling.entity.RecurringInvoice convertToEntity(RecurringInvoiceResponse response) {
        com.easybilling.entity.RecurringInvoice entity = new com.easybilling.entity.RecurringInvoice();
        entity.setId(response.getId());
        entity.setTenantId(response.getTenantId());
        entity.setCustomerId(response.getCustomerId());
        entity.setCustomerName(response.getCustomerName());
        entity.setFrequency(response.getFrequency());
        entity.setStartDate(response.getStartDate());
        entity.setEndDate(response.getEndDate());
        entity.setNextInvoiceDate(response.getNextInvoiceDate());
        entity.setLastInvoiceDate(response.getLastInvoiceDate());
        entity.setAmount(response.getAmount());
        entity.setDescription(response.getDescription());
        entity.setPaymentTerms(response.getPaymentTerms());
        entity.setLateFeePercentage(response.getLateFeePercentage());
        entity.setLateFeeAmount(response.getLateFeeAmount());
        entity.setGracePeriodDays(response.getGracePeriodDays());
        entity.setMaxInvoices(response.getMaxInvoices());
        entity.setInvoicesGenerated(response.getInvoicesGenerated());
        entity.setAutoEmail(response.getAutoEmail());
        entity.setActive(response.getActive());
        entity.setCreatedBy(response.getCreatedBy());
        entity.setCreatedAt(response.getCreatedAt());
        return entity;
    }
}
