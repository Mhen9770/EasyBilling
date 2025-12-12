package com.easybilling.controller;

import com.easybilling.dto.*;
import com.easybilling.enums.QuoteStatus;
import com.easybilling.service.QuoteService;
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
 * REST Controller for Quote/Estimate operations
 */
@RestController
@RequestMapping("/api/v1/quotes")
@RequiredArgsConstructor
@Tag(name = "Quotes", description = "Quote and estimate operations")
@PreAuthorize("isAuthenticated()")
public class QuoteController extends BaseController {

    private final QuoteService quoteService;

    @PostMapping
    @Operation(summary = "Create new quote")
    @PreAuthorize("hasAnyAuthority('CREATE_QUOTE', 'ADMIN')")
    public ApiResponse<QuoteResponse> createQuote(@Valid @RequestBody QuoteRequest request) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        QuoteResponse response = quoteService.createQuote(tenantId, userId, request);
        return ApiResponse.success("Quote created successfully", response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update quote (only in DRAFT status)")
    @PreAuthorize("hasAnyAuthority('UPDATE_QUOTE', 'ADMIN')")
    public ApiResponse<QuoteResponse> updateQuote(
            @PathVariable Long id,
            @Valid @RequestBody QuoteRequest request) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        QuoteResponse response = quoteService.updateQuote(id, tenantId, userId, request);
        return ApiResponse.success("Quote updated successfully", response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get quote by ID")
    @PreAuthorize("hasAnyAuthority('VIEW_QUOTE', 'ADMIN')")
    public ApiResponse<QuoteResponse> getQuote(@PathVariable Long id) {
        Integer tenantId = getCurrentTenantId();
        QuoteResponse response = quoteService.getQuoteById(id, tenantId);
        return ApiResponse.success(response);
    }

    @GetMapping
    @Operation(summary = "List all quotes")
    @PreAuthorize("hasAnyAuthority('VIEW_QUOTE', 'ADMIN')")
    public ApiResponse<PageResponse<QuoteResponse>> getAllQuotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Integer tenantId = getCurrentTenantId();
        Pageable pageable = PageRequest.of(page, size);
        Page<QuoteResponse> result = quoteService.getAllQuotes(tenantId, pageable);
        return ApiResponse.success(PageResponse.of(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        ));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get quotes by status")
    @PreAuthorize("hasAnyAuthority('VIEW_QUOTE', 'ADMIN')")
    public ApiResponse<List<QuoteResponse>> getQuotesByStatus(@PathVariable QuoteStatus status) {
        Integer tenantId = getCurrentTenantId();
        List<QuoteResponse> response = quoteService.getQuotesByStatus(tenantId, status);
        return ApiResponse.success(response);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get quotes by customer")
    @PreAuthorize("hasAnyAuthority('VIEW_QUOTE', 'ADMIN')")
    public ApiResponse<List<QuoteResponse>> getQuotesByCustomer(@PathVariable Long customerId) {
        Integer tenantId = getCurrentTenantId();
        List<QuoteResponse> response = quoteService.getQuotesByCustomer(tenantId, customerId);
        return ApiResponse.success(response);
    }

    @PostMapping("/{id}/send")
    @Operation(summary = "Send quote to customer")
    @PreAuthorize("hasAnyAuthority('SEND_QUOTE', 'ADMIN')")
    public ApiResponse<QuoteResponse> sendQuote(@PathVariable Long id) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        QuoteResponse response = quoteService.sendQuote(id, tenantId, userId);
        return ApiResponse.success("Quote sent to customer successfully", response);
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "Accept quote")
    @PreAuthorize("hasAnyAuthority('ACCEPT_QUOTE', 'ADMIN')")
    public ApiResponse<QuoteResponse> acceptQuote(
            @PathVariable Long id,
            @RequestParam(required = false) String acceptanceNotes) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        QuoteResponse response = quoteService.acceptQuote(id, tenantId, userId, acceptanceNotes);
        return ApiResponse.success("Quote accepted successfully", response);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject quote")
    @PreAuthorize("hasAnyAuthority('REJECT_QUOTE', 'ADMIN')")
    public ApiResponse<QuoteResponse> rejectQuote(
            @PathVariable Long id,
            @RequestParam(required = false) String rejectionReason) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        QuoteResponse response = quoteService.rejectQuote(id, tenantId, userId, rejectionReason);
        return ApiResponse.success("Quote rejected", response);
    }

    @PostMapping("/{id}/convert")
    @Operation(summary = "Convert quote to invoice")
    @PreAuthorize("hasAnyAuthority('CREATE_INVOICE', 'ADMIN')")
    public ApiResponse<InvoiceResponse> convertToInvoice(@PathVariable Long id) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        InvoiceResponse response = quoteService.convertToInvoice(id, tenantId, userId);
        return ApiResponse.success("Quote converted to invoice successfully", response);
    }

    @GetMapping("/expired")
    @Operation(summary = "Get expired quotes")
    @PreAuthorize("hasAnyAuthority('VIEW_QUOTE', 'ADMIN')")
    public ApiResponse<List<QuoteResponse>> getExpiredQuotes() {
        Integer tenantId = getCurrentTenantId();
        List<QuoteResponse> response = quoteService.getExpiredQuotes(tenantId);
        return ApiResponse.success(response);
    }
}
