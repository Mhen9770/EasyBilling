package com.easybilling.customer.repository;

import com.easybilling.customer.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, String> {
    
    Page<WalletTransaction> findByTenantIdAndCustomerId(String tenantId, String customerId, Pageable pageable);
}
