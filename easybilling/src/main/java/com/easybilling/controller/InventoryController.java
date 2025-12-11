package com.easybilling.controller;

import com.easybilling.dto.ApiResponse;
import com.easybilling.dto.PageResponse;
import com.easybilling.dto.*;
import com.easybilling.dto.StockAdjustmentRequest;
import com.easybilling.dto.StockTransferRequest;
import com.easybilling.dto.BulkProductUpdateRequest;
import com.easybilling.dto.ProductSearchRequest;
import com.easybilling.dto.InventoryDashboardResponse;
import com.easybilling.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Inventory Management", description = "APIs for managing products, stock, categories, and brands")
public class InventoryController extends BaseController {
    private final InventoryService inventoryService;

    // Product Management
    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new product")
    public ApiResponse<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        Integer tenantId = getCurrentTenantId();
        return ApiResponse.success("Product created successfully", 
                inventoryService.createProduct(request, tenantId));
    }

    @GetMapping("/products")
    @Operation(summary = "Get all products")
    public ApiResponse<PageResponse<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Integer tenantId = getCurrentTenantId();
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(inventoryService.getProducts(tenantId, pageable));
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Get product by ID")
    public ApiResponse<ProductResponse> getProduct(@PathVariable Long id) {
        Integer tenantId = getCurrentTenantId();
        return ApiResponse.success(inventoryService.getProduct(id, tenantId));
    }

    @GetMapping("/products/barcode/{barcode}")
    @Operation(summary = "Search product by barcode")
    public ApiResponse<ProductResponse> getProductByBarcode(@PathVariable String barcode) {
        Integer tenantId = getCurrentTenantId();
        return ApiResponse.success(inventoryService.getProductByBarcode(barcode, tenantId));
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Update product")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        Integer tenantId = getCurrentTenantId();
        return ApiResponse.success("Product updated successfully", 
                inventoryService.updateProduct(id, request, tenantId));
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Delete product")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        Integer tenantId = getCurrentTenantId();
        inventoryService.deleteProduct(id, tenantId);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Product deleted successfully")
                .build();
    }

    // Category Management
    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new category")
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        Integer tenantId = getCurrentTenantId();
        return ApiResponse.success("Category created successfully", 
                inventoryService.createCategory(request, tenantId));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories")
    public ApiResponse<List<CategoryResponse>> getCategories() {
        Integer tenantId = getCurrentTenantId();
        return ApiResponse.success(inventoryService.getCategories(tenantId));
    }

    // Brand Management
    @PostMapping("/brands")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new brand")
    public ApiResponse<BrandResponse> createBrand(@Valid @RequestBody BrandRequest request) {
        Integer tenantId = getCurrentTenantId();
        return ApiResponse.success("Brand created successfully", 
                inventoryService.createBrand(request, tenantId));
    }

    @GetMapping("/brands")
    @Operation(summary = "Get all brands")
    public ApiResponse<List<BrandResponse>> getBrands() {
        Integer tenantId = getCurrentTenantId();
        return ApiResponse.success(inventoryService.getBrands(tenantId));
    }

    // Stock Management
    @GetMapping("/stock/product/{productId}")
    @Operation(summary = "Get stock levels for a product")
    public ApiResponse<List<StockResponse>> getStockForProduct(@PathVariable Long productId) {
        Integer tenantId = getCurrentTenantId();
        return ApiResponse.success(inventoryService.getStockForProduct(productId, tenantId));
    }

    @PostMapping("/stock/movements")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record stock movement")
    public ApiResponse<Void> recordStockMovement(@Valid @RequestBody StockMovementRequest request) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        inventoryService.recordStockMovement(request, tenantId, userId);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Stock movement recorded successfully")
                .build();
    }

    // Advanced Inventory Features
    @PostMapping("/stock/adjust")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adjust stock levels")
    public ApiResponse<Void> adjustStock(@Valid @RequestBody StockAdjustmentRequest request) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        inventoryService.adjustStock(request, tenantId, userId);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Stock adjusted successfully")
                .build();
    }

    @PostMapping("/stock/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Transfer stock between locations")
    public ApiResponse<Void> transferStock(@Valid @RequestBody StockTransferRequest request) {
        Integer tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        inventoryService.transferStock(request, tenantId, userId);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Stock transferred successfully")
                .build();
    }

    @PostMapping("/products/bulk-update")
    @Operation(summary = "Bulk update products")
    public ApiResponse<Void> bulkUpdateProducts(@Valid @RequestBody BulkProductUpdateRequest request) {
        Integer tenantId = getCurrentTenantId();
        inventoryService.bulkUpdateProducts(request, tenantId);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Products updated successfully")
                .build();
    }

    @PostMapping("/products/search")
    @Operation(summary = "Advanced product search")
    public ApiResponse<PageResponse<ProductResponse>> searchProducts(@Valid @RequestBody ProductSearchRequest request) {
        Integer tenantId = getCurrentTenantId();
        return ApiResponse.success(inventoryService.searchProducts(request, tenantId));
    }

    @GetMapping("/inventory/dashboard")
    @Operation(summary = "Get inventory dashboard data")
    public ApiResponse<InventoryDashboardResponse> getInventoryDashboard() {
        Integer tenantId = getCurrentTenantId();
        return ApiResponse.success(inventoryService.getInventoryDashboard(tenantId));
    }

    @GetMapping("/stock/low-stock-alerts")
    @Operation(summary = "Get low stock alerts")
    public ApiResponse<List<StockResponse>> getLowStockAlerts(
            @RequestParam(required = false) String locationId) {
        Integer tenantId = getCurrentTenantId();
        return ApiResponse.success(inventoryService.getLowStockAlerts(tenantId, locationId));
    }
}
