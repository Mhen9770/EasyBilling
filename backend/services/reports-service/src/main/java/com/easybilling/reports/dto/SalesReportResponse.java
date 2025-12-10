package com.easybilling.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalSales;
    private BigDecimal totalCost;
    private BigDecimal grossProfit;
    private BigDecimal totalTax;
    private BigDecimal netProfit;
    private Integer totalInvoices;
    private Integer totalCustomers;
    private BigDecimal averageOrderValue;
    private List<DailySales> dailySales;
    private List<CategorySales> categorySales;
    private List<TopProduct> topProducts;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySales {
        private LocalDate date;
        private BigDecimal sales;
        private Integer invoices;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySales {
        private String categoryName;
        private BigDecimal sales;
        private Integer quantity;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private String productName;
        private BigDecimal revenue;
        private Integer quantitySold;
    }
}
