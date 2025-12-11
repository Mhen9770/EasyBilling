package com.easybilling.repository;

import com.easybilling.entity.HeldInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HeldInvoiceRepository extends JpaRepository<HeldInvoice, String> {
    
    List<HeldInvoice> findByTenantIdOrderByHeldAtDesc(Integer tenantId);
    
    List<HeldInvoice> findByTenantIdAndStoreIdOrderByHeldAtDesc(Integer tenantId, String storeId);
    
    Optional<HeldInvoice> findByTenantIdAndHoldReference(Integer tenantId, String holdReference);
}
