package com.easybilling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDashboardResponse {
    private InventorySummary summary;
    private List<LowStockAlert> lowStockAlerts;
    private List<RecentMovement> recentMovements;
    private List<CategorySummary> categorySummary;
    private List<TopProduct> topProducts;
    private InventoryValuation valuation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventorySummary {
        private Long totalProducts;
        private Long activeProducts;
        private Long lowStockProducts;
        private Long outOfStockProducts;
        private BigDecimal totalStockValue;
        private BigDecimal totalInventoryValue;
        private Integer totalLocations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockAlert {
        private Long productId;
        private String productName;
        private String sku;
        private String locationId;
        private BigDecimal currentStock;
        private BigDecimal minStockLevel;
        private BigDecimal reorderQuantity;
        private String category;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentMovement {
        private Long id;
        private String productName;
        private String sku;
        private String movementType;
        private BigDecimal quantity;
        private String locationId;
        private String referenceType;
        private String referenceId;
        private String performedBy;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummary {
        private Long categoryId;
        private String categoryName;
        private Long productCount;
        private BigDecimal totalValue;
        private BigDecimal totalStock;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private Long productId;
        private String productName;
        private String sku;
        private BigDecimal totalSales;
        private BigDecimal totalQuantity;
        private BigDecimal stockValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryValuation {
        private BigDecimal totalCostValue;
        private BigDecimal totalSellingValue;
        private BigDecimal potentialProfit;
        private BigDecimal profitMargin;
    }
}

