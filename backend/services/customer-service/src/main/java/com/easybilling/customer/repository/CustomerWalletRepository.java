package com.easybilling.customer.repository;

import com.easybilling.customer.entity.CustomerWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerWalletRepository extends JpaRepository<CustomerWallet, String> {
    
    Optional<CustomerWallet> findByTenantIdAndCustomerId(String tenantId, String customerId);
}
