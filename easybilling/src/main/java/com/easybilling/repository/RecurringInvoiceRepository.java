package com.easybilling.repository;

import com.easybilling.entity.RecurringInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringInvoiceRepository extends JpaRepository<RecurringInvoice, String> {

    Page<RecurringInvoice> findByTenantIdAndIsActive(Integer tenantId, Boolean isActive, Pageable pageable);

    Page<RecurringInvoice> findByTenantIdAndCustomerId(Integer tenantId, String customerId, Pageable pageable);

    @Query("SELECT r FROM RecurringInvoice r WHERE r.tenantId = :tenantId AND r.isActive = true " +
           "AND r.nextInvoiceDate <= :date ORDER BY r.nextInvoiceDate ASC")
    List<RecurringInvoice> findDueRecurringInvoices(@Param("tenantId") Integer tenantId,
                                                      @Param("date") LocalDate date);

    @Query("SELECT r FROM RecurringInvoice r WHERE r.isActive = true " +
           "AND r.nextInvoiceDate <= :date ORDER BY r.nextInvoiceDate ASC")
    List<RecurringInvoice> findAllDueRecurringInvoices(@Param("date") LocalDate date);

    @Query("SELECT COUNT(r) FROM RecurringInvoice r WHERE r.tenantId = :tenantId AND r.isActive = true")
    Long countActiveByTenantId(@Param("tenantId") Integer tenantId);

    Optional<RecurringInvoice> findByIdAndTenantId(String id, Integer tenantId);
}
