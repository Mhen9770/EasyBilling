package com.easybilling.repository;

import com.easybilling.entity.CustomerVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerVisitRepository extends JpaRepository<CustomerVisit, String> {
    
    long countByTenantIdAndCustomerId(Integer tenantId, String customerId);
}
