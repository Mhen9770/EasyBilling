import apiClient from '../client';
import { ApiResponse } from '../types';

export type ReportType = 'SALES' | 'INVENTORY' | 'CUSTOMER' | 'TAX' | 'PROFIT_LOSS' | 'PERFORMANCE';

export interface ReportRequest {
  reportType: ReportType;
  startDate: string;
  endDate: string;
  filters?: Record<string, any>;
}

export interface SalesReportResponse {
  reportType: 'SALES';
  period: {
    startDate: string;
    endDate: string;
  };
  summary: {
    totalSales: number;
    totalOrders: number;
    averageOrderValue: number;
    totalProfit: number;
    profitMargin: number;
  };
  dailySales: Array<{
    date: string;
    sales: number;
    orders: number;
  }>;
  categoryBreakdown: Array<{
    categoryName: string;
    sales: number;
    percentage: number;
  }>;
  topProducts: Array<{
    productName: string;
    quantitySold: number;
    revenue: number;
  }>;
}

export interface InventoryReportResponse {
  reportType: 'INVENTORY';
  generatedAt: string;
  summary: {
    totalProducts: number;
    totalStockValue: number;
    lowStockItems: number;
    outOfStockItems: number;
  };
  stockLevels: Array<{
    productName: string;
    sku: string;
    currentStock: number;
    lowStockThreshold: number;
    stockValue: number;
    status: 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';
  }>;
  lowStockAlerts: Array<{
    productName: string;
    currentStock: number;
    threshold: number;
  }>;
}

export type ReportResponse = SalesReportResponse | InventoryReportResponse | Record<string, any>;

export const reportsApi = {
  generateReport: async (request: ReportRequest): Promise<ApiResponse<ReportResponse>> => {
    const response = await apiClient.post<ApiResponse<ReportResponse>>(
      '/reports-service/api/v1/reports/generate',
      request
    );
    return response.data;
  },

  getReportTypes: async (): Promise<ApiResponse<ReportType[]>> => {
    const response = await apiClient.get<ApiResponse<ReportType[]>>(
      '/reports-service/api/v1/reports/types'
    );
    return response.data;
  },

  // Convenience methods for specific reports
  generateSalesReport: async (startDate: string, endDate: string): Promise<ApiResponse<SalesReportResponse>> => {
    return reportsApi.generateReport({
      reportType: 'SALES',
      startDate,
      endDate,
    }) as Promise<ApiResponse<SalesReportResponse>>;
  },

  generateInventoryReport: async (): Promise<ApiResponse<InventoryReportResponse>> => {
    const today = new Date().toISOString().split('T')[0];
    return reportsApi.generateReport({
      reportType: 'INVENTORY',
      startDate: today,
      endDate: today,
    }) as Promise<ApiResponse<InventoryReportResponse>>;
  },
};
