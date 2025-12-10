import apiClient from '../client';
import { ApiResponse, PageResponse } from '../types';

export interface ProductResponse {
  id: string;
  name: string;
  description?: string;
  sku: string;
  barcode?: string;
  categoryId?: string;
  categoryName?: string;
  brandId?: string;
  brandName?: string;
  basePrice: number;
  sellingPrice: number;
  taxRate: number;
  unit: string;
  lowStockThreshold: number;
  active: boolean;
  createdAt: string;
}

export interface StockResponse {
  productId: string;
  productName: string;
  locationId?: string;
  totalQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  lowStockThreshold: number;
  isLowStock: boolean;
}

export interface CategoryResponse {
  id: string;
  name: string;
  description?: string;
  parentId?: string;
  active: boolean;
}

export interface BrandResponse {
  id: string;
  name: string;
  description?: string;
  logo?: string;
  active: boolean;
}

export interface ProductRequest {
  name: string;
  description?: string;
  sku?: string;
  barcode?: string;
  categoryId?: string;
  brandId?: string;
  basePrice: number;
  sellingPrice: number;
  taxRate: number;
  unit: string;
  lowStockThreshold?: number;
}

export const inventoryApi = {
  // Product management
  createProduct: async (data: ProductRequest): Promise<ApiResponse<ProductResponse>> => {
    const response = await apiClient.post<ApiResponse<ProductResponse>>(
      '/inventory-service/api/v1/products',
      data
    );
    return response.data;
  },

  listProducts: async (page = 0, size = 20): Promise<ApiResponse<PageResponse<ProductResponse>>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<ProductResponse>>>(
      '/inventory-service/api/v1/products',
      { params: { page, size } }
    );
    return response.data;
  },

  getProduct: async (id: string): Promise<ApiResponse<ProductResponse>> => {
    const response = await apiClient.get<ApiResponse<ProductResponse>>(
      `/inventory-service/api/v1/products/${id}`
    );
    return response.data;
  },

  findByBarcode: async (barcode: string): Promise<ApiResponse<ProductResponse>> => {
    const response = await apiClient.get<ApiResponse<ProductResponse>>(
      `/inventory-service/api/v1/products/barcode/${barcode}`
    );
    return response.data;
  },

  updateProduct: async (id: string, data: ProductRequest): Promise<ApiResponse<ProductResponse>> => {
    const response = await apiClient.put<ApiResponse<ProductResponse>>(
      `/inventory-service/api/v1/products/${id}`,
      data
    );
    return response.data;
  },

  deleteProduct: async (id: string): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(
      `/inventory-service/api/v1/products/${id}`
    );
    return response.data;
  },

  // Stock management
  getStock: async (productId: string): Promise<ApiResponse<StockResponse>> => {
    const response = await apiClient.get<ApiResponse<StockResponse>>(
      `/inventory-service/api/v1/stock/product/${productId}`
    );
    return response.data;
  },

  // Categories
  listCategories: async (): Promise<ApiResponse<CategoryResponse[]>> => {
    const response = await apiClient.get<ApiResponse<CategoryResponse[]>>(
      '/inventory-service/api/v1/categories'
    );
    return response.data;
  },

  // Brands
  listBrands: async (): Promise<ApiResponse<BrandResponse[]>> => {
    const response = await apiClient.get<ApiResponse<BrandResponse[]>>(
      '/inventory-service/api/v1/brands'
    );
    return response.data;
  },
};
