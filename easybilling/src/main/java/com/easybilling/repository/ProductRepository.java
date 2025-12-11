package com.easybilling.repository;

import com.easybilling.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByTenantId(Integer tenantId, Pageable pageable);
    Optional<Product> findByIdAndTenantId(Long id, Integer tenantId);
    Optional<Product> findByBarcodeAndTenantId(String barcode, Integer tenantId);
    Optional<Product> findBySkuAndTenantId(String sku, Integer tenantId);
    boolean existsByBarcodeAndTenantId(String barcode, Integer tenantId);
}
