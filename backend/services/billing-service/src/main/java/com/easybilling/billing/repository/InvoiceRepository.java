package com.easybilling.billing.repository;

import com.easybilling.billing.entity.Invoice;
import com.easybilling.billing.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    
    Page<Invoice> findByTenantIdOrderByCreatedAtDesc(String tenantId, Pageable pageable);
    
    Page<Invoice> findByTenantIdAndStatusOrderByCreatedAtDesc(String tenantId, InvoiceStatus status, Pageable pageable);
    
    Optional<Invoice> findByTenantIdAndInvoiceNumber(String tenantId, String invoiceNumber);
    
    List<Invoice> findByTenantIdAndStoreIdAndCreatedAtBetween(String tenantId, String storeId, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.tenantId = :tenantId AND i.createdAt >= :startDate")
    Long countInvoicesSince(String tenantId, LocalDateTime startDate);
}
