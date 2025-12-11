package com.easybilling.repository;

import com.easybilling.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, String> {
    
    Page<Supplier> findByTenantId(Integer tenantId, Pageable pageable);
    
    Optional<Supplier> findByIdAndTenantId(String id, Integer tenantId);
    
    Optional<Supplier> findByPhoneAndTenantId(String phone, Integer tenantId);
    
    Optional<Supplier> findByGstinAndTenantId(String gstin, Integer tenantId);
    
    Page<Supplier> findByTenantIdAndActive(Integer tenantId, Boolean active, Pageable pageable);
    
    @Query("SELECT s FROM Supplier s WHERE s.tenantId = :tenantId AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.gstin) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Supplier> searchSuppliers(@Param("tenantId") Integer tenantId, 
                                   @Param("search") String search, 
                                   Pageable pageable);
    
    long countByTenantId(Integer tenantId);
}
