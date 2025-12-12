package com.easybilling.service;

import com.easybilling.dto.*;
import com.easybilling.entity.CreditNote;
import com.easybilling.entity.CreditNoteItem;
import com.easybilling.entity.Invoice;
import com.easybilling.enums.CreditNoteReason;
import com.easybilling.enums.CreditNoteStatus;
import com.easybilling.exception.ResourceNotFoundException;
import com.easybilling.repository.CreditNoteRepository;
import com.easybilling.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing Credit Notes (Refunds, Returns, Corrections)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreditNoteService {

    private final CreditNoteRepository creditNoteRepository;
    private final InvoiceRepository invoiceRepository;
    private final InventoryService inventoryService;
    private final ConfigurationService configurationService;

    /**
     * Create a new credit note
     */
    public CreditNoteResponse createCreditNote(Integer tenantId, String userId, CreditNoteRequest request) {
        log.info("Creating credit note for tenant: {}, invoice: {}", tenantId, request.getInvoiceId());

        // Validate invoice exists
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + request.getInvoiceId()));

        if (!invoice.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Invoice does not belong to this tenant");
        }

        // Generate credit note number
        String creditNoteNumber = generateCreditNoteNumber(tenantId);

        // Create credit note entity
        CreditNote creditNote = new CreditNote();
        creditNote.setTenantId(tenantId);
        creditNote.setCreditNoteNumber(creditNoteNumber);
        creditNote.setInvoiceId(request.getInvoiceId());
        creditNote.setInvoiceNumber(invoice.getInvoiceNumber());
        creditNote.setCustomerId(invoice.getCustomerId());
        creditNote.setCustomerName(invoice.getCustomer() != null ? invoice.getCustomer().getName() : "");
        creditNote.setIssueDate(LocalDateTime.now());
        creditNote.setReason(request.getReason());
        creditNote.setReasonDescription(request.getReasonDescription());
        creditNote.setApplicationMethod(request.getApplicationMethod() != null ? request.getApplicationMethod() : "REDUCE_INVOICE");
        creditNote.setRestockItems(request.getRestockItems() != null ? request.getRestockItems() : false);
        creditNote.setStatus(CreditNoteStatus.DRAFT);
        creditNote.setNotes(request.getNotes());
        creditNote.setCreatedBy(userId);
        creditNote.setCreatedAt(LocalDateTime.now());

        // Add credit note items
        List<CreditNoteItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        if (request.getItems() != null) {
            for (CreditNoteItemRequest itemReq : request.getItems()) {
                CreditNoteItem item = new CreditNoteItem();
                item.setCreditNote(creditNote);
                item.setInvoiceItemId(itemReq.getInvoiceItemId());
                item.setProductId(itemReq.getProductId());
                item.setProductName(itemReq.getProductName());
                item.setDescription(itemReq.getDescription());
                item.setQuantity(itemReq.getQuantity());
                item.setUnitPrice(itemReq.getUnitPrice());
                item.setDiscount(itemReq.getDiscount() != null ? itemReq.getDiscount() : BigDecimal.ZERO);
                item.setTaxPercentage(itemReq.getTaxPercentage() != null ? itemReq.getTaxPercentage() : BigDecimal.ZERO);
                item.setRestockItem(itemReq.getRestockItem() != null ? itemReq.getRestockItem() : false);

                // Calculate amounts
                BigDecimal itemSubtotal = item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()))
                        .subtract(item.getDiscount());
                BigDecimal itemTax = itemSubtotal.multiply(item.getTaxPercentage()).divide(BigDecimal.valueOf(100));
                BigDecimal itemTotal = itemSubtotal.add(itemTax);

                item.setSubtotal(itemSubtotal);
                item.setTaxAmount(itemTax);
                item.setTotal(itemTotal);

                subtotal = subtotal.add(itemSubtotal);
                totalTax = totalTax.add(itemTax);

                items.add(item);
            }
        }

        creditNote.setItems(items);
        creditNote.setSubtotal(subtotal);
        creditNote.setTotalTax(totalTax);
        creditNote.setTotalAmount(subtotal.add(totalTax));

        creditNote = creditNoteRepository.save(creditNote);
        log.info("Created credit note with number: {}", creditNoteNumber);

        return mapToResponse(creditNote);
    }

    /**
     * Update existing credit note (only in DRAFT status)
     */
    public CreditNoteResponse updateCreditNote(String id, Integer tenantId, String userId, CreditNoteRequest request) {
        log.info("Updating credit note: {} for tenant: {}", id, tenantId);

        CreditNote creditNote = creditNoteRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit note not found with id: " + id));

        if (creditNote.getStatus() != CreditNoteStatus.DRAFT) {
            throw new IllegalStateException("Cannot update credit note in status: " + creditNote.getStatus());
        }

        // Update fields
        if (request.getReason() != null) {
            creditNote.setReason(request.getReason());
        }
        if (request.getReasonDescription() != null) {
            creditNote.setReasonDescription(request.getReasonDescription());
        }
        if (request.getApplicationMethod() != null) {
            creditNote.setApplicationMethod(request.getApplicationMethod());
        }
        if (request.getRestockItems() != null) {
            creditNote.setRestockItems(request.getRestockItems());
        }
        if (request.getNotes() != null) {
            creditNote.setNotes(request.getNotes());
        }

        // Update items if provided
        if (request.getItems() != null) {
            creditNote.getItems().clear();
            List<CreditNoteItem> items = new ArrayList<>();
            BigDecimal subtotal = BigDecimal.ZERO;
            BigDecimal totalTax = BigDecimal.ZERO;

            for (CreditNoteItemRequest itemReq : request.getItems()) {
                CreditNoteItem item = new CreditNoteItem();
                item.setCreditNote(creditNote);
                item.setInvoiceItemId(itemReq.getInvoiceItemId());
                item.setProductId(itemReq.getProductId());
                item.setProductName(itemReq.getProductName());
                item.setDescription(itemReq.getDescription());
                item.setQuantity(itemReq.getQuantity());
                item.setUnitPrice(itemReq.getUnitPrice());
                item.setDiscount(itemReq.getDiscount() != null ? itemReq.getDiscount() : BigDecimal.ZERO);
                item.setTaxPercentage(itemReq.getTaxPercentage() != null ? itemReq.getTaxPercentage() : BigDecimal.ZERO);
                item.setRestockItem(itemReq.getRestockItem() != null ? itemReq.getRestockItem() : false);

                // Calculate amounts
                BigDecimal itemSubtotal = item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()))
                        .subtract(item.getDiscount());
                BigDecimal itemTax = itemSubtotal.multiply(item.getTaxPercentage()).divide(BigDecimal.valueOf(100));
                BigDecimal itemTotal = itemSubtotal.add(itemTax);

                item.setSubtotal(itemSubtotal);
                item.setTaxAmount(itemTax);
                item.setTotal(itemTotal);

                subtotal = subtotal.add(itemSubtotal);
                totalTax = totalTax.add(itemTax);

                items.add(item);
            }

            creditNote.setItems(items);
            creditNote.setSubtotal(subtotal);
            creditNote.setTotalTax(totalTax);
            creditNote.setTotalAmount(subtotal.add(totalTax));
        }

        creditNote.setUpdatedBy(userId);
        creditNote.setUpdatedAt(LocalDateTime.now());

        creditNote = creditNoteRepository.save(creditNote);
        log.info("Updated credit note: {}", id);

        return mapToResponse(creditNote);
    }

    /**
     * Submit credit note for approval
     */
    public CreditNoteResponse submitForApproval(String id, Integer tenantId, String userId) {
        log.info("Submitting credit note {} for approval", id);

        CreditNote creditNote = creditNoteRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit note not found with id: " + id));

        if (creditNote.getStatus() != CreditNoteStatus.DRAFT) {
            throw new IllegalStateException("Credit note must be in DRAFT status to submit");
        }

        creditNote.setStatus(CreditNoteStatus.PENDING_APPROVAL);
        creditNote.setUpdatedBy(userId);
        creditNote.setUpdatedAt(LocalDateTime.now());

        creditNote = creditNoteRepository.save(creditNote);
        return mapToResponse(creditNote);
    }

    /**
     * Approve credit note
     */
    public CreditNoteResponse approveCreditNote(String id, Integer tenantId, String userId) {
        log.info("Approving credit note: {}", id);

        CreditNote creditNote = creditNoteRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit note not found with id: " + id));

        if (creditNote.getStatus() != CreditNoteStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Credit note must be pending approval to approve");
        }

        creditNote.setStatus(CreditNoteStatus.APPROVED);
        creditNote.setApprovedBy(userId);
        creditNote.setApprovedDate(LocalDateTime.now());
        creditNote.setUpdatedBy(userId);
        creditNote.setUpdatedAt(LocalDateTime.now());

        creditNote = creditNoteRepository.save(creditNote);
        return mapToResponse(creditNote);
    }

    /**
     * Issue credit note (after approval)
     */
    public CreditNoteResponse issueCreditNote(String id, Integer tenantId, String userId) {
        log.info("Issuing credit note: {}", id);

        CreditNote creditNote = creditNoteRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit note not found with id: " + id));

        if (creditNote.getStatus() != CreditNoteStatus.APPROVED) {
            throw new IllegalStateException("Credit note must be approved to issue");
        }

        creditNote.setStatus(CreditNoteStatus.ISSUED);
        creditNote.setIssuedDate(LocalDateTime.now());
        creditNote.setUpdatedBy(userId);
        creditNote.setUpdatedAt(LocalDateTime.now());

        // Restock items if requested
        if (creditNote.getRestockItems()) {
            restockItems(creditNote);
        }

        creditNote = creditNoteRepository.save(creditNote);
        return mapToResponse(creditNote);
    }

    /**
     * Apply credit note to invoice or customer account
     */
    public CreditNoteResponse applyCreditNote(String id, Integer tenantId, String userId) {
        log.info("Applying credit note: {}", id);

        CreditNote creditNote = creditNoteRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit note not found with id: " + id));

        if (creditNote.getStatus() != CreditNoteStatus.ISSUED) {
            throw new IllegalStateException("Credit note must be issued to apply");
        }

        // Apply credit based on application method
        switch (creditNote.getApplicationMethod()) {
            case "REDUCE_INVOICE":
                // Reduce invoice amount owed
                applyToInvoice(creditNote);
                break;
            case "REFUND":
                // Issue refund (would integrate with payment gateway)
                processRefund(creditNote);
                break;
            case "STORE_CREDIT":
                // Add to customer wallet/store credit
                addToStoreCredit(creditNote);
                break;
            default:
                log.warn("Unknown application method: {}", creditNote.getApplicationMethod());
        }

        creditNote.setStatus(CreditNoteStatus.APPLIED);
        creditNote.setAppliedDate(LocalDateTime.now());
        creditNote.setUpdatedBy(userId);
        creditNote.setUpdatedAt(LocalDateTime.now());

        creditNote = creditNoteRepository.save(creditNote);
        return mapToResponse(creditNote);
    }

    /**
     * Cancel credit note
     */
    public CreditNoteResponse cancelCreditNote(String id, Integer tenantId, String userId, String cancelReason) {
        log.info("Cancelling credit note: {}", id);

        CreditNote creditNote = creditNoteRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit note not found with id: " + id));

        if (creditNote.getStatus() == CreditNoteStatus.APPLIED) {
            throw new IllegalStateException("Cannot cancel applied credit note");
        }

        creditNote.setStatus(CreditNoteStatus.CANCELLED);
        creditNote.setCancelReason(cancelReason);
        creditNote.setUpdatedBy(userId);
        creditNote.setUpdatedAt(LocalDateTime.now());

        creditNote = creditNoteRepository.save(creditNote);
        return mapToResponse(creditNote);
    }

    /**
     * Get credit note by ID
     */
    @Transactional(readOnly = true)
    public CreditNoteResponse getCreditNoteById(String id, Integer tenantId) {
        CreditNote creditNote = creditNoteRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit note not found with id: " + id));
        return mapToResponse(creditNote);
    }

    /**
     * Get all credit notes for a tenant
     */
    @Transactional(readOnly = true)
    public Page<CreditNoteResponse> getAllCreditNotes(Integer tenantId, Pageable pageable) {
        Page<CreditNote> creditNotes = creditNoteRepository.findByTenantId(tenantId, pageable);
        return creditNotes.map(this::mapToResponse);
    }

    /**
     * Get credit notes by status
     */
    @Transactional(readOnly = true)
    public List<CreditNoteResponse> getCreditNotesByStatus(Integer tenantId, CreditNoteStatus status) {
        List<CreditNote> creditNotes = creditNoteRepository.findByTenantIdAndStatus(tenantId, status);
        return creditNotes.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Get credit notes by invoice
     */
    @Transactional(readOnly = true)
    public List<CreditNoteResponse> getCreditNotesByInvoice(Integer tenantId, String invoiceId) {
        List<CreditNote> creditNotes = creditNoteRepository.findByTenantIdAndInvoiceId(tenantId, invoiceId);
        return creditNotes.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Generate credit note number
     */
    private String generateCreditNoteNumber(Integer tenantId) {
        String prefix = configurationService.getConfigValue("billing.credit_note_prefix", tenantId, "CN");
        long count = creditNoteRepository.countByTenantId(tenantId) + 1;
        return String.format("%s/%04d", prefix, count);
    }

    /**
     * Restock items from credit note
     */
    private void restockItems(CreditNote creditNote) {
        log.info("Restocking items from credit note: {}", creditNote.getId());

        for (CreditNoteItem item : creditNote.getItems()) {
            if (item.getRestockItem() && item.getProductId() != null) {
                try {
                    // Increase product stock
                    inventoryService.adjustStock(item.getProductId(), creditNote.getTenantId(), 
                            item.getQuantity(), "Restocked from credit note: " + creditNote.getCreditNoteNumber());
                    log.info("Restocked {} units of product {}", item.getQuantity(), item.getProductId());
                } catch (Exception e) {
                    log.error("Error restocking product {}: {}", item.getProductId(), e.getMessage());
                }
            }
        }
    }

    /**
     * Apply credit note to reduce invoice amount
     */
    private void applyToInvoice(CreditNote creditNote) {
        log.info("Applying credit note {} to invoice {}", creditNote.getId(), creditNote.getInvoiceId());
        // Implementation would update invoice to reduce amount owed
        // This would integrate with payment/invoice service
    }

    /**
     * Process refund for credit note
     */
    private void processRefund(CreditNote creditNote) {
        log.info("Processing refund for credit note {}", creditNote.getId());
        // Implementation would integrate with payment gateway to issue refund
    }

    /**
     * Add credit note amount to customer's store credit
     */
    private void addToStoreCredit(CreditNote creditNote) {
        log.info("Adding credit note {} amount to customer {} store credit", creditNote.getId(), creditNote.getCustomerId());
        // Implementation would add to customer wallet/store credit balance
    }

    /**
     * Map entity to response DTO
     */
    private CreditNoteResponse mapToResponse(CreditNote creditNote) {
        CreditNoteResponse response = new CreditNoteResponse();
        response.setId(creditNote.getId());
        response.setTenantId(creditNote.getTenantId());
        response.setCreditNoteNumber(creditNote.getCreditNoteNumber());
        response.setInvoiceId(creditNote.getInvoiceId());
        response.setInvoiceNumber(creditNote.getInvoiceNumber());
        response.setCustomerId(creditNote.getCustomerId());
        response.setCustomerName(creditNote.getCustomerName());
        response.setIssueDate(creditNote.getIssueDate());
        response.setReason(creditNote.getReason());
        response.setReasonDescription(creditNote.getReasonDescription());
        response.setApplicationMethod(creditNote.getApplicationMethod());
        response.setRestockItems(creditNote.getRestockItems());
        response.setStatus(creditNote.getStatus());
        response.setSubtotal(creditNote.getSubtotal());
        response.setTotalTax(creditNote.getTotalTax());
        response.setTotalAmount(creditNote.getTotalAmount());
        response.setNotes(creditNote.getNotes());
        response.setApprovedBy(creditNote.getApprovedBy());
        response.setApprovedDate(creditNote.getApprovedDate());
        response.setIssuedDate(creditNote.getIssuedDate());
        response.setAppliedDate(creditNote.getAppliedDate());
        response.setCancelReason(creditNote.getCancelReason());
        response.setCreatedBy(creditNote.getCreatedBy());
        response.setCreatedAt(creditNote.getCreatedAt());
        response.setUpdatedBy(creditNote.getUpdatedBy());
        response.setUpdatedAt(creditNote.getUpdatedAt());

        // Map items
        if (creditNote.getItems() != null) {
            List<CreditNoteItemResponse> items = creditNote.getItems().stream()
                    .map(this::mapItemToResponse)
                    .collect(Collectors.toList());
            response.setItems(items);
        }

        return response;
    }

    /**
     * Map item entity to response DTO
     */
    private CreditNoteItemResponse mapItemToResponse(CreditNoteItem item) {
        CreditNoteItemResponse response = new CreditNoteItemResponse();
        response.setId(item.getId());
        response.setInvoiceItemId(item.getInvoiceItemId());
        response.setProductId(item.getProductId());
        response.setProductName(item.getProductName());
        response.setDescription(item.getDescription());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setDiscount(item.getDiscount());
        response.setSubtotal(item.getSubtotal());
        response.setTaxPercentage(item.getTaxPercentage());
        response.setTaxAmount(item.getTaxAmount());
        response.setTotal(item.getTotal());
        response.setRestockItem(item.getRestockItem());
        return response;
    }
}
