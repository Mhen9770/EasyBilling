package com.easybilling.inventory.repository;

import com.easybilling.inventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByProductIdAndTenantIdOrderByCreatedAtDesc(Long productId, String tenantId);
}
