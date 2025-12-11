package com.easybilling.repository;

import com.easybilling.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, String> {
    
    Page<WalletTransaction> findByTenantIdAndCustomerId(Integer tenantId, String customerId, Pageable pageable);
}
