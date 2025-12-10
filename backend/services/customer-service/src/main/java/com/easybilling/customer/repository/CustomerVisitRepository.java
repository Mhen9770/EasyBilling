package com.easybilling.customer.repository;

import com.easybilling.customer.entity.CustomerVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerVisitRepository extends JpaRepository<CustomerVisit, String> {
    
    long countByTenantIdAndCustomerId(String tenantId, String customerId);
}
