package com.easybilling.controller;

import com.easybilling.dto.SupplierRequest;
import com.easybilling.dto.SupplierResponse;
import com.easybilling.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Slf4j
public class SupplierController extends BaseController {
    
    private final SupplierService supplierService;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSupplier(@Valid @RequestBody SupplierRequest request) {
        String tenantId = getCurrentTenantId();
        log.info("Creating supplier for tenant: {}", tenantId);
        SupplierResponse response = supplierService.createSupplier(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createSuccessResponse(response));
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        String tenantId = getCurrentTenantId();
        log.info("Fetching suppliers for tenant: {}", tenantId);
        
        Page<SupplierResponse> suppliers;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        if (search != null && !search.isBlank()) {
            suppliers = supplierService.searchSuppliers(tenantId, search, pageRequest);
        } else {
            suppliers = supplierService.getAllSuppliers(tenantId, pageRequest);
        }
        
        return ResponseEntity.ok(createPageResponse(suppliers));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getSupplierById(@PathVariable String id) {
        String tenantId = getCurrentTenantId();
        log.info("Fetching supplier {} for tenant: {}", id, tenantId);
        SupplierResponse response = supplierService.getSupplierById(id, tenantId);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateSupplier(
            @PathVariable String id,
            @Valid @RequestBody SupplierRequest request) {
        String tenantId = getCurrentTenantId();
        log.info("Updating supplier {} for tenant: {}", id, tenantId);
        SupplierResponse response = supplierService.updateSupplier(id, request, tenantId);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSupplier(@PathVariable String id) {
        String tenantId = getCurrentTenantId();
        log.info("Deleting supplier {} for tenant: {}", id, tenantId);
        supplierService.deleteSupplier(id, tenantId);
        return ResponseEntity.ok(createSuccessResponse("Supplier deleted successfully"));
    }
    
    @PostMapping("/{id}/purchase")
    public ResponseEntity<Map<String, Object>> recordPurchase(
            @PathVariable String id,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "false") boolean isPaid) {
        String tenantId = getCurrentTenantId();
        log.info("Recording purchase of {} from supplier {} in tenant: {}", amount, id, tenantId);
        SupplierResponse response = supplierService.recordPurchase(id, tenantId, amount, isPaid);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @PostMapping("/{id}/payment")
    public ResponseEntity<Map<String, Object>> recordPayment(
            @PathVariable String id,
            @RequestParam BigDecimal amount) {
        String tenantId = getCurrentTenantId();
        log.info("Recording payment of {} to supplier {} in tenant: {}", amount, id, tenantId);
        SupplierResponse response = supplierService.recordPayment(id, tenantId, amount);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateSupplier(@PathVariable String id) {
        String tenantId = getCurrentTenantId();
        log.info("Deactivating supplier {} for tenant: {}", id, tenantId);
        SupplierResponse response = supplierService.deactivateSupplier(id, tenantId);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<Map<String, Object>> reactivateSupplier(@PathVariable String id) {
        String tenantId = getCurrentTenantId();
        log.info("Reactivating supplier {} for tenant: {}", id, tenantId);
        SupplierResponse response = supplierService.reactivateSupplier(id, tenantId);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @PutMapping("/{id}/credit-terms")
    public ResponseEntity<Map<String, Object>> updateCreditTerms(
            @PathVariable String id,
            @RequestParam int creditDays) {
        String tenantId = getCurrentTenantId();
        log.info("Updating credit terms to {} days for supplier {} in tenant: {}", creditDays, id, tenantId);
        SupplierResponse response = supplierService.updateCreditTerms(id, tenantId, creditDays);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    private Map<String, Object> createSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    private Map<String, Object> createPageResponse(Page<?> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        Map<String, Object> pageData = new HashMap<>();
        pageData.put("content", page.getContent());
        pageData.put("page", page.getNumber());
        pageData.put("size", page.getSize());
        pageData.put("totalElements", page.getTotalElements());
        pageData.put("totalPages", page.getTotalPages());
        pageData.put("first", page.isFirst());
        pageData.put("last", page.isLast());
        pageData.put("empty", page.isEmpty());
        
        response.put("data", pageData);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
