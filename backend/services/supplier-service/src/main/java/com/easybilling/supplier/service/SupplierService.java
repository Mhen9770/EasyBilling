package com.easybilling.supplier.service;

import com.easybilling.supplier.dto.SupplierRequest;
import com.easybilling.supplier.dto.SupplierResponse;
import com.easybilling.supplier.entity.Supplier;
import com.easybilling.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
