package com.easybilling.repository;

import com.easybilling.entity.HeldInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HeldInvoiceRepository extends JpaRepository<HeldInvoice, String> {
    
    List<HeldInvoice> findByTenantIdOrderByHeldAtDesc(String tenantId);
    
    List<HeldInvoice> findByTenantIdAndStoreIdOrderByHeldAtDesc(String tenantId, String storeId);
    
    Optional<HeldInvoice> findByTenantIdAndHoldReference(String tenantId, String holdReference);
}
