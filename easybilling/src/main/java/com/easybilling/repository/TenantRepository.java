package com.easybilling.repository;

import com.easybilling.entity.Tenant;
import com.easybilling.entity.Tenant.TenantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Tenant entity.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {
    
    Optional<Tenant> findBySlug(String slug);
    
    boolean existsBySlug(String slug);
    
    Page<Tenant> findByStatus(TenantStatus status, Pageable pageable);
    
    Page<Tenant> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
