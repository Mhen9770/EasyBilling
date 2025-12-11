package com.easybilling.service;

import com.easybilling.dto.CustomerRequest;
import com.easybilling.dto.CustomerResponse;
import com.easybilling.entity.Customer;
import com.easybilling.enums.CustomerSegment;
import com.easybilling.repository.CustomerRepository;
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
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    
    // Business Logic Constants
    private static final BigDecimal VIP_THRESHOLD = new BigDecimal("50000");
    private static final BigDecimal PREMIUM_THRESHOLD = new BigDecimal("100000");
    private static final int LOYALTY_POINTS_PER_100 = 1; // 1 point per 100 currency spent
    
    public CustomerResponse createCustomer(CustomerRequest request, Integer tenantId) {
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
    public Page<CustomerResponse> getAllCustomers(Integer tenantId, Pageable pageable) {
        return customerRepository.findByTenantId(tenantId, pageable)
                .map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<CustomerResponse> searchCustomers(Integer tenantId, String search, Pageable pageable) {
        return customerRepository.searchCustomers(tenantId, search, pageable)
                .map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(String id, Integer tenantId) {
        Customer customer = customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return mapToResponse(customer);
    }
    
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByPhone(String phone, Integer tenantId) {
        Customer customer = customerRepository.findByPhoneAndTenantId(phone, tenantId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return mapToResponse(customer);
    }
    
    public CustomerResponse updateCustomer(String id, CustomerRequest request, Integer tenantId) {
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
    
    public void deleteCustomer(String id, Integer tenantId) {
        Customer customer = customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customerRepository.delete(customer);
    }
    
    // Business Logic Methods
    
    /**
     * Record a purchase and update customer metrics
     */
    public CustomerResponse recordPurchase(String customerId, Integer tenantId, BigDecimal amount) {
        log.info("Recording purchase of {} for customer {} in tenant {}", amount, customerId, tenantId);
        
        Customer customer = customerRepository.findByIdAndTenantId(customerId, tenantId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // Update total spent
        customer.setTotalSpent(customer.getTotalSpent().add(amount));
        
        // Calculate and add loyalty points
        int pointsEarned = calculateLoyaltyPoints(amount);
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + pointsEarned);
        
        // Update visit tracking
        customer.setVisitCount(customer.getVisitCount() + 1);
        customer.setLastVisitDate(LocalDateTime.now());
        
        // Auto-upgrade customer segment based on spending
        updateCustomerSegment(customer);
        
        Customer updated = customerRepository.save(customer);
        log.info("Customer {} earned {} loyalty points. New total: {}", 
                customerId, pointsEarned, updated.getLoyaltyPoints());
        
        return mapToResponse(updated);
    }
    
    /**
     * Add money to customer wallet
     */
    public CustomerResponse addToWallet(String customerId, Integer tenantId, BigDecimal amount) {
        log.info("Adding {} to wallet for customer {} in tenant {}", amount, customerId, tenantId);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be positive");
        }
        
        Customer customer = customerRepository.findByIdAndTenantId(customerId, tenantId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        customer.setWalletBalance(customer.getWalletBalance().add(amount));
        
        Customer updated = customerRepository.save(customer);
        return mapToResponse(updated);
    }
    
    /**
     * Deduct money from customer wallet
     */
    public CustomerResponse deductFromWallet(String customerId, Integer tenantId, BigDecimal amount) {
        log.info("Deducting {} from wallet for customer {} in tenant {}", amount, customerId, tenantId);
        
        Customer customer = customerRepository.findByIdAndTenantId(customerId, tenantId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (customer.getWalletBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient wallet balance");
        }
        
        customer.setWalletBalance(customer.getWalletBalance().subtract(amount));
        
        Customer updated = customerRepository.save(customer);
        return mapToResponse(updated);
    }
    
    /**
     * Redeem loyalty points
     */
    public CustomerResponse redeemLoyaltyPoints(String customerId, Integer tenantId, int points) {
        log.info("Redeeming {} loyalty points for customer {} in tenant {}", points, customerId, tenantId);
        
        Customer customer = customerRepository.findByIdAndTenantId(customerId, tenantId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (customer.getLoyaltyPoints() < points) {
            throw new RuntimeException("Insufficient loyalty points");
        }
        
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() - points);
        
        // Convert points to wallet balance (e.g., 100 points = 1 currency unit)
        BigDecimal walletCredit = new BigDecimal(points).divide(new BigDecimal("100"));
        customer.setWalletBalance(customer.getWalletBalance().add(walletCredit));
        
        Customer updated = customerRepository.save(customer);
        log.info("Redeemed {} points to {} wallet credit for customer {}", 
                points, walletCredit, customerId);
        
        return mapToResponse(updated);
    }
    
    /**
     * Get customer statistics
     */
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerStatistics(String customerId, Integer tenantId) {
        Customer customer = customerRepository.findByIdAndTenantId(customerId, tenantId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        CustomerResponse response = mapToResponse(customer);
        
        // Calculate average order value
        if (customer.getVisitCount() > 0) {
            BigDecimal avgOrderValue = customer.getTotalSpent()
                    .divide(new BigDecimal(customer.getVisitCount()), 2, BigDecimal.ROUND_HALF_UP);
            // Add to response if needed
        }
        
        return response;
    }
    
    /**
     * Get customers by segment
     */
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getCustomersBySegment(Integer tenantId, CustomerSegment segment, Pageable pageable) {
        return customerRepository.findByTenantIdAndSegment(tenantId, segment, pageable)
                .map(this::mapToResponse);
    }
    
    /**
     * Deactivate customer
     */
    public CustomerResponse deactivateCustomer(String customerId, Integer tenantId) {
        Customer customer = customerRepository.findByIdAndTenantId(customerId, tenantId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        customer.setActive(false);
        Customer updated = customerRepository.save(customer);
        
        return mapToResponse(updated);
    }
    
    /**
     * Reactivate customer
     */
    public CustomerResponse reactivateCustomer(String customerId, Integer tenantId) {
        Customer customer = customerRepository.findByIdAndTenantId(customerId, tenantId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        customer.setActive(true);
        Customer updated = customerRepository.save(customer);
        
        return mapToResponse(updated);
    }
    
    // Private helper methods
    
    private int calculateLoyaltyPoints(BigDecimal amount) {
        // 1 point for every 100 currency units spent
        return amount.divide(new BigDecimal("100"), 0, BigDecimal.ROUND_DOWN).intValue() * LOYALTY_POINTS_PER_100;
    }
    
    private void updateCustomerSegment(Customer customer) {
        BigDecimal totalSpent = customer.getTotalSpent();
        
        if (totalSpent.compareTo(PREMIUM_THRESHOLD) >= 0) {
            customer.setSegment(CustomerSegment.PREMIUM);
            log.info("Customer {} upgraded to PREMIUM segment", customer.getId());
        } else if (totalSpent.compareTo(VIP_THRESHOLD) >= 0) {
            customer.setSegment(CustomerSegment.VIP);
            log.info("Customer {} upgraded to VIP segment", customer.getId());
        } else {
            customer.setSegment(CustomerSegment.REGULAR);
        }
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
