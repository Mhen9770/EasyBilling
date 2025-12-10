package com.easybilling.inventory.repository;

import com.easybilling.inventory.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByTenantId(String tenantId, Pageable pageable);
    Optional<Product> findByIdAndTenantId(Long id, String tenantId);
    Optional<Product> findByBarcodeAndTenantId(String barcode, String tenantId);
    Optional<Product> findBySkuAndTenantId(String sku, String tenantId);
    boolean existsByBarcodeAndTenantId(String barcode, String tenantId);
}
