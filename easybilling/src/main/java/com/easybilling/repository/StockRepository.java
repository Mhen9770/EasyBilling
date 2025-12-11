package com.easybilling.repository;

import com.easybilling.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProductIdAndLocationIdAndTenantId(Long productId, String locationId, Integer tenantId);
    List<Stock> findByProductIdAndTenantId(Long productId, Integer tenantId);
    List<Stock> findByTenantId(Integer tenantId);
    List<Stock> findByLocationIdAndTenantId(String locationId, Integer tenantId);
}
