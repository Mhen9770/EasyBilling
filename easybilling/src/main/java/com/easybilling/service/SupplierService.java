package com.easybilling.service;

import com.easybilling.dto.SupplierRequest;
import com.easybilling.dto.SupplierResponse;
import com.easybilling.entity.Supplier;
import com.easybilling.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SupplierService {
    
    private final SupplierRepository supplierRepository;
    
    public SupplierResponse createSupplier(SupplierRequest request, String tenantId) {
        log.info("Creating supplier for tenant: {}", tenantId);
        
        Supplier supplier = Supplier.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .contactPerson(request.getContactPerson())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .country(request.getCountry())
                .gstin(request.getGstin())
                .website(request.getWebsite())
                .notes(request.getNotes())
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .ifscCode(request.getIfscCode())
                .creditDays(request.getCreditDays() != null ? request.getCreditDays() : 0)
                .totalPurchases(BigDecimal.ZERO)
                .outstandingBalance(BigDecimal.ZERO)
                .purchaseCount(0)
                .build();
        
        Supplier saved = supplierRepository.save(supplier);
        return mapToResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public Page<SupplierResponse> getAllSuppliers(String tenantId, Pageable pageable) {
        return supplierRepository.findByTenantId(tenantId, pageable)
                .map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<SupplierResponse> searchSuppliers(String tenantId, String search, Pageable pageable) {
        return supplierRepository.searchSuppliers(tenantId, search, pageable)
                .map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(String id, String tenantId) {
        Supplier supplier = supplierRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        return mapToResponse(supplier);
    }
    
    public SupplierResponse updateSupplier(String id, SupplierRequest request, String tenantId) {
        Supplier supplier = supplierRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        
        supplier.setName(request.getName());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setAddress(request.getAddress());
        supplier.setCity(request.getCity());
        supplier.setState(request.getState());
        supplier.setPincode(request.getPincode());
        supplier.setCountry(request.getCountry());
        supplier.setGstin(request.getGstin());
        supplier.setWebsite(request.getWebsite());
        supplier.setNotes(request.getNotes());
        supplier.setBankName(request.getBankName());
        supplier.setAccountNumber(request.getAccountNumber());
        supplier.setIfscCode(request.getIfscCode());
        if (request.getCreditDays() != null) {
            supplier.setCreditDays(request.getCreditDays());
        }
        
        Supplier updated = supplierRepository.save(supplier);
        return mapToResponse(updated);
    }
    
    public void deleteSupplier(String id, String tenantId) {
        Supplier supplier = supplierRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        supplierRepository.delete(supplier);
    }
    
    // Business Logic Methods
    
    /**
     * Record a purchase from supplier
     */
    public SupplierResponse recordPurchase(String supplierId, String tenantId, BigDecimal amount, boolean isPaid) {
        log.info("Recording purchase of {} from supplier {} in tenant {}", amount, supplierId, tenantId);
        
        Supplier supplier = supplierRepository.findByIdAndTenantId(supplierId, tenantId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        
        // Update total purchases
        supplier.setTotalPurchases(supplier.getTotalPurchases().add(amount));
        supplier.setPurchaseCount(supplier.getPurchaseCount() + 1);
        supplier.setLastPurchaseDate(LocalDateTime.now());
        
        // Update outstanding balance if not paid
        if (!isPaid) {
            supplier.setOutstandingBalance(supplier.getOutstandingBalance().add(amount));
        }
        
        Supplier updated = supplierRepository.save(supplier);
        log.info("Supplier {} purchase recorded. Total purchases: {}, Outstanding: {}", 
                supplierId, updated.getTotalPurchases(), updated.getOutstandingBalance());
        
        return mapToResponse(updated);
    }
    
    /**
     * Record payment to supplier
     */
    public SupplierResponse recordPayment(String supplierId, String tenantId, BigDecimal amount) {
        log.info("Recording payment of {} to supplier {} in tenant {}", amount, supplierId, tenantId);
        
        Supplier supplier = supplierRepository.findByIdAndTenantId(supplierId, tenantId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        
        if (supplier.getOutstandingBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Payment amount exceeds outstanding balance");
        }
        
        supplier.setOutstandingBalance(supplier.getOutstandingBalance().subtract(amount));
        
        Supplier updated = supplierRepository.save(supplier);
        log.info("Payment recorded for supplier {}. New outstanding balance: {}", 
                supplierId, updated.getOutstandingBalance());
        
        return mapToResponse(updated);
    }
    
    /**
     * Calculate payment due date based on credit days
     */
    public LocalDateTime calculateDueDate(String supplierId, String tenantId) {
        Supplier supplier = supplierRepository.findByIdAndTenantId(supplierId, tenantId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        
        if (supplier.getLastPurchaseDate() == null) {
            return LocalDateTime.now().plusDays(supplier.getCreditDays());
        }
        
        return supplier.getLastPurchaseDate().plusDays(supplier.getCreditDays());
    }
    
    /**
     * Check if payment is overdue
     */
    public boolean isPaymentOverdue(String supplierId, String tenantId) {
        LocalDateTime dueDate = calculateDueDate(supplierId, tenantId);
        return LocalDateTime.now().isAfter(dueDate);
    }
    
    /**
     * Get suppliers with outstanding balance
     */
    @Transactional(readOnly = true)
    public Page<SupplierResponse> getSuppliersWithOutstanding(String tenantId, Pageable pageable) {
        // This would need a repository method
        return supplierRepository.findByTenantId(tenantId, pageable)
                .map(this::mapToResponse)
                .map(supplier -> {
                    if (supplier.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0) {
                        return supplier;
                    }
                    return null;
                });
    }
    
    /**
     * Deactivate supplier
     */
    public SupplierResponse deactivateSupplier(String supplierId, String tenantId) {
        Supplier supplier = supplierRepository.findByIdAndTenantId(supplierId, tenantId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        
        supplier.setActive(false);
        Supplier updated = supplierRepository.save(supplier);
        
        return mapToResponse(updated);
    }
    
    /**
     * Reactivate supplier
     */
    public SupplierResponse reactivateSupplier(String supplierId, String tenantId) {
        Supplier supplier = supplierRepository.findByIdAndTenantId(supplierId, tenantId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        
        supplier.setActive(true);
        Supplier updated = supplierRepository.save(supplier);
        
        return mapToResponse(updated);
    }
    
    /**
     * Update credit terms
     */
    public SupplierResponse updateCreditTerms(String supplierId, String tenantId, int creditDays) {
        log.info("Updating credit terms to {} days for supplier {} in tenant {}", creditDays, supplierId, tenantId);
        
        Supplier supplier = supplierRepository.findByIdAndTenantId(supplierId, tenantId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        
        supplier.setCreditDays(creditDays);
        Supplier updated = supplierRepository.save(supplier);
        
        return mapToResponse(updated);
    }
    
    private SupplierResponse mapToResponse(Supplier supplier) {
        SupplierResponse response = new SupplierResponse();
        response.setId(supplier.getId());
        response.setName(supplier.getName());
        response.setEmail(supplier.getEmail());
        response.setPhone(supplier.getPhone());
        response.setContactPerson(supplier.getContactPerson());
        response.setAddress(supplier.getAddress());
        response.setCity(supplier.getCity());
        response.setState(supplier.getState());
        response.setPincode(supplier.getPincode());
        response.setCountry(supplier.getCountry());
        response.setGstin(supplier.getGstin());
        response.setWebsite(supplier.getWebsite());
        response.setTotalPurchases(supplier.getTotalPurchases());
        response.setOutstandingBalance(supplier.getOutstandingBalance());
        response.setPurchaseCount(supplier.getPurchaseCount());
        response.setLastPurchaseDate(supplier.getLastPurchaseDate());
        response.setActive(supplier.getActive());
        response.setNotes(supplier.getNotes());
        response.setBankName(supplier.getBankName());
        response.setAccountNumber(supplier.getAccountNumber());
        response.setIfscCode(supplier.getIfscCode());
        response.setCreditDays(supplier.getCreditDays());
        response.setCreatedAt(supplier.getCreatedAt());
        response.setUpdatedAt(supplier.getUpdatedAt());
        return response;
    }
}
