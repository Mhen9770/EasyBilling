package com.easybilling.repository;

import com.easybilling.entity.GstRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for GST Rate operations.
 */
@Repository
public interface GstRateRepository extends JpaRepository<GstRate, String> {
    
    /**
     * Find GST rate by HSN code.
     */
    Optional<GstRate> findByHsnCodeAndIsActiveTrue(String hsnCode);
    
    /**
     * Find GST rate by SAC code.
     */
    Optional<GstRate> findBySacCodeAndIsActiveTrue(String sacCode);
    
    /**
     * Find GST rate by tax category.
     */
    Optional<GstRate> findByTaxCategoryAndIsActiveTrue(String taxCategory);
    
    /**
     * Find all active GST rates.
     */
    List<GstRate> findByIsActiveTrue();
    
    /**
     * Find GST rate valid for a specific date.
     */
    @Query("SELECT g FROM GstRate g WHERE g.isActive = true " +
           "AND g.effectiveFrom <= :date " +
           "AND (g.effectiveTo IS NULL OR g.effectiveTo >= :date)")
    List<GstRate> findValidRatesForDate(@Param("date") LocalDate date);
    
    /**
     * Find GST rate by HSN code valid for a specific date.
     */
    @Query("SELECT g FROM GstRate g WHERE g.hsnCode = :hsnCode " +
           "AND g.isActive = true " +
           "AND g.effectiveFrom <= :date " +
           "AND (g.effectiveTo IS NULL OR g.effectiveTo >= :date)")
    Optional<GstRate> findByHsnCodeAndDate(@Param("hsnCode") String hsnCode, @Param("date") LocalDate date);
}
