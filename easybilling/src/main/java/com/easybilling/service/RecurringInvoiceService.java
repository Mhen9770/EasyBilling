package com.easybilling.service;

import com.easybilling.dto.*;
import com.easybilling.entity.Customer;
import com.easybilling.entity.RecurringInvoice;
import com.easybilling.enums.RecurringFrequency;
import com.easybilling.exception.ResourceNotFoundException;
import com.easybilling.repository.CustomerRepository;
import com.easybilling.repository.RecurringInvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing Recurring Invoices (Subscriptions)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecurringInvoiceService {

    private final RecurringInvoiceRepository recurringInvoiceRepository;
    private final CustomerRepository customerRepository;
    private final BillingService billingService;
    private final ConfigurationService configurationService;

    /**
     * Create a new recurring invoice schedule
     */
    public RecurringInvoiceResponse createRecurringInvoice(Integer tenantId, String userId, RecurringInvoiceRequest request) {
        log.info("Creating recurring invoice for tenant: {}, customer: {}", tenantId, request.getCustomerId());

        // Validate customer exists
        Customer customer = customerRepository.findByIdAndTenantId(request.getCustomerId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        // Create recurring invoice entity
        RecurringInvoice recurringInvoice = new RecurringInvoice();
        recurringInvoice.setTenantId(tenantId);
        recurringInvoice.setCustomerId(request.getCustomerId());
        recurringInvoice.setCustomerName(customer.getName());
        recurringInvoice.setFrequency(request.getFrequency());
        recurringInvoice.setStartDate(request.getStartDate());
        recurringInvoice.setEndDate(request.getEndDate());
        recurringInvoice.setNextInvoiceDate(request.getStartDate());
        recurringInvoice.setAmount(request.getAmount());
        recurringInvoice.setDescription(request.getDescription());
        recurringInvoice.setPaymentTerms(request.getPaymentTerms() != null ? request.getPaymentTerms() : "Net 30");
        recurringInvoice.setLateFeePercentage(request.getLateFeePercentage() != null ? request.getLateFeePercentage() : BigDecimal.ZERO);
        recurringInvoice.setLateFeeAmount(request.getLateFeeAmount() != null ? request.getLateFeeAmount() : BigDecimal.ZERO);
        recurringInvoice.setGracePeriodDays(request.getGracePeriodDays() != null ? request.getGracePeriodDays() : 0);
        recurringInvoice.setMaxInvoices(request.getMaxInvoices());
        recurringInvoice.setInvoicesGenerated(0);
        recurringInvoice.setAutoEmail(request.getAutoEmail() != null ? request.getAutoEmail() : false);
        recurringInvoice.setActive(true);
        recurringInvoice.setCreatedBy(userId);
        recurringInvoice.setCreatedAt(LocalDateTime.now());

        recurringInvoice = recurringInvoiceRepository.save(recurringInvoice);
        log.info("Created recurring invoice with ID: {}", recurringInvoice.getId());

        return mapToResponse(recurringInvoice);
    }

    /**
     * Update existing recurring invoice
     */
    public RecurringInvoiceResponse updateRecurringInvoice(Long id, Integer tenantId, String userId, RecurringInvoiceRequest request) {
        log.info("Updating recurring invoice: {} for tenant: {}", id, tenantId);

        RecurringInvoice recurringInvoice = recurringInvoiceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring invoice not found with id: " + id));

        // Update fields
        if (request.getFrequency() != null) {
            recurringInvoice.setFrequency(request.getFrequency());
        }
        if (request.getAmount() != null) {
            recurringInvoice.setAmount(request.getAmount());
        }
        if (request.getDescription() != null) {
            recurringInvoice.setDescription(request.getDescription());
        }
        if (request.getPaymentTerms() != null) {
            recurringInvoice.setPaymentTerms(request.getPaymentTerms());
        }
        if (request.getLateFeePercentage() != null) {
            recurringInvoice.setLateFeePercentage(request.getLateFeePercentage());
        }
        if (request.getLateFeeAmount() != null) {
            recurringInvoice.setLateFeeAmount(request.getLateFeeAmount());
        }
        if (request.getGracePeriodDays() != null) {
            recurringInvoice.setGracePeriodDays(request.getGracePeriodDays());
        }
        if (request.getEndDate() != null) {
            recurringInvoice.setEndDate(request.getEndDate());
        }
        if (request.getMaxInvoices() != null) {
            recurringInvoice.setMaxInvoices(request.getMaxInvoices());
        }
        if (request.getAutoEmail() != null) {
            recurringInvoice.setAutoEmail(request.getAutoEmail());
        }

        recurringInvoice.setUpdatedBy(userId);
        recurringInvoice.setUpdatedAt(LocalDateTime.now());

        recurringInvoice = recurringInvoiceRepository.save(recurringInvoice);
        log.info("Updated recurring invoice: {}", id);

        return mapToResponse(recurringInvoice);
    }

    /**
     * Get recurring invoice by ID
     */
    @Transactional(readOnly = true)
    public RecurringInvoiceResponse getRecurringInvoiceById(Long id, Integer tenantId) {
        RecurringInvoice recurringInvoice = recurringInvoiceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring invoice not found with id: " + id));
        return mapToResponse(recurringInvoice);
    }

    /**
     * Get all recurring invoices for a tenant
     */
    @Transactional(readOnly = true)
    public Page<RecurringInvoiceResponse> getAllRecurringInvoices(Integer tenantId, Pageable pageable) {
        Page<RecurringInvoice> invoices = recurringInvoiceRepository.findByTenantId(tenantId, pageable);
        return invoices.map(this::mapToResponse);
    }

    /**
     * Get active recurring invoices
     */
    @Transactional(readOnly = true)
    public List<RecurringInvoiceResponse> getActiveRecurringInvoices(Integer tenantId) {
        List<RecurringInvoice> invoices = recurringInvoiceRepository.findByTenantIdAndActive(tenantId, true);
        return invoices.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Get recurring invoices by customer
     */
    @Transactional(readOnly = true)
    public List<RecurringInvoiceResponse> getRecurringInvoicesByCustomer(Integer tenantId, Long customerId) {
        List<RecurringInvoice> invoices = recurringInvoiceRepository.findByTenantIdAndCustomerId(tenantId, customerId);
        return invoices.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Activate/Deactivate recurring invoice
     */
    public RecurringInvoiceResponse toggleActive(Long id, Integer tenantId, String userId, boolean active) {
        log.info("Setting recurring invoice {} active status to: {}", id, active);

        RecurringInvoice recurringInvoice = recurringInvoiceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring invoice not found with id: " + id));

        recurringInvoice.setActive(active);
        recurringInvoice.setUpdatedBy(userId);
        recurringInvoice.setUpdatedAt(LocalDateTime.now());

        recurringInvoice = recurringInvoiceRepository.save(recurringInvoice);
        return mapToResponse(recurringInvoice);
    }

    /**
     * Process due recurring invoices - called by scheduler
     */
    public void processDueInvoices() {
        log.info("Processing due recurring invoices");

        LocalDate today = LocalDate.now();
        List<RecurringInvoice> dueInvoices = recurringInvoiceRepository.findDueInvoices(today);

        log.info("Found {} due recurring invoices", dueInvoices.size());

        for (RecurringInvoice recurring : dueInvoices) {
            try {
                generateInvoiceFromRecurring(recurring);
            } catch (Exception e) {
                log.error("Error generating invoice for recurring invoice: {}", recurring.getId(), e);
            }
        }
    }

    /**
     * Generate an invoice from a recurring invoice schedule
     */
    public InvoiceResponse generateInvoiceFromRecurring(RecurringInvoice recurring) {
        log.info("Generating invoice from recurring invoice: {}", recurring.getId());

        // Check if max invoices reached
        if (recurring.getMaxInvoices() != null && recurring.getInvoicesGenerated() >= recurring.getMaxInvoices()) {
            log.info("Max invoices reached for recurring invoice: {}", recurring.getId());
            recurring.setActive(false);
            recurringInvoiceRepository.save(recurring);
            return null;
        }

        // Check if end date passed
        if (recurring.getEndDate() != null && LocalDate.now().isAfter(recurring.getEndDate())) {
            log.info("End date passed for recurring invoice: {}", recurring.getId());
            recurring.setActive(false);
            recurringInvoiceRepository.save(recurring);
            return null;
        }

        // Create invoice request
        InvoiceRequest invoiceRequest = new InvoiceRequest();
        invoiceRequest.setCustomerId(recurring.getCustomerId());
        invoiceRequest.setTotalAmount(recurring.getAmount());
        invoiceRequest.setNotes("Auto-generated from recurring invoice schedule");
        // Add more details as needed

        // Generate invoice using billing service
        InvoiceResponse invoice = billingService.createInvoice(recurring.getTenantId(), recurring.getCreatedBy(), invoiceRequest);

        // Update recurring invoice
        recurring.setInvoicesGenerated(recurring.getInvoicesGenerated() + 1);
        recurring.setLastInvoiceDate(LocalDate.now());
        recurring.setNextInvoiceDate(calculateNextInvoiceDate(recurring.getNextInvoiceDate(), recurring.getFrequency()));
        recurring.setUpdatedAt(LocalDateTime.now());
        recurringInvoiceRepository.save(recurring);

        log.info("Generated invoice {} from recurring invoice {}", invoice.getId(), recurring.getId());

        return invoice;
    }

    /**
     * Calculate next invoice date based on frequency
     */
    private LocalDate calculateNextInvoiceDate(LocalDate currentDate, RecurringFrequency frequency) {
        switch (frequency) {
            case DAILY:
                return currentDate.plusDays(1);
            case WEEKLY:
                return currentDate.plusWeeks(1);
            case BIWEEKLY:
                return currentDate.plusWeeks(2);
            case MONTHLY:
                return currentDate.plusMonths(1);
            case QUARTERLY:
                return currentDate.plusMonths(3);
            case SEMIANNUALLY:
                return currentDate.plusMonths(6);
            case ANNUALLY:
                return currentDate.plusYears(1);
            default:
                return currentDate.plusMonths(1);
        }
    }

    /**
     * Map entity to response DTO
     */
    private RecurringInvoiceResponse mapToResponse(RecurringInvoice recurring) {
        RecurringInvoiceResponse response = new RecurringInvoiceResponse();
        response.setId(recurring.getId());
        response.setTenantId(recurring.getTenantId());
        response.setCustomerId(recurring.getCustomerId());
        response.setCustomerName(recurring.getCustomerName());
        response.setFrequency(recurring.getFrequency());
        response.setStartDate(recurring.getStartDate());
        response.setEndDate(recurring.getEndDate());
        response.setNextInvoiceDate(recurring.getNextInvoiceDate());
        response.setLastInvoiceDate(recurring.getLastInvoiceDate());
        response.setAmount(recurring.getAmount());
        response.setDescription(recurring.getDescription());
        response.setPaymentTerms(recurring.getPaymentTerms());
        response.setLateFeePercentage(recurring.getLateFeePercentage());
        response.setLateFeeAmount(recurring.getLateFeeAmount());
        response.setGracePeriodDays(recurring.getGracePeriodDays());
        response.setMaxInvoices(recurring.getMaxInvoices());
        response.setInvoicesGenerated(recurring.getInvoicesGenerated());
        response.setAutoEmail(recurring.getAutoEmail());
        response.setActive(recurring.getActive());
        response.setCreatedBy(recurring.getCreatedBy());
        response.setCreatedAt(recurring.getCreatedAt());
        response.setUpdatedBy(recurring.getUpdatedBy());
        response.setUpdatedAt(recurring.getUpdatedAt());
        return response;
    }
}
