package com.easybilling.billing.controller;

import com.easybilling.billing.dto.*;
import com.easybilling.billing.service.BillingService;
import com.easybilling.common.dto.ApiResponse;
import com.easybilling.common.dto.PageResponse;
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
public class BillingController {

    private final BillingService billingService;

    @PostMapping
    @Operation(summary = "Create new invoice")
    public ApiResponse<InvoiceResponse> createInvoice(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody InvoiceRequest request) {
        return ApiResponse.success("Invoice created", billingService.createInvoice(tenantId, userId, request));
    }

    @GetMapping
    @Operation(summary = "List all invoices")
    public ApiResponse<PageResponse<InvoiceResponse>> listInvoices(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
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
    public ApiResponse<InvoiceResponse> getInvoice(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @PathVariable String id) {
        return ApiResponse.success(billingService.getInvoice(tenantId, id));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete invoice with payments")
    public ApiResponse<InvoiceResponse> completeInvoice(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id,
            @Valid @RequestBody List<PaymentRequest> payments) {
        return ApiResponse.success("Invoice completed", billingService.completeInvoice(tenantId, id, userId, payments));
    }

    @PostMapping("/hold")
    @Operation(summary = "Hold invoice for later")
    public ApiResponse<String> holdInvoice(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody InvoiceRequest request) {
        String holdRef = billingService.holdInvoice(tenantId, userId, request);
        return ApiResponse.success("Invoice held", holdRef);
    }
}
