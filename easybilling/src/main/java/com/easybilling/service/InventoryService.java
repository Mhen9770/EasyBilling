package com.easybilling.service;

import com.easybilling.dto.PageResponse;
import com.easybilling.exception.ResourceNotFoundException;
import com.easybilling.exception.ValidationException;
import com.easybilling.dto.*;
import com.easybilling.entity.*;
import com.easybilling.enums.MovementType;
import com.easybilling.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.easybilling.dto.StockAdjustmentRequest;
import com.easybilling.dto.StockTransferRequest;
import com.easybilling.dto.BulkProductUpdateRequest;
import com.easybilling.dto.ProductSearchRequest;
import com.easybilling.dto.InventoryDashboardResponse;

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

        return PageResponse.of(
                products,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
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

    /**
     * Check if product has sufficient stock available
     */
    public boolean checkStockAvailability(String productId, String locationId, BigDecimal quantity, String tenantId) {
        try {
            Long productIdLong = Long.parseLong(productId);
            var stockList = getStockForProduct(productIdLong, tenantId);
            
            // Find stock for the specific location
            var stock = stockList.stream()
                    .filter(s -> locationId.equals(s.getLocationId()))
                    .findFirst()
                    .orElse(null);
            
            if (stock != null) {
                BigDecimal available = stock.getAvailableQuantity();
                return available.compareTo(quantity) >= 0;
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to check stock availability for product: {}", productId, e);
            // In case of error, allow the transaction to proceed
            // This prevents blocking sales due to temporary service issues
            return true;
        }
    }

    /**
     * Deduct stock when invoice is completed
     */
    public void deductStock(String productId, String locationId, BigDecimal quantity, String referenceId, String performedBy, String tenantId) {
        try {
            Long productIdLong = Long.parseLong(productId);
            StockMovementRequest request = new StockMovementRequest();
            request.setProductId(productIdLong);
            request.setLocationId(locationId);
            request.setMovementType(MovementType.OUT);
            request.setQuantity(quantity);
            request.setReferenceId(referenceId);
            request.setReferenceType("SALE");
            request.setNotes("Sale - Invoice: " + referenceId);
            
            recordStockMovement(request, tenantId, performedBy);
            log.info("Deducted stock for product: {}, quantity: {}", productId, quantity);
        } catch (Exception e) {
            log.error("Failed to deduct stock for product: {}", productId, e);
            // Don't throw exception - the sale should complete even if stock update fails
            // This can be reconciled later through reports
        }
    }

    /**
     * Reverse stock deduction (for returns/cancellations)
     */
    public void reverseStockDeduction(String productId, String locationId, BigDecimal quantity, String referenceId, String performedBy, String tenantId) {
        try {
            Long productIdLong = Long.parseLong(productId);
            StockMovementRequest request = new StockMovementRequest();
            request.setProductId(productIdLong);
            request.setLocationId(locationId);
            request.setMovementType(MovementType.IN);
            request.setQuantity(quantity);
            request.setReferenceId(referenceId);
            request.setReferenceType("RETURN");
            request.setNotes("Return - Invoice: " + referenceId);
            
            recordStockMovement(request, tenantId, performedBy);
            log.info("Reversed stock deduction for product: {}, quantity: {}", productId, quantity);
        } catch (Exception e) {
            log.error("Failed to reverse stock deduction for product: {}", productId, e);
        }
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

    // Advanced Inventory Features
    
    @Transactional
    public void adjustStock(StockAdjustmentRequest request, String tenantId, String userId) {
        Product product = productRepository.findByIdAndTenantId(request.getProductId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

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
            return stockRepository.save(newStock);
        });

        BigDecimal previousQty = stock.getQuantity();
        BigDecimal newQty;

        switch (request.getAdjustmentType()) {
            case INCREASE:
                newQty = previousQty.add(request.getQuantity());
                break;
            case DECREASE:
                newQty = previousQty.subtract(request.getQuantity());
                if (newQty.compareTo(BigDecimal.ZERO) < 0) {
                    throw new ValidationException("Insufficient stock for adjustment");
                }
                break;
            case SET:
                newQty = request.getQuantity();
                break;
            default:
                throw new ValidationException("Invalid adjustment type");
        }

        stock.setQuantity(newQty);
        stockRepository.save(stock);

        // Record movement
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setLocationId(request.getLocationId());
        movement.setMovementType(MovementType.ADJUSTMENT);
        movement.setQuantity(request.getQuantity());
        movement.setPreviousQuantity(previousQty);
        movement.setNewQuantity(newQty);
        movement.setReferenceType("ADJUSTMENT");
        movement.setReferenceId(request.getReferenceNumber());
        movement.setNotes("Adjustment: " + request.getReason() + ". " + (request.getNotes() != null ? request.getNotes() : ""));
        movement.setPerformedBy(userId);
        movement.setTenantId(tenantId);
        stockMovementRepository.save(movement);

        log.info("Stock adjusted for product: {} at location: {} by user: {}", 
                product.getId(), request.getLocationId(), userId);
    }

    @Transactional
    public void transferStock(StockTransferRequest request, String tenantId, String userId) {
        Product product = productRepository.findByIdAndTenantId(request.getProductId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Get source stock
        Stock fromStock = stockRepository.findByProductIdAndLocationIdAndTenantId(
                request.getProductId(), request.getFromLocationId(), tenantId
        ).orElseThrow(() -> new ResourceNotFoundException("Stock not found at source location"));

        if (fromStock.getAvailableQuantity().compareTo(request.getQuantity()) < 0) {
            throw new ValidationException("Insufficient stock at source location");
        }

        // Deduct from source
        BigDecimal fromPreviousQty = fromStock.getQuantity();
        BigDecimal fromNewQty = fromPreviousQty.subtract(request.getQuantity());
        fromStock.setQuantity(fromNewQty);
        stockRepository.save(fromStock);

        // Add to destination
        Stock toStock = stockRepository.findByProductIdAndLocationIdAndTenantId(
                request.getProductId(), request.getToLocationId(), tenantId
        ).orElseGet(() -> {
            Stock newStock = new Stock();
            newStock.setProduct(product);
            newStock.setLocationId(request.getToLocationId());
            newStock.setQuantity(BigDecimal.ZERO);
            newStock.setReservedQuantity(BigDecimal.ZERO);
            newStock.setAvailableQuantity(BigDecimal.ZERO);
            newStock.setTenantId(tenantId);
            return stockRepository.save(newStock);
        });

        BigDecimal toPreviousQty = toStock.getQuantity();
        BigDecimal toNewQty = toPreviousQty.add(request.getQuantity());
        toStock.setQuantity(toNewQty);
        stockRepository.save(toStock);

        // Record movements
        StockMovement outMovement = new StockMovement();
        outMovement.setProduct(product);
        outMovement.setLocationId(request.getFromLocationId());
        outMovement.setMovementType(MovementType.TRANSFER);
        outMovement.setQuantity(request.getQuantity());
        outMovement.setPreviousQuantity(fromPreviousQty);
        outMovement.setNewQuantity(fromNewQty);
        outMovement.setReferenceType("TRANSFER");
        outMovement.setReferenceId(request.getReferenceNumber());
        outMovement.setNotes("Transfer to " + request.getToLocationId() + ". " + (request.getNotes() != null ? request.getNotes() : ""));
        outMovement.setPerformedBy(userId);
        outMovement.setTenantId(tenantId);
        stockMovementRepository.save(outMovement);

        StockMovement inMovement = new StockMovement();
        inMovement.setProduct(product);
        inMovement.setLocationId(request.getToLocationId());
        inMovement.setMovementType(MovementType.IN);
        inMovement.setQuantity(request.getQuantity());
        inMovement.setPreviousQuantity(toPreviousQty);
        inMovement.setNewQuantity(toNewQty);
        inMovement.setReferenceType("TRANSFER");
        inMovement.setReferenceId(request.getReferenceNumber());
        inMovement.setNotes("Transfer from " + request.getFromLocationId() + ". " + (request.getNotes() != null ? request.getNotes() : ""));
        inMovement.setPerformedBy(userId);
        inMovement.setTenantId(tenantId);
        stockMovementRepository.save(inMovement);

        log.info("Stock transferred for product: {} from {} to {} by user: {}", 
                product.getId(), request.getFromLocationId(), request.getToLocationId(), userId);
    }

    @Transactional
    public void bulkUpdateProducts(BulkProductUpdateRequest request, String tenantId) {
        List<Product> products = productRepository.findAllById(request.getProductIds())
                .stream()
                .filter(p -> p.getTenantId().equals(tenantId))
                .collect(Collectors.toList());

        for (Product product : products) {
            switch (request.getUpdateType()) {
                case PRICE_UPDATE:
                    if (request.getPriceChange() != null) {
                        product.setSellingPrice(product.getSellingPrice().add(request.getPriceChange()));
                    }
                    break;
                case COST_UPDATE:
                    if (request.getCostChange() != null) {
                        product.setCostPrice(product.getCostPrice().add(request.getCostChange()));
                    }
                    break;
                case THRESHOLD_UPDATE:
                    if (request.getStockThresholdChange() != null) {
                        product.setLowStockThreshold(product.getLowStockThreshold() + request.getStockThresholdChange());
                    }
                    break;
                case STATUS_UPDATE:
                    if (request.getActiveStatus() != null) {
                        product.setIsActive(request.getActiveStatus());
                    }
                    break;
                case CATEGORY_UPDATE:
                    if (request.getCategoryId() != null) {
                        Category category = categoryRepository.findByIdAndTenantId(request.getCategoryId(), tenantId)
                                .orElse(null);
                        product.setCategory(category);
                    }
                    break;
                case BRAND_UPDATE:
                    if (request.getBrandId() != null) {
                        Brand brand = brandRepository.findByIdAndTenantId(request.getBrandId(), tenantId)
                                .orElse(null);
                        product.setBrand(brand);
                    }
                    break;
            }
        }

        productRepository.saveAll(products);
        log.info("Bulk updated {} products for tenant: {}", products.size(), tenantId);
    }

    public PageResponse<ProductResponse> searchProducts(ProductSearchRequest request, String tenantId) {
        // This is a simplified search - in production, use JPA Specifications or QueryDSL
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<Product> page = productRepository.findByTenantId(tenantId, pageable);
        
        List<Product> filtered = page.getContent();
        
        // Apply filters
        if (request.getSearchTerm() != null && !request.getSearchTerm().isEmpty()) {
            String term = request.getSearchTerm().toLowerCase();
            filtered = filtered.stream()
                    .filter(p -> p.getName().toLowerCase().contains(term) ||
                            p.getSku().toLowerCase().contains(term) ||
                            p.getBarcode().toLowerCase().contains(term))
                    .collect(Collectors.toList());
        }
        
        if (request.getActiveOnly() != null && request.getActiveOnly()) {
            filtered = filtered.stream()
                    .filter(Product::getIsActive)
                    .collect(Collectors.toList());
        }
        
        if (request.getLowStockOnly() != null && request.getLowStockOnly()) {
            // This would need stock data - simplified for now
            filtered = filtered.stream()
                    .filter(p -> p.getTrackStock() && p.getLowStockThreshold() > 0)
                    .collect(Collectors.toList());
        }
        
        List<ProductResponse> products = filtered.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());

        return PageResponse.of(products, request.getPage(), request.getSize(), filtered.size());
    }

    public InventoryDashboardResponse getInventoryDashboard(String tenantId) {
        List<Product> allProducts = productRepository.findByTenantId(tenantId, Pageable.unpaged()).getContent();
        List<Stock> allStocks = stockRepository.findByTenantId(tenantId);
        
        long totalProducts = allProducts.size();
        long activeProducts = allProducts.stream().filter(Product::getIsActive).count();
        
        // Calculate low stock products
        long lowStockProducts = allProducts.stream()
                .filter(p -> {
                    if (!p.getTrackStock()) return false;
                    return allStocks.stream()
                            .filter(s -> s.getProduct().getId().equals(p.getId()))
                            .anyMatch(s -> s.getAvailableQuantity().compareTo(BigDecimal.valueOf(p.getLowStockThreshold())) < 0);
                })
                .count();
        
        // Calculate inventory value
        BigDecimal totalStockValue = allStocks.stream()
                .map(s -> s.getQuantity().multiply(s.getProduct().getCostPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalInventoryValue = allProducts.stream()
                .map(Product::getCostPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Get low stock alerts
        List<InventoryDashboardResponse.LowStockAlert> lowStockAlerts = allProducts.stream()
                .filter(p -> p.getTrackStock())
                .flatMap(p -> allStocks.stream()
                        .filter(s -> s.getProduct().getId().equals(p.getId()))
                        .filter(s -> s.getAvailableQuantity().compareTo(BigDecimal.valueOf(p.getLowStockThreshold())) < 0)
                        .map(s -> InventoryDashboardResponse.LowStockAlert.builder()
                                .productId(p.getId())
                                .productName(p.getName())
                                .sku(p.getSku())
                                .locationId(s.getLocationId())
                                .currentStock(s.getAvailableQuantity())
                                .minStockLevel(BigDecimal.valueOf(p.getLowStockThreshold()))
                                .reorderQuantity(BigDecimal.valueOf(p.getLowStockThreshold() * 2))
                                .category(p.getCategory() != null ? p.getCategory().getName() : "Uncategorized")
                                .build()))
                .limit(10)
                .collect(Collectors.toList());
        
        // Get recent movements
        List<StockMovement> recentMovements = stockMovementRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, PageRequest.of(0, 10))
                .getContent();
        
        List<InventoryDashboardResponse.RecentMovement> recentMovementList = recentMovements.stream()
                .map(m -> InventoryDashboardResponse.RecentMovement.builder()
                        .id(m.getId())
                        .productName(m.getProduct().getName())
                        .sku(m.getProduct().getSku())
                        .movementType(m.getMovementType().name())
                        .quantity(m.getQuantity())
                        .locationId(m.getLocationId())
                        .referenceType(m.getReferenceType())
                        .referenceId(m.getReferenceId())
                        .performedBy(m.getPerformedBy())
                        .createdAt(m.getCreatedAt().toString())
                        .build())
                .collect(Collectors.toList());
        
        return InventoryDashboardResponse.builder()
                .summary(InventoryDashboardResponse.InventorySummary.builder()
                        .totalProducts(totalProducts)
                        .activeProducts(activeProducts)
                        .lowStockProducts(lowStockProducts)
                        .outOfStockProducts(0L) // Would need to calculate
                        .totalStockValue(totalStockValue)
                        .totalInventoryValue(totalInventoryValue)
                        .totalLocations(1) // Would need to get from locations
                        .build())
                .lowStockAlerts(lowStockAlerts)
                .recentMovements(recentMovementList)
                .categorySummary(List.of()) // Would need to calculate
                .topProducts(List.of()) // Would need to calculate from sales
                .valuation(InventoryDashboardResponse.InventoryValuation.builder()
                        .totalCostValue(totalInventoryValue)
                        .totalSellingValue(allProducts.stream()
                                .map(Product::getSellingPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .potentialProfit(BigDecimal.ZERO) // Would need to calculate
                        .profitMargin(BigDecimal.ZERO) // Would need to calculate
                        .build())
                .build();
    }

    public List<StockResponse> getLowStockAlerts(String tenantId, String locationId) {
        List<Product> products = productRepository.findByTenantId(tenantId, Pageable.unpaged()).getContent();
        List<Stock> stocks = locationId != null 
                ? stockRepository.findByLocationIdAndTenantId(locationId, tenantId)
                : stockRepository.findByTenantId(tenantId);
        
        return products.stream()
                .filter(Product::getTrackStock)
                .flatMap(p -> stocks.stream()
                        .filter(s -> s.getProduct().getId().equals(p.getId()))
                        .filter(s -> s.getAvailableQuantity().compareTo(BigDecimal.valueOf(p.getLowStockThreshold())) < 0)
                        .map(s -> mapToStockResponse(s, p)))
                .collect(Collectors.toList());
    }
}
