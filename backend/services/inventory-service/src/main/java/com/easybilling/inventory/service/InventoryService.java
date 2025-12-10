package com.easybilling.inventory.service;

import com.easybilling.common.dto.PageResponse;
import com.easybilling.common.exception.ResourceNotFoundException;
import com.easybilling.common.exception.ValidationException;
import com.easybilling.inventory.dto.*;
import com.easybilling.inventory.entity.*;
import com.easybilling.inventory.enums.MovementType;
import com.easybilling.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;

    // Product Management
    @Transactional
    public ProductResponse createProduct(ProductRequest request, String tenantId) {
        // Check if barcode already exists
        if (productRepository.existsByBarcodeAndTenantId(request.getBarcode(), tenantId)) {
            throw new ValidationException("Product with barcode " + request.getBarcode() + " already exists");
        }

        Product product = new Product();
        product.setSku(generateSKU());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBarcode(request.getBarcode());
        product.setCostPrice(request.getCostPrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setMrp(request.getMrp());
        product.setTaxRate(request.getTaxRate());
        product.setUnit(request.getUnit());
        product.setTrackStock(request.getTrackStock());
        product.setLowStockThreshold(request.getLowStockThreshold());
        product.setImageUrl(request.getImageUrl());
        product.setTenantId(tenantId);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndTenantId(request.getCategoryId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }

        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findByIdAndTenantId(request.getBrandId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
            product.setBrand(brand);
        }

        product = productRepository.save(product);
        log.info("Product created: {} for tenant: {}", product.getId(), tenantId);

        return mapToProductResponse(product);
    }

    public PageResponse<ProductResponse> getProducts(String tenantId, Pageable pageable) {
        Page<Product> page = productRepository.findByTenantId(tenantId, pageable);
        List<ProductResponse> products = page.getContent().stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                products,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public ProductResponse getProduct(Long id, String tenantId) {
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return mapToProductResponse(product);
    }

    public ProductResponse getProductByBarcode(String barcode, String tenantId) {
        Product product = productRepository.findByBarcodeAndTenantId(barcode, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with barcode: " + barcode));
        return mapToProductResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request, String tenantId) {
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBarcode(request.getBarcode());
        product.setCostPrice(request.getCostPrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setMrp(request.getMrp());
        product.setTaxRate(request.getTaxRate());
        product.setUnit(request.getUnit());
        product.setTrackStock(request.getTrackStock());
        product.setLowStockThreshold(request.getLowStockThreshold());
        product.setImageUrl(request.getImageUrl());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndTenantId(request.getCategoryId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }

        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findByIdAndTenantId(request.getBrandId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
            product.setBrand(brand);
        }

        product = productRepository.save(product);
        return mapToProductResponse(product);
    }

    @Transactional
    public void deleteProduct(Long id, String tenantId) {
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setIsActive(false);
        productRepository.save(product);
    }

    // Category Management
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, String tenantId) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setTenantId(tenantId);

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findByIdAndTenantId(request.getParentId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            category.setParent(parent);
        }

        category = categoryRepository.save(category);
        return mapToCategoryResponse(category);
    }

    public List<CategoryResponse> getCategories(String tenantId) {
        return categoryRepository.findByTenantIdAndIsActive(tenantId, true).stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    // Brand Management
    @Transactional
    public BrandResponse createBrand(BrandRequest request, String tenantId) {
        Brand brand = new Brand();
        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setLogoUrl(request.getLogoUrl());
        brand.setTenantId(tenantId);

        brand = brandRepository.save(brand);
        return mapToBrandResponse(brand);
    }

    public List<BrandResponse> getBrands(String tenantId) {
        return brandRepository.findByTenantIdAndIsActive(tenantId, true).stream()
                .map(this::mapToBrandResponse)
                .collect(Collectors.toList());
    }

    // Stock Management
    public List<StockResponse> getStockForProduct(Long productId, String tenantId) {
        Product product = productRepository.findByIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return stockRepository.findByProductIdAndTenantId(productId, tenantId).stream()
                .map(stock -> mapToStockResponse(stock, product))
                .collect(Collectors.toList());
    }

    @Transactional
    public void recordStockMovement(StockMovementRequest request, String tenantId, String userId) {
        Product product = productRepository.findByIdAndTenantId(request.getProductId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Get or create stock record
        Stock stock = stockRepository.findByProductIdAndLocationIdAndTenantId(
                request.getProductId(), request.getLocationId(), tenantId
        ).orElseGet(() -> {
            Stock newStock = new Stock();
            newStock.setProduct(product);
            newStock.setLocationId(request.getLocationId());
            newStock.setQuantity(BigDecimal.ZERO);
            newStock.setReservedQuantity(BigDecimal.ZERO);
            newStock.setAvailableQuantity(BigDecimal.ZERO);
            newStock.setTenantId(tenantId);
            return newStock;
        });

        BigDecimal previousQty = stock.getQuantity();
        BigDecimal newQty;

        // Apply movement
        switch (request.getMovementType()) {
            case IN:
            case ADJUSTMENT:
                newQty = previousQty.add(request.getQuantity());
                break;
            case OUT:
                newQty = previousQty.subtract(request.getQuantity());
                if (newQty.compareTo(BigDecimal.ZERO) < 0) {
                    throw new ValidationException("Insufficient stock");
                }
                break;
            case TRANSFER:
                // Handle transfer logic
                newQty = previousQty.subtract(request.getQuantity());
                break;
            default:
                throw new ValidationException("Invalid movement type");
        }

        stock.setQuantity(newQty);
        stockRepository.save(stock);

        // Record movement
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setLocationId(request.getLocationId());
        movement.setMovementType(request.getMovementType());
        movement.setQuantity(request.getQuantity());
        movement.setPreviousQuantity(previousQty);
        movement.setNewQuantity(newQty);
        movement.setReferenceType(request.getReferenceType());
        movement.setReferenceId(request.getReferenceId());
        movement.setNotes(request.getNotes());
        movement.setPerformedBy(userId);
        movement.setTenantId(tenantId);

        stockMovementRepository.save(movement);
        log.info("Stock movement recorded for product: {} at location: {}", product.getId(), request.getLocationId());
    }

    // Helper methods
    private String generateSKU() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%05d", (int) (Math.random() * 100000));
        return "SKU-" + datePart + "-" + randomPart;
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .barcode(product.getBarcode())
                .category(product.getCategory() != null ? mapToCategoryResponse(product.getCategory()) : null)
                .brand(product.getBrand() != null ? mapToBrandResponse(product.getBrand()) : null)
                .costPrice(product.getCostPrice())
                .sellingPrice(product.getSellingPrice())
                .mrp(product.getMrp())
                .taxRate(product.getTaxRate())
                .unit(product.getUnit())
                .isActive(product.getIsActive())
                .trackStock(product.getTrackStock())
                .lowStockThreshold(product.getLowStockThreshold())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .build();
    }

    private BrandResponse mapToBrandResponse(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .logoUrl(brand.getLogoUrl())
                .isActive(brand.getIsActive())
                .createdAt(brand.getCreatedAt())
                .build();
    }

    private StockResponse mapToStockResponse(Stock stock, Product product) {
        boolean isLowStock = product.getTrackStock() && 
                stock.getAvailableQuantity().compareTo(BigDecimal.valueOf(product.getLowStockThreshold())) < 0;

        return StockResponse.builder()
                .id(stock.getId())
                .productId(product.getId())
                .productName(product.getName())
                .locationId(stock.getLocationId())
                .quantity(stock.getQuantity())
                .reservedQuantity(stock.getReservedQuantity())
                .availableQuantity(stock.getAvailableQuantity())
                .isLowStock(isLowStock)
                .build();
    }
}
