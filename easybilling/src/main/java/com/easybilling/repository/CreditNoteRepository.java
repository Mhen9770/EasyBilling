package com.easybilling.repository;

import com.easybilling.entity.CreditNote;
import com.easybilling.enums.CreditNoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditNoteRepository extends JpaRepository<CreditNote, String> {

    Page<CreditNote> findByTenantIdAndStatus(Integer tenantId, CreditNoteStatus status, Pageable pageable);

    List<CreditNote> findByInvoiceIdAndTenantId(String invoiceId, Integer tenantId);

    Page<CreditNote> findByTenantIdAndCustomerId(Integer tenantId, String customerId, Pageable pageable);

    Optional<CreditNote> findByCreditNoteNumberAndTenantId(String creditNoteNumber, Integer tenantId);

    Optional<CreditNote> findByIdAndTenantId(String id, Integer tenantId);

    @Query("SELECT SUM(c.totalAmount) FROM CreditNote c WHERE c.tenantId = :tenantId " +
           "AND c.status = :status AND c.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalAmountByStatusAndDateRange(@Param("tenantId") Integer tenantId,
                                                    @Param("status") CreditNoteStatus status,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(c) FROM CreditNote c WHERE c.tenantId = :tenantId " +
           "AND c.requiresApproval = true AND c.status = :status")
    Long countPendingApprovals(@Param("tenantId") Integer tenantId,
                                 @Param("status") CreditNoteStatus status);

    @Query("SELECT c FROM CreditNote c WHERE c.tenantId = :tenantId " +
           "AND c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    List<CreditNote> findByDateRange(@Param("tenantId") Integer tenantId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
}
