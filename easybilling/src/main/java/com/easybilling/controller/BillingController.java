package com.easybilling.controller;

import com.easybilling.dto.*;
import com.easybilling.dto.InvoiceRequest;
import com.easybilling.dto.InvoiceResponse;
import com.easybilling.dto.PaymentRequest;
import com.easybilling.service.BillingService;
import com.easybilling.dto.ApiResponse;
import com.easybilling.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Billing", description = "Invoice and POS operations")
public class BillingController extends BaseController {

    private final BillingService billingService;

    @PostMapping
    @Operation(summary = "Create new invoice")
    public ApiResponse<InvoiceResponse> createInvoice(@Valid @RequestBody InvoiceRequest request) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        return ApiResponse.success("Invoice created", billingService.createInvoice(tenantId, userId, request));
    }

    @GetMapping
    @Operation(summary = "List all invoices")
    public ApiResponse<PageResponse<InvoiceResponse>> listInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Integer tenantId = getCurrentTenantId();
        Pageable pageable = PageRequest.of(page, size);
        Page<InvoiceResponse> result = billingService.listInvoices(tenantId, pageable);
        return ApiResponse.success(PageResponse.of(
            result.getContent(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements()
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID")
    public ApiResponse<InvoiceResponse> getInvoice(@PathVariable String id) {
        Integer tenantId = getCurrentTenantId();
        return ApiResponse.success(billingService.getInvoice(tenantId, id));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete invoice with payments")
    public ApiResponse<InvoiceResponse> completeInvoice(
            @PathVariable String id,
            @Valid @RequestBody List<PaymentRequest> payments) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        return ApiResponse.success("Invoice completed", billingService.completeInvoice(tenantId, id, userId, payments));
    }

    @PostMapping("/hold")
    @Operation(summary = "Hold invoice for later")
    public ApiResponse<String> holdInvoice(@Valid @RequestBody InvoiceRequest request) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        String holdRef = billingService.holdInvoice(tenantId, userId, request);
        return ApiResponse.success("Invoice held", holdRef);
    }
    
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel completed invoice and reverse stock")
    public ApiResponse<InvoiceResponse> cancelInvoice(
            @PathVariable String id,
            @RequestParam String reason) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        return ApiResponse.success("Invoice cancelled", billingService.cancelInvoice(tenantId, id, userId, reason));
    }
    
    @PostMapping("/{id}/return")
    @Operation(summary = "Process return for invoice items")
    public ApiResponse<InvoiceResponse> processReturn(
            @PathVariable String id,
            @RequestParam List<String> itemIds,
            @RequestParam String reason) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        return ApiResponse.success("Return processed", billingService.processReturn(tenantId, id, userId, itemIds, reason));
    }

    @GetMapping("/held")
    @Operation(summary = "List all held invoices")
    public ApiResponse<List<HeldInvoiceResponse>> listHeldInvoices() {
        Integer tenantId = getCurrentTenantId();
        return ApiResponse.success(billingService.listHeldInvoices(tenantId));
    }

    @GetMapping("/held/{holdReference}")
    @Operation(summary = "Resume a held invoice")
    public ApiResponse<InvoiceRequest> resumeHeldInvoice(@PathVariable String holdReference) {
        Integer tenantId = getCurrentTenantId();
        return ApiResponse.success("Held invoice retrieved", billingService.resumeHeldInvoice(tenantId, holdReference));
    }

    @DeleteMapping("/held/{holdReference}")
    @Operation(summary = "Delete a held invoice")
    public ApiResponse<Void> deleteHeldInvoice(@PathVariable String holdReference) {
        Integer tenantId = getCurrentTenantId();
        billingService.deleteHeldInvoice(tenantId, holdReference);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Held invoice deleted")
                .build();
    }
}
