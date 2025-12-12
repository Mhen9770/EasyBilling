package com.easybilling.controller;

import com.easybilling.dto.*;
import com.easybilling.enums.CreditNoteStatus;
import com.easybilling.service.CreditNoteService;
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
 * REST Controller for Credit Note operations
 */
@RestController
@RequestMapping("/api/v1/credit-notes")
@RequiredArgsConstructor
@Tag(name = "Credit Notes", description = "Refund, return, and billing correction operations")
@PreAuthorize("isAuthenticated()")
public class CreditNoteController extends BaseController {

    private final CreditNoteService creditNoteService;

    @PostMapping
    @Operation(summary = "Create new credit note")
    @PreAuthorize("hasAnyAuthority('CREATE_CREDIT_NOTE', 'ADMIN')")
    public ApiResponse<CreditNoteResponse> createCreditNote(@Valid @RequestBody CreditNoteRequest request) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        CreditNoteResponse response = creditNoteService.createCreditNote(tenantId, userId, request);
        return ApiResponse.success("Credit note created successfully", response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update credit note (only in DRAFT status)")
    @PreAuthorize("hasAnyAuthority('UPDATE_CREDIT_NOTE', 'ADMIN')")
    public ApiResponse<CreditNoteResponse> updateCreditNote(
            @PathVariable Long id,
            @Valid @RequestBody CreditNoteRequest request) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        CreditNoteResponse response = creditNoteService.updateCreditNote(id, tenantId, userId, request);
        return ApiResponse.success("Credit note updated successfully", response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get credit note by ID")
    @PreAuthorize("hasAnyAuthority('VIEW_CREDIT_NOTE', 'ADMIN')")
    public ApiResponse<CreditNoteResponse> getCreditNote(@PathVariable Long id) {
        Integer tenantId = getCurrentTenantId();
        CreditNoteResponse response = creditNoteService.getCreditNoteById(id, tenantId);
        return ApiResponse.success(response);
    }

    @GetMapping
    @Operation(summary = "List all credit notes")
    @PreAuthorize("hasAnyAuthority('VIEW_CREDIT_NOTE', 'ADMIN')")
    public ApiResponse<PageResponse<CreditNoteResponse>> getAllCreditNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Integer tenantId = getCurrentTenantId();
        Pageable pageable = PageRequest.of(page, size);
        Page<CreditNoteResponse> result = creditNoteService.getAllCreditNotes(tenantId, pageable);
        return ApiResponse.success(PageResponse.of(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        ));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get credit notes by status")
    @PreAuthorize("hasAnyAuthority('VIEW_CREDIT_NOTE', 'ADMIN')")
    public ApiResponse<List<CreditNoteResponse>> getCreditNotesByStatus(@PathVariable CreditNoteStatus status) {
        Integer tenantId = getCurrentTenantId();
        List<CreditNoteResponse> response = creditNoteService.getCreditNotesByStatus(tenantId, status);
        return ApiResponse.success(response);
    }

    @GetMapping("/invoice/{invoiceId}")
    @Operation(summary = "Get credit notes by invoice")
    @PreAuthorize("hasAnyAuthority('VIEW_CREDIT_NOTE', 'ADMIN')")
    public ApiResponse<List<CreditNoteResponse>> getCreditNotesByInvoice(@PathVariable String invoiceId) {
        Integer tenantId = getCurrentTenantId();
        List<CreditNoteResponse> response = creditNoteService.getCreditNotesByInvoice(tenantId, invoiceId);
        return ApiResponse.success(response);
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit credit note for approval")
    @PreAuthorize("hasAnyAuthority('UPDATE_CREDIT_NOTE', 'ADMIN')")
    public ApiResponse<CreditNoteResponse> submitForApproval(@PathVariable Long id) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        CreditNoteResponse response = creditNoteService.submitForApproval(id, tenantId, userId);
        return ApiResponse.success("Credit note submitted for approval", response);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve credit note")
    @PreAuthorize("hasAnyAuthority('APPROVE_CREDIT_NOTE', 'ADMIN')")
    public ApiResponse<CreditNoteResponse> approveCreditNote(@PathVariable Long id) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        CreditNoteResponse response = creditNoteService.approveCreditNote(id, tenantId, userId);
        return ApiResponse.success("Credit note approved successfully", response);
    }

    @PostMapping("/{id}/issue")
    @Operation(summary = "Issue credit note (after approval)")
    @PreAuthorize("hasAnyAuthority('ISSUE_CREDIT_NOTE', 'ADMIN')")
    public ApiResponse<CreditNoteResponse> issueCreditNote(@PathVariable Long id) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        CreditNoteResponse response = creditNoteService.issueCreditNote(id, tenantId, userId);
        return ApiResponse.success("Credit note issued successfully", response);
    }

    @PostMapping("/{id}/apply")
    @Operation(summary = "Apply credit note to invoice or customer account")
    @PreAuthorize("hasAnyAuthority('APPLY_CREDIT_NOTE', 'ADMIN')")
    public ApiResponse<CreditNoteResponse> applyCreditNote(@PathVariable Long id) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        CreditNoteResponse response = creditNoteService.applyCreditNote(id, tenantId, userId);
        return ApiResponse.success("Credit note applied successfully", response);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel credit note")
    @PreAuthorize("hasAnyAuthority('CANCEL_CREDIT_NOTE', 'ADMIN')")
    public ApiResponse<CreditNoteResponse> cancelCreditNote(
            @PathVariable Long id,
            @RequestParam(required = false) String cancelReason) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        CreditNoteResponse response = creditNoteService.cancelCreditNote(id, tenantId, userId, cancelReason);
        return ApiResponse.success("Credit note cancelled successfully", response);
    }
}
