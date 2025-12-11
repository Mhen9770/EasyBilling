package com.easybilling.repository;

import com.easybilling.entity.CustomerWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerWalletRepository extends JpaRepository<CustomerWallet, String> {
    
    Optional<CustomerWallet> findByTenantIdAndCustomerId(Integer tenantId, String customerId);
}
