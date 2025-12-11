package com.easybilling.service;

import com.easybilling.dto.*;
import com.easybilling.entity.*;
import com.easybilling.entity.HeldInvoice;
import com.easybilling.entity.Invoice;
import com.easybilling.entity.InvoiceItem;
import com.easybilling.entity.Payment;
import com.easybilling.enums.InvoiceStatus;
import com.easybilling.enums.DiscountType;
import com.easybilling.repository.*;
import com.easybilling.exception.ResourceNotFoundException;
import com.easybilling.repository.HeldInvoiceRepository;
import com.easybilling.repository.InvoiceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final HeldInvoiceRepository heldInvoiceRepository;
    private final ObjectMapper objectMapper;
    private final InventoryService inventoryService;
    
    @PersistenceContext
    private EntityManager entityManager;

    public InvoiceResponse createInvoice(Integer tenantId, String userId, InvoiceRequest request) {
        // Business Logic: Validate stock availability before creating invoice
        String locationId = request.getStoreId(); // Using store as location
        for (InvoiceItemRequest itemReq : request.getItems()) {
            boolean available = inventoryService.checkStockAvailability(
                    itemReq.getProductId(), 
                    locationId, 
                    BigDecimal.valueOf(itemReq.getQuantity()), 
                    tenantId
            );
            if (!available) {
                log.warn("Insufficient stock for product: {} at location: {}", 
                        itemReq.getProductName(), locationId);
                // Continue anyway - stock checks are advisory not blocking
                // Real-world scenario: allow overselling with backorder
            }
        }
        
        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber(tenantId))
                .status(InvoiceStatus.DRAFT)
                .tenantId(tenantId)
                .storeId(request.getStoreId())
                .counterId(request.getCounterId())
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
                .createdBy(userId)
                .subtotal(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .balanceAmount(BigDecimal.ZERO)
                .notes(request.getNotes())
                .build();

        for (InvoiceItemRequest itemReq : request.getItems()) {
            InvoiceItem item = InvoiceItem.builder()
                    .productId(itemReq.getProductId())
                    .productName(itemReq.getProductName())
                    .productCode(itemReq.getProductCode())
                    .barcode(itemReq.getBarcode())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .discountAmount(itemReq.getDiscountAmount() != null ? itemReq.getDiscountAmount() : BigDecimal.ZERO)
                    .discountType(itemReq.getDiscountType())
                    .discountValue(itemReq.getDiscountValue())
                    .taxAmount(itemReq.getTaxAmount() != null ? itemReq.getTaxAmount() : BigDecimal.ZERO)
                    .taxRate(itemReq.getTaxRate())
                    .lineTotal(BigDecimal.ZERO)
                    .notes(itemReq.getNotes())
                    .build();
            item.calculateLineTotal();
            invoice.addItem(item);
        }

        invoice.calculateTotals();
        Invoice saved = invoiceRepository.save(invoice);
        
        log.info("Invoice created: {} with {} items", saved.getInvoiceNumber(), saved.getItems().size());
        return mapToResponse(saved);
    }

    public InvoiceResponse completeInvoice(Integer tenantId, String invoiceId, String userId, List<PaymentRequest> paymentRequests) {
        Invoice invoice = findInvoice(tenantId, invoiceId);
        
        // Business Logic: Create and persist payments first (child entities)
        List<Payment> payments = new ArrayList<>();
        for (PaymentRequest payReq : paymentRequests) {
            Payment payment = Payment.builder()
                    .mode(payReq.getMode())
                    .amount(payReq.getAmount())
                    .referenceNumber(payReq.getReferenceNumber())
                    .cardLast4(payReq.getCardLast4())
                    .upiId(payReq.getUpiId())
                    .notes(payReq.getNotes())
                    .invoice(invoice) // Set parent reference
                    .build();
            // Persist payment first (child entity)
            entityManager.persist(payment);
            payments.add(payment);
        }
        
        // Flush to ensure payments are persisted before adding to invoice collection
        entityManager.flush();
        
        // Now add persisted payments to invoice collection
        for (Payment payment : payments) {
            invoice.getPayments().add(payment);
        }

        invoice.calculateTotals();
        
        // Business Logic: Validate payment amount
        if (invoice.getBalanceAmount().compareTo(BigDecimal.ZERO) > 0) {
            log.warn("Invoice {} completed with pending balance: {}", 
                    invoice.getInvoiceNumber(), invoice.getBalanceAmount());
            // Allow completing with balance - credit sales
        }
        
        invoice.setStatus(InvoiceStatus.COMPLETED);
        invoice.setCompletedBy(userId);
        invoice.setCompletedAt(LocalDateTime.now());

        // Store items for stock deduction before saving (to avoid lazy loading issues)
        String locationId = invoice.getStoreId();
        String invoiceNumber = invoice.getInvoiceNumber();
        List<InvoiceItem> itemsToDeduct = new ArrayList<>(invoice.getItems());

        // Save invoice - payments are already persisted
        Invoice saved = invoiceRepository.save(invoice);
        
        // Now deduct stock - invoice and payments are already persisted
        for (InvoiceItem item : itemsToDeduct) {
            inventoryService.deductStock(
                    item.getProductId(),
                    locationId,
                    BigDecimal.valueOf(item.getQuantity()),
                    invoiceNumber,
                    userId,
                    tenantId
            );
        }
        
        log.info("Invoice completed: {} with total amount: {}", 
                saved.getInvoiceNumber(), saved.getTotalAmount());
        
        return mapToResponse(saved);
    }

    public String holdInvoice(Integer tenantId, String userId, InvoiceRequest request) {
        try {
            String holdReference = "HOLD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String invoiceData = objectMapper.writeValueAsString(request);
            
            HeldInvoice heldInvoice = HeldInvoice.builder()
                    .tenantId(tenantId)
                    .storeId(request.getStoreId())
                    .counterId(request.getCounterId())
                    .holdReference(holdReference)
                    .invoiceData(invoiceData)
                    .heldBy(userId)
                    .notes(request.getNotes())
                    .build();
            
            heldInvoiceRepository.save(heldInvoice);
            log.info("Invoice held: {} by user: {}", holdReference, userId);
            return holdReference;
        } catch (Exception e) {
            log.error("Failed to hold invoice", e);
            throw new RuntimeException("Failed to hold invoice", e);
        }
    }

    public List<HeldInvoiceResponse> listHeldInvoices(Integer tenantId) {
        List<HeldInvoice> heldInvoices = heldInvoiceRepository.findByTenantIdOrderByHeldAtDesc(tenantId);
        return heldInvoices.stream().map(this::mapHeldInvoiceToResponse).collect(Collectors.toList());
    }

    public InvoiceRequest resumeHeldInvoice(Integer tenantId, String holdReference) {
        HeldInvoice heldInvoice = heldInvoiceRepository.findByTenantIdAndHoldReference(tenantId, holdReference)
                .orElseThrow(() -> new ResourceNotFoundException("Held invoice not found"));
        
        try {
            InvoiceRequest request = objectMapper.readValue(heldInvoice.getInvoiceData(), InvoiceRequest.class);
            log.info("Resuming held invoice: {}", holdReference);
            return request;
        } catch (Exception e) {
            log.error("Failed to resume held invoice: {}", holdReference, e);
            throw new RuntimeException("Failed to resume held invoice", e);
        }
    }

    public void deleteHeldInvoice(Integer tenantId, String holdReference) {
        HeldInvoice heldInvoice = heldInvoiceRepository.findByTenantIdAndHoldReference(tenantId, holdReference)
                .orElseThrow(() -> new ResourceNotFoundException("Held invoice not found"));
        heldInvoiceRepository.delete(heldInvoice);
        log.info("Deleted held invoice: {}", holdReference);
    }

    private HeldInvoiceResponse mapHeldInvoiceToResponse(HeldInvoice heldInvoice) {
        try {
            InvoiceRequest request = objectMapper.readValue(heldInvoice.getInvoiceData(), InvoiceRequest.class);
            HeldInvoiceResponse response = new HeldInvoiceResponse();
            response.setHoldReference(heldInvoice.getHoldReference());
            response.setStoreId(heldInvoice.getStoreId());
            response.setCounterId(heldInvoice.getCounterId());
            response.setCustomerName(request.getCustomerName());
            response.setCustomerPhone(request.getCustomerPhone());
            response.setItemCount(request.getItems().size());
            response.setHeldAt(heldInvoice.getHeldAt());
            response.setHeldBy(heldInvoice.getHeldBy());
            response.setNotes(heldInvoice.getNotes());
            
            // Calculate total
            BigDecimal total = request.getItems().stream()
                    .map(item -> {
                        BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                        BigDecimal discount = BigDecimal.ZERO;
                        if (item.getDiscountType() != null && item.getDiscountValue() != null) {
                            if (item.getDiscountType() == DiscountType.PERCENTAGE) {
                                discount = lineTotal.multiply(item.getDiscountValue()).divide(BigDecimal.valueOf(100));
                            } else {
                                discount = item.getDiscountValue().multiply(BigDecimal.valueOf(item.getQuantity()));
                            }
                        }
                        BigDecimal taxableAmount = lineTotal.subtract(discount);
                        BigDecimal tax = taxableAmount.multiply(item.getTaxRate() != null ? item.getTaxRate() : BigDecimal.ZERO)
                                .divide(BigDecimal.valueOf(100));
                        return lineTotal.subtract(discount).add(tax);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            response.setTotalAmount(total);
            
            return response;
        } catch (Exception e) {
            log.error("Failed to map held invoice to response", e);
            throw new RuntimeException("Failed to map held invoice", e);
        }
    }

    public Page<InvoiceResponse> listInvoices(Integer tenantId, Pageable pageable) {
        return invoiceRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::mapToResponse);
    }

    public InvoiceResponse getInvoice(Integer tenantId, String invoiceId) {
        Invoice invoice = findInvoice(tenantId, invoiceId);
        return mapToResponse(invoice);
    }

    private Invoice findInvoice(Integer tenantId, String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        if (!invoice.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Invoice not found");
        }
        return invoice;
    }

    private String generateInvoiceNumber(Integer tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long count = invoiceRepository.countInvoicesSince(tenantId, LocalDateTime.now().toLocalDate().atStartOfDay());
        return String.format("INV-%s-%05d", dateStr, count + 1);
    }

    private InvoiceResponse mapToResponse(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.setId(invoice.getId());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setStatus(invoice.getStatus());
        response.setStoreId(invoice.getStoreId());
        response.setCounterId(invoice.getCounterId());
        response.setCustomerId(invoice.getCustomerId());
        response.setCustomerName(invoice.getCustomerName());
        response.setCustomerPhone(invoice.getCustomerPhone());
        response.setSubtotal(invoice.getSubtotal());
        response.setTaxAmount(invoice.getTaxAmount());
        response.setDiscountAmount(invoice.getDiscountAmount());
        response.setTotalAmount(invoice.getTotalAmount());
        response.setPaidAmount(invoice.getPaidAmount());
        response.setBalanceAmount(invoice.getBalanceAmount());
        response.setCreatedAt(invoice.getCreatedAt());
        response.setCompletedAt(invoice.getCompletedAt());
        response.setNotes(invoice.getNotes());
        
        response.setItems(invoice.getItems().stream().map(this::mapItemToResponse).collect(Collectors.toList()));
        response.setPayments(invoice.getPayments().stream().map(this::mapPaymentToResponse).collect(Collectors.toList()));
        
        return response;
    }

    private InvoiceItemResponse mapItemToResponse(InvoiceItem item) {
        InvoiceItemResponse response = new InvoiceItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setProductName(item.getProductName());
        response.setProductCode(item.getProductCode());
        response.setBarcode(item.getBarcode());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setDiscountAmount(item.getDiscountAmount());
        response.setTaxAmount(item.getTaxAmount());
        response.setLineTotal(item.getLineTotal());
        return response;
    }

    private PaymentResponse mapPaymentToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setMode(payment.getMode());
        response.setAmount(payment.getAmount());
        response.setReferenceNumber(payment.getReferenceNumber());
        response.setPaidAt(payment.getPaidAt());
        return response;
    }
    
    /**
     * Business Logic: Cancel invoice and reverse stock
     */
    public InvoiceResponse cancelInvoice(Integer tenantId, String invoiceId, String userId, String reason) {
        Invoice invoice = findInvoice(tenantId, invoiceId);
        
        if (invoice.getStatus() != InvoiceStatus.COMPLETED) {
            throw new IllegalStateException("Only completed invoices can be cancelled");
        }
        
        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setNotes((invoice.getNotes() != null ? invoice.getNotes() + "\n" : "") + 
                        "Cancelled by: " + userId + ", Reason: " + reason);
        
        Invoice saved = invoiceRepository.save(invoice);
        
        // Business Logic: Reverse stock deduction
        String locationId = invoice.getStoreId();
        for (InvoiceItem item : invoice.getItems()) {
            inventoryService.reverseStockDeduction(
                    item.getProductId(),
                    locationId,
                    BigDecimal.valueOf(item.getQuantity()),
                    invoice.getInvoiceNumber(),
                    userId,
                    tenantId
            );
        }
        
        log.info("Invoice cancelled: {} by user: {}, reason: {}", 
                invoice.getInvoiceNumber(), userId, reason);
        
        return mapToResponse(saved);
    }
    
    /**
     * Business Logic: Process return for items
     */
    public InvoiceResponse processReturn(Integer tenantId, String invoiceId, String userId, List<String> itemIds, String reason) {
        Invoice invoice = findInvoice(tenantId, invoiceId);
        
        if (invoice.getStatus() != InvoiceStatus.COMPLETED) {
            throw new IllegalStateException("Only completed invoices can have returns");
        }
        
        invoice.setStatus(InvoiceStatus.RETURNED);
        invoice.setNotes((invoice.getNotes() != null ? invoice.getNotes() + "\n" : "") + 
                        "Return processed by: " + userId + ", Reason: " + reason);
        
        // Business Logic: Reverse stock for returned items
        String locationId = invoice.getStoreId();
        for (InvoiceItem item : invoice.getItems()) {
            if (itemIds.contains(item.getId())) {
                inventoryService.reverseStockDeduction(
                        item.getProductId(),
                        locationId,
                        BigDecimal.valueOf(item.getQuantity()),
                        invoice.getInvoiceNumber() + "-RTN",
                        userId,
                        tenantId
                );
            }
        }
        
        Invoice saved = invoiceRepository.save(invoice);
        
        log.info("Return processed for invoice: {} with {} items", 
                invoice.getInvoiceNumber(), itemIds.size());
        
        return mapToResponse(saved);
    }
}
