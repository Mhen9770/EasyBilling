package com.easybilling.repository;

import com.easybilling.entity.Offer;
import com.easybilling.enums.OfferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, String> {
    
    Page<Offer> findByTenantId(Integer tenantId, Pageable pageable);
    
    Optional<Offer> findByIdAndTenantId(String id, Integer tenantId);
    
    Page<Offer> findByTenantIdAndStatus(Integer tenantId, OfferStatus status, Pageable pageable);
    
    @Query("SELECT o FROM Offer o WHERE o.tenantId = :tenantId " +
           "AND o.status = 'ACTIVE' " +
           "AND o.validFrom <= :now " +
           "AND o.validTo >= :now")
    List<Offer> findActiveOffers(@Param("tenantId") Integer tenantId, 
                                  @Param("now") LocalDateTime now);
    
    long countByTenantIdAndStatus(Integer tenantId, OfferStatus status);
}
