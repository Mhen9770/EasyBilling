package com.easybilling.repository;

import com.easybilling.entity.LoyaltyTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, String> {
    
    Page<LoyaltyTransaction> findByTenantIdAndCustomerId(String tenantId, String customerId, Pageable pageable);
}
