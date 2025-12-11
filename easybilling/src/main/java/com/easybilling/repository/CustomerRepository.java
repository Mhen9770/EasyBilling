package com.easybilling.repository;

import com.easybilling.entity.Customer;
import com.easybilling.enums.CustomerSegment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    
    Page<Customer> findByTenantId(Integer tenantId, Pageable pageable);
    
    Optional<Customer> findByIdAndTenantId(String id, Integer tenantId);
    
    Optional<Customer> findByPhoneAndTenantId(String phone, Integer tenantId);
    
    Optional<Customer> findByTenantIdAndPhone(Integer tenantId, String phone);
    
    Optional<Customer> findByTenantIdAndEmail(Integer tenantId, String email);
    
    Page<Customer> findByTenantIdAndSegment(Integer tenantId, CustomerSegment segment, Pageable pageable);
    
    Page<Customer> findByTenantIdAndActive(Integer tenantId, Boolean active, Pageable pageable);
    
    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> searchCustomers(@Param("tenantId") Integer tenantId, 
                                   @Param("search") String search, 
                                   Pageable pageable);
    
    long countByTenantId(Integer tenantId);
}
