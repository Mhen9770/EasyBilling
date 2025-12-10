package com.easybilling.controller;

import com.easybilling.dto.CustomerRequest;
import com.easybilling.dto.CustomerResponse;
import com.easybilling.service.CustomerService;
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
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController extends BaseController {
    
    private final CustomerService customerService;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCustomer(@Valid @RequestBody CustomerRequest request) {
        String tenantId = getCurrentTenantId();
        log.info("Creating customer for tenant: {}", tenantId);
        CustomerResponse response = customerService.createCustomer(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createSuccessResponse(response));
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        String tenantId = getCurrentTenantId();
        log.info("Fetching customers for tenant: {}", tenantId);
        
        Page<CustomerResponse> customers;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        if (search != null && !search.isBlank()) {
            customers = customerService.searchCustomers(tenantId, search, pageRequest);
        } else {
            customers = customerService.getAllCustomers(tenantId, pageRequest);
        }
        
        return ResponseEntity.ok(createPageResponse(customers));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCustomerById(@PathVariable String id) {
        String tenantId = getCurrentTenantId();
        log.info("Fetching customer {} for tenant: {}", id, tenantId);
        CustomerResponse response = customerService.getCustomerById(id, tenantId);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody CustomerRequest request) {
        String tenantId = getCurrentTenantId();
        log.info("Updating customer {} for tenant: {}", id, tenantId);
        CustomerResponse response = customerService.updateCustomer(id, request, tenantId);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCustomer(@PathVariable String id) {
        String tenantId = getCurrentTenantId();
        log.info("Deleting customer {} for tenant: {}", id, tenantId);
        customerService.deleteCustomer(id, tenantId);
        return ResponseEntity.ok(createSuccessResponse("Customer deleted successfully"));
    }
    
    @GetMapping("/phone/{phone}")
    public ResponseEntity<Map<String, Object>> getCustomerByPhone(@PathVariable String phone) {
        String tenantId = getCurrentTenantId();
        log.info("Fetching customer by phone {} for tenant: {}", phone, tenantId);
        CustomerResponse response = customerService.getCustomerByPhone(phone, tenantId);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @PostMapping("/{id}/purchase")
    public ResponseEntity<Map<String, Object>> recordPurchase(
            @PathVariable String id,
            @RequestParam BigDecimal amount) {
        String tenantId = getCurrentTenantId();
        log.info("Recording purchase of {} for customer {} in tenant: {}", amount, id, tenantId);
        CustomerResponse response = customerService.recordPurchase(id, tenantId, amount);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @PostMapping("/{id}/wallet/add")
    public ResponseEntity<Map<String, Object>> addToWallet(
            @PathVariable String id,
            @RequestParam BigDecimal amount) {
        String tenantId = getCurrentTenantId();
        log.info("Adding {} to wallet for customer {} in tenant: {}", amount, id, tenantId);
        CustomerResponse response = customerService.addToWallet(id, tenantId, amount);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @PostMapping("/{id}/wallet/deduct")
    public ResponseEntity<Map<String, Object>> deductFromWallet(
            @PathVariable String id,
            @RequestParam BigDecimal amount) {
        String tenantId = getCurrentTenantId();
        log.info("Deducting {} from wallet for customer {} in tenant: {}", amount, id, tenantId);
        CustomerResponse response = customerService.deductFromWallet(id, tenantId, amount);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @PostMapping("/{id}/loyalty/redeem")
    public ResponseEntity<Map<String, Object>> redeemLoyaltyPoints(
            @PathVariable String id,
            @RequestParam int points) {
        String tenantId = getCurrentTenantId();
        log.info("Redeeming {} loyalty points for customer {} in tenant: {}", points, id, tenantId);
        CustomerResponse response = customerService.redeemLoyaltyPoints(id, tenantId, points);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateCustomer(@PathVariable String id) {
        String tenantId = getCurrentTenantId();
        log.info("Deactivating customer {} for tenant: {}", id, tenantId);
        CustomerResponse response = customerService.deactivateCustomer(id, tenantId);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<Map<String, Object>> reactivateCustomer(@PathVariable String id) {
        String tenantId = getCurrentTenantId();
        log.info("Reactivating customer {} for tenant: {}", id, tenantId);
        CustomerResponse response = customerService.reactivateCustomer(id, tenantId);
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
