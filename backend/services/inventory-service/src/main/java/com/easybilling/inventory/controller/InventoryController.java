package com.easybilling.inventory.controller;

import com.easybilling.common.dto.ApiResponse;
import com.easybilling.common.dto.PageResponse;
import com.easybilling.inventory.dto.*;
import com.easybilling.inventory.service.InventoryService;
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
public class InventoryController {
    private final InventoryService inventoryService;

    // Product Management
    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new product")
    public ApiResponse<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        return ApiResponse.success("Product created successfully", 
                inventoryService.createProduct(request, tenantId));
    }

    @GetMapping("/products")
    @Operation(summary = "Get all products")
    public ApiResponse<PageResponse<ProductResponse>> getProducts(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(inventoryService.getProducts(tenantId, pageable));
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Get product by ID")
    public ApiResponse<ProductResponse> getProduct(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        return ApiResponse.success(inventoryService.getProduct(id, tenantId));
    }

    @GetMapping("/products/barcode/{barcode}")
    @Operation(summary = "Search product by barcode")
    public ApiResponse<ProductResponse> getProductByBarcode(
            @PathVariable String barcode,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        return ApiResponse.success(inventoryService.getProductByBarcode(barcode, tenantId));
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Update product")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        return ApiResponse.success("Product updated successfully", 
                inventoryService.updateProduct(id, request, tenantId));
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Delete product")
    public ApiResponse<Void> deleteProduct(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        inventoryService.deleteProduct(id, tenantId);
        return ApiResponse.success("Product deleted successfully");
    }

    // Category Management
    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new category")
    public ApiResponse<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        return ApiResponse.success("Category created successfully", 
                inventoryService.createCategory(request, tenantId));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories")
    public ApiResponse<List<CategoryResponse>> getCategories(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        return ApiResponse.success(inventoryService.getCategories(tenantId));
    }

    // Brand Management
    @PostMapping("/brands")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new brand")
    public ApiResponse<BrandResponse> createBrand(
            @Valid @RequestBody BrandRequest request,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        return ApiResponse.success("Brand created successfully", 
                inventoryService.createBrand(request, tenantId));
    }

    @GetMapping("/brands")
    @Operation(summary = "Get all brands")
    public ApiResponse<List<BrandResponse>> getBrands(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        return ApiResponse.success(inventoryService.getBrands(tenantId));
    }

    // Stock Management
    @GetMapping("/stock/product/{productId}")
    @Operation(summary = "Get stock levels for a product")
    public ApiResponse<List<StockResponse>> getStockForProduct(
            @PathVariable Long productId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        return ApiResponse.success(inventoryService.getStockForProduct(productId, tenantId));
    }

    @PostMapping("/stock/movement")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record stock movement")
    public ApiResponse<Void> recordStockMovement(
            @Valid @RequestBody StockMovementRequest request,
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-User-Id") String userId) {
        inventoryService.recordStockMovement(request, tenantId, userId);
        return ApiResponse.success("Stock movement recorded successfully");
    }
}
