package com.easybilling.controller;

import com.easybilling.dto.CustomerRequest;
import com.easybilling.dto.CustomerResponse;
import com.easybilling.dto.ApiResponse;
import com.easybilling.dto.PageResponse;
import com.easybilling.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController extends BaseController {
    
    private final CustomerService customerService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        Integer tenantId = getCurrentTenantId();
        log.info("Creating customer for tenant: {}", tenantId);
        CustomerResponse response = customerService.createCustomer(request, tenantId);
        return ApiResponse.success("Customer created successfully", response);
    }
    
    @GetMapping
    public ApiResponse<PageResponse<CustomerResponse>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        Integer tenantId = getCurrentTenantId();
        log.info("Fetching customers for tenant: {}", tenantId);
        
        Page<CustomerResponse> customers;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        if (search != null && !search.isBlank()) {
            customers = customerService.searchCustomers(tenantId, search, pageRequest);
        } else {
            customers = customerService.getAllCustomers(tenantId, pageRequest);
        }
        
        return ApiResponse.success(PageResponse.of(
            customers.getContent(),
            customers.getNumber(),
            customers.getSize(),
            customers.getTotalElements()
        ));
    }
    
    @GetMapping("/{id}")
    public ApiResponse<CustomerResponse> getCustomerById(@PathVariable String id) {
        Integer tenantId = getCurrentTenantId();
        log.info("Fetching customer {} for tenant: {}", id, tenantId);
        CustomerResponse response = customerService.getCustomerById(id, tenantId);
        return ApiResponse.success(response);
    }
    
    @PutMapping("/{id}")
    public ApiResponse<CustomerResponse> updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody CustomerRequest request) {
        Integer tenantId = getCurrentTenantId();
        log.info("Updating customer {} for tenant: {}", id, tenantId);
        CustomerResponse response = customerService.updateCustomer(id, request, tenantId);
        return ApiResponse.success("Customer updated successfully", response);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCustomer(@PathVariable String id) {
        Integer tenantId = getCurrentTenantId();
        log.info("Deleting customer {} for tenant: {}", id, tenantId);
        customerService.deleteCustomer(id, tenantId);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Customer deleted successfully")
                .build();
    }
    
    @GetMapping("/phone/{phone}")
    public ApiResponse<CustomerResponse> getCustomerByPhone(@PathVariable String phone) {
        Integer tenantId = getCurrentTenantId();
        log.info("Fetching customer by phone {} for tenant: {}", phone, tenantId);
        CustomerResponse response = customerService.getCustomerByPhone(phone, tenantId);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/{id}/purchase")
    public ApiResponse<CustomerResponse> recordPurchase(
            @PathVariable String id,
            @RequestParam BigDecimal amount) {
        Integer tenantId = getCurrentTenantId();
        log.info("Recording purchase of {} for customer {} in tenant: {}", amount, id, tenantId);
        CustomerResponse response = customerService.recordPurchase(id, tenantId, amount);
        return ApiResponse.success("Purchase recorded successfully", response);
    }
    
    @PostMapping("/{id}/wallet/add")
    public ApiResponse<CustomerResponse> addToWallet(
            @PathVariable String id,
            @RequestParam BigDecimal amount) {
        Integer tenantId = getCurrentTenantId();
        log.info("Adding {} to wallet for customer {} in tenant: {}", amount, id, tenantId);
        CustomerResponse response = customerService.addToWallet(id, tenantId, amount);
        return ApiResponse.success("Amount added to wallet successfully", response);
    }
    
    @PostMapping("/{id}/wallet/deduct")
    public ApiResponse<CustomerResponse> deductFromWallet(
            @PathVariable String id,
            @RequestParam BigDecimal amount) {
        Integer tenantId = getCurrentTenantId();
        log.info("Deducting {} from wallet for customer {} in tenant: {}", amount, id, tenantId);
        CustomerResponse response = customerService.deductFromWallet(id, tenantId, amount);
        return ApiResponse.success("Amount deducted from wallet successfully", response);
    }
    
    @PostMapping("/{id}/loyalty/redeem")
    public ApiResponse<CustomerResponse> redeemLoyaltyPoints(
            @PathVariable String id,
            @RequestParam int points) {
        Integer tenantId = getCurrentTenantId();
        log.info("Redeeming {} loyalty points for customer {} in tenant: {}", points, id, tenantId);
        CustomerResponse response = customerService.redeemLoyaltyPoints(id, tenantId, points);
        return ApiResponse.success("Loyalty points redeemed successfully", response);
    }
    
    @PostMapping("/{id}/deactivate")
    public ApiResponse<CustomerResponse> deactivateCustomer(@PathVariable String id) {
        Integer tenantId = getCurrentTenantId();
        log.info("Deactivating customer {} for tenant: {}", id, tenantId);
        CustomerResponse response = customerService.deactivateCustomer(id, tenantId);
        return ApiResponse.success("Customer deactivated successfully", response);
    }
    
    @PostMapping("/{id}/reactivate")
    public ApiResponse<CustomerResponse> reactivateCustomer(@PathVariable String id) {
        Integer tenantId = getCurrentTenantId();
        log.info("Reactivating customer {} for tenant: {}", id, tenantId);
        CustomerResponse response = customerService.reactivateCustomer(id, tenantId);
        return ApiResponse.success("Customer reactivated successfully", response);
    }
}
