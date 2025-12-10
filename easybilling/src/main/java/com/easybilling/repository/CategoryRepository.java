package com.easybilling.repository;

import com.easybilling.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByTenantIdAndIsActive(String tenantId, Boolean isActive);
    Optional<Category> findByIdAndTenantId(Long id, String tenantId);
}
