package com.easybilling.customer.service;

import com.easybilling.customer.dto.CustomerRequest;
import com.easybilling.customer.dto.CustomerResponse;
import com.easybilling.customer.entity.Customer;
import com.easybilling.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    
    public CustomerResponse createCustomer(CustomerRequest request, String tenantId) {
        log.info("Creating customer for tenant: {}", tenantId);
        
        Customer customer = Customer.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .notes(request.getNotes())
                .build();
        
        Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAllCustomers(String tenantId, Pageable pageable) {
        return customerRepository.findByTenantId(tenantId, pageable)
                .map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<CustomerResponse> searchCustomers(String tenantId, String search, Pageable pageable) {
        return customerRepository.searchCustomers(tenantId, search, pageable)
                .map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(String id, String tenantId) {
        Customer customer = customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return mapToResponse(customer);
    }
    
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByPhone(String phone, String tenantId) {
        Customer customer = customerRepository.findByPhoneAndTenantId(phone, tenantId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return mapToResponse(customer);
    }
    
    public CustomerResponse updateCustomer(String id, CustomerRequest request, String tenantId) {
        Customer customer = customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setPincode(request.getPincode());
        customer.setNotes(request.getNotes());
        
        Customer updated = customerRepository.save(customer);
        return mapToResponse(updated);
    }
    
    public void deleteCustomer(String id, String tenantId) {
        Customer customer = customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customerRepository.delete(customer);
    }
    
    private CustomerResponse mapToResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());
        response.setDateOfBirth(customer.getDateOfBirth());
        response.setAddress(customer.getAddress());
        response.setCity(customer.getCity());
        response.setState(customer.getState());
        response.setPincode(customer.getPincode());
        response.setSegment(customer.getSegment());
        response.setLoyaltyPoints(customer.getLoyaltyPoints());
        response.setWalletBalance(customer.getWalletBalance());
        response.setTotalSpent(customer.getTotalSpent());
        response.setVisitCount(customer.getVisitCount());
        response.setLastVisitDate(customer.getLastVisitDate());
        response.setActive(customer.getActive());
        response.setNotes(customer.getNotes());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        return response;
    }
}
