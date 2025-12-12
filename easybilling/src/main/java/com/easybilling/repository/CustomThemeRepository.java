package com.easybilling.repository;

import com.easybilling.entity.CustomTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomThemeRepository extends JpaRepository<CustomTheme, Long> {
    
    Optional<CustomTheme> findByTenantId(Integer tenantId);
    
    Optional<CustomTheme> findByTenantIdAndIsActiveTrue(Integer tenantId);
    
    boolean existsByTenantId(Integer tenantId);
}
