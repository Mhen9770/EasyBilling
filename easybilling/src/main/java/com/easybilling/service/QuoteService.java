package com.easybilling.service;

import com.easybilling.dto.*;
import com.easybilling.entity.Customer;
import com.easybilling.entity.Quote;
import com.easybilling.entity.QuoteItem;
import com.easybilling.enums.QuoteStatus;
import com.easybilling.exception.ResourceNotFoundException;
import com.easybilling.repository.CustomerRepository;
import com.easybilling.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing Quotes/Estimates
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceNumberService invoiceNumberService;
    private final BillingService billingService;

    /**
     * Create a new quote
     */
    public QuoteResponse createQuote(Integer tenantId, String userId, QuoteRequest request) {
        log.info("Creating quote for tenant: {}, customer: {}", tenantId, request.getCustomerId());

        // Get customer details if customer ID provided
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findByIdAndTenantId(request.getCustomerId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
        }

        // Build quote entity
        Quote quote = Quote.builder()
                .quoteNumber(generateQuoteNumber(tenantId))
                .tenantId(tenantId)
                .customerId(request.getCustomerId())
                .customerName(customer != null ? customer.getName() : request.getCustomerName())
                .customerEmail(customer != null ? customer.getEmail() : request.getCustomerEmail())
                .customerPhone(customer != null ? customer.getPhone() : request.getCustomerPhone())
                .customerAddress(request.getCustomerAddress())
                .status(QuoteStatus.DRAFT)
                .quoteDate(request.getQuoteDate() != null ? request.getQuoteDate() : LocalDate.now())
                .validUntil(request.getValidUntil() != null ? request.getValidUntil() : LocalDate.now().plusDays(30))
                .discount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO)
                .notes(request.getNotes())
                .terms(request.getTerms())
                .paymentTermDays(request.getPaymentTermDays())
                .documentTemplateId(request.getDocumentTemplateId())
                .createdBy(userId)
                .build();

        // Calculate totals
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;

        List<QuoteItem> quoteItems = new ArrayList<>();
        if (request.getItems() != null) {
            for (QuoteItemRequest itemReq : request.getItems()) {
                BigDecimal itemTotal = itemReq.getUnitPrice()
                        .multiply(itemReq.getQuantity())
                        .subtract(itemReq.getDiscount() != null ? itemReq.getDiscount() : BigDecimal.ZERO)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal itemTax = itemTotal.multiply(itemReq.getTaxRate() != null ? itemReq.getTaxRate() : BigDecimal.ZERO)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                QuoteItem item = QuoteItem.builder()
                        .quote(quote)
                        .productId(itemReq.getProductId())
                        .productName(itemReq.getProductName())
                        .description(itemReq.getDescription())
                        .quantity(itemReq.getQuantity())
                        .unitPrice(itemReq.getUnitPrice())
                        .discount(itemReq.getDiscount() != null ? itemReq.getDiscount() : BigDecimal.ZERO)
                        .taxRate(itemReq.getTaxRate() != null ? itemReq.getTaxRate() : BigDecimal.ZERO)
                        .total(itemTotal.add(itemTax))
                        .build();

                quoteItems.add(item);
                subtotal = subtotal.add(itemTotal);
                taxAmount = taxAmount.add(itemTax);
            }
        }

        quote.setItems(quoteItems);
        quote.setSubtotal(subtotal);
        quote.setTaxAmount(taxAmount);
        quote.setTotal(subtotal.add(taxAmount).subtract(quote.getDiscount()));

        Quote savedQuote = quoteRepository.save(quote);
        log.info("Quote created successfully: {}", savedQuote.getQuoteNumber());

        return mapToResponse(savedQuote);
    }

    /**
     * Send quote to customer
     */
    public QuoteResponse sendQuote(Integer tenantId, String quoteId) {
        Quote quote = quoteRepository.findByIdAndTenantId(quoteId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found"));

        if (quote.getStatus() == QuoteStatus.CONVERTED) {
            throw new IllegalStateException("Cannot send already converted quote");
        }

        quote.setStatus(QuoteStatus.SENT);
        quote.setSentAt(LocalDateTime.now());

        Quote updated = quoteRepository.save(quote);
        log.info("Quote sent: {}", quote.getQuoteNumber());

        // TODO: Send email notification with quote PDF

        return mapToResponse(updated);
    }

    /**
     * Convert quote to invoice
     */
    public InvoiceResponse convertToInvoice(Integer tenantId, String userId, String quoteId) {
        Quote quote = quoteRepository.findByIdAndTenantId(quoteId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found"));

        if (quote.getStatus() == QuoteStatus.CONVERTED) {
            throw new IllegalStateException("Quote already converted to invoice");
        }

        if (quote.getStatus() == QuoteStatus.REJECTED) {
            throw new IllegalStateException("Cannot convert rejected quote");
        }

        // Build invoice request from quote
        InvoiceRequest invoiceRequest = new InvoiceRequest();
        invoiceRequest.setCustomerId(quote.getCustomerId());
        invoiceRequest.setCustomerName(quote.getCustomerName());
        invoiceRequest.setCustomerPhone(quote.getCustomerPhone());
        invoiceRequest.setCustomerEmail(quote.getCustomerEmail());
        invoiceRequest.setNotes(quote.getNotes());
        invoiceRequest.setDiscount(quote.getDiscount());

        // Map quote items to invoice items
        List<InvoiceItemRequest> invoiceItems = quote.getItems().stream()
                .map(qi -> {
                    InvoiceItemRequest item = new InvoiceItemRequest();
                    item.setProductId(qi.getProductId());
                    item.setQuantity(qi.getQuantity());
                    item.setUnitPrice(qi.getUnitPrice());
                    item.setTaxRate(qi.getTaxRate());
                    return item;
                })
                .collect(Collectors.toList());
        invoiceRequest.setItems(invoiceItems);

        // Create invoice
        InvoiceResponse invoice = billingService.createInvoice(tenantId, userId, invoiceRequest);

        // Update quote status
        quote.setStatus(QuoteStatus.CONVERTED);
        quote.setConvertedInvoiceId(invoice.getId());
        quote.setConvertedAt(LocalDateTime.now());
        quoteRepository.save(quote);

        log.info("Quote {} converted to invoice {}", quote.getQuoteNumber(), invoice.getInvoiceNumber());

        return invoice;
    }

    /**
     * Accept a quote
     */
    public QuoteResponse acceptQuote(Integer tenantId, String quoteId) {
        Quote quote = quoteRepository.findByIdAndTenantId(quoteId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found"));

        if (quote.getStatus() == QuoteStatus.CONVERTED) {
            throw new IllegalStateException("Quote already converted");
        }

        quote.setStatus(QuoteStatus.ACCEPTED);
        quote.setAcceptedAt(LocalDateTime.now());

        Quote updated = quoteRepository.save(quote);
        log.info("Quote accepted: {}", quote.getQuoteNumber());

        return mapToResponse(updated);
    }

    /**
     * Reject a quote
     */
    public QuoteResponse rejectQuote(Integer tenantId, String quoteId, String reason) {
        Quote quote = quoteRepository.findByIdAndTenantId(quoteId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found"));

        if (quote.getStatus() == QuoteStatus.CONVERTED) {
            throw new IllegalStateException("Cannot reject converted quote");
        }

        quote.setStatus(QuoteStatus.REJECTED);
        quote.setRejectedAt(LocalDateTime.now());
        quote.setRejectionReason(reason);

        Quote updated = quoteRepository.save(quote);
        log.info("Quote rejected: {}", quote.getQuoteNumber());

        return mapToResponse(updated);
    }

    /**
     * Get quote by ID
     */
    @Transactional(readOnly = true)
    public QuoteResponse getQuoteById(Integer tenantId, String quoteId) {
        Quote quote = quoteRepository.findByIdAndTenantId(quoteId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found"));
        return mapToResponse(quote);
    }

    /**
     * Get all quotes with pagination
     */
    @Transactional(readOnly = true)
    public Page<QuoteResponse> getAllQuotes(Integer tenantId, Pageable pageable) {
        return quoteRepository.findAll(pageable).map(this::mapToResponse);
    }

    /**
     * Get quotes by status
     */
    @Transactional(readOnly = true)
    public Page<QuoteResponse> getQuotesByStatus(Integer tenantId, QuoteStatus status, Pageable pageable) {
        return quoteRepository.findByTenantIdAndStatus(tenantId, status, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get quotes by customer
     */
    @Transactional(readOnly = true)
    public Page<QuoteResponse> getQuotesByCustomer(Integer tenantId, String customerId, Pageable pageable) {
        return quoteRepository.findByTenantIdAndCustomerId(tenantId, customerId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Mark expired quotes
     */
    @Transactional
    public void markExpiredQuotes(Integer tenantId) {
        List<Quote> expiredQuotes = quoteRepository.findExpiredQuotes(tenantId, QuoteStatus.SENT, LocalDate.now());
        expiredQuotes.forEach(quote -> {
            quote.setStatus(QuoteStatus.EXPIRED);
            log.info("Quote marked as expired: {}", quote.getQuoteNumber());
        });
        quoteRepository.saveAll(expiredQuotes);
    }

    /**
     * Generate quote number
     */
    private String generateQuoteNumber(Integer tenantId) {
        String prefix = "QT";
        String year = String.valueOf(LocalDate.now().getYear());
        long count = quoteRepository.count() + 1;
        return String.format("%s/%s/%06d", prefix, year, count);
    }

    /**
     * Map entity to response DTO
     */
    private QuoteResponse mapToResponse(Quote quote) {
        List<QuoteItemResponse> items = quote.getItems().stream()
                .map(item -> QuoteItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .description(item.getDescription())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .discount(item.getDiscount())
                        .taxRate(item.getTaxRate())
                        .total(item.getTotal())
                        .build())
                .collect(Collectors.toList());

        return QuoteResponse.builder()
                .id(quote.getId())
                .quoteNumber(quote.getQuoteNumber())
                .customerId(quote.getCustomerId())
                .customerName(quote.getCustomerName())
                .customerEmail(quote.getCustomerEmail())
                .customerPhone(quote.getCustomerPhone())
                .customerAddress(quote.getCustomerAddress())
                .status(quote.getStatus())
                .quoteDate(quote.getQuoteDate())
                .validUntil(quote.getValidUntil())
                .items(items)
                .subtotal(quote.getSubtotal())
                .discount(quote.getDiscount())
                .taxAmount(quote.getTaxAmount())
                .total(quote.getTotal())
                .notes(quote.getNotes())
                .terms(quote.getTerms())
                .paymentTermDays(quote.getPaymentTermDays())
                .convertedInvoiceId(quote.getConvertedInvoiceId())
                .convertedAt(quote.getConvertedAt())
                .documentTemplateId(quote.getDocumentTemplateId())
                .createdBy(quote.getCreatedBy())
                .createdAt(quote.getCreatedAt())
                .updatedAt(quote.getUpdatedAt())
                .sentAt(quote.getSentAt())
                .acceptedAt(quote.getAcceptedAt())
                .build();
    }
}
