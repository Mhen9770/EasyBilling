package com.easybilling.repository;

import com.easybilling.entity.Quote;
import com.easybilling.enums.QuoteStatus;
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
public interface QuoteRepository extends JpaRepository<Quote, String> {

    Page<Quote> findByTenantIdAndStatus(Integer tenantId, QuoteStatus status, Pageable pageable);

    Page<Quote> findByTenantIdAndCustomerId(Integer tenantId, String customerId, Pageable pageable);

    Optional<Quote> findByQuoteNumberAndTenantId(String quoteNumber, Integer tenantId);

    Optional<Quote> findByIdAndTenantId(String id, Integer tenantId);

    @Query("SELECT q FROM Quote q WHERE q.tenantId = :tenantId AND q.status = :status " +
           "AND q.validUntil < :date")
    List<Quote> findExpiredQuotes(@Param("tenantId") Integer tenantId,
                                    @Param("status") QuoteStatus status,
                                    @Param("date") LocalDate date);

    @Query("SELECT COUNT(q) FROM Quote q WHERE q.tenantId = :tenantId AND q.status = :status")
    Long countByTenantIdAndStatus(@Param("tenantId") Integer tenantId,
                                    @Param("status") QuoteStatus status);

    @Query("SELECT q FROM Quote q WHERE q.tenantId = :tenantId AND " +
           "q.quoteDate BETWEEN :startDate AND :endDate ORDER BY q.quoteDate DESC")
    List<Quote> findByDateRange(@Param("tenantId") Integer tenantId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);
}
