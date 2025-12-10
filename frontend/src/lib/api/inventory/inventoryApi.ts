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
  locationName?: string;
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
  price?: number;
  cost?: number;
  basePrice?: number;
  sellingPrice?: number;
  taxRate: number;
  unit: string;
  lowStockThreshold?: number;
}

export interface CategoryRequest {
  name: string;
  description?: string;
  parentId?: string;
}

export interface BrandRequest {
  name: string;
  description?: string;
  website?: string;
  logoUrl?: string;
}

export interface StockMovementRequest {
  productId: string;
  movementType: 'IN' | 'OUT' | 'TRANSFER' | 'ADJUSTMENT';
  quantity: number;
  locationId: string;
  reference?: string;
  notes?: string;
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

  // Categories
  createCategory: async (data: CategoryRequest): Promise<ApiResponse<CategoryResponse>> => {
    const response = await apiClient.post<ApiResponse<CategoryResponse>>(
      '/inventory-service/api/v1/categories',
      data
    );
    return response.data;
  },

  getCategories: async (): Promise<ApiResponse<CategoryResponse[]>> => {
    return inventoryApi.listCategories();
  },

  updateCategory: async (id: string, data: CategoryRequest): Promise<ApiResponse<CategoryResponse>> => {
    const response = await apiClient.put<ApiResponse<CategoryResponse>>(
      `/inventory-service/api/v1/categories/${id}`,
      data
    );
    return response.data;
  },

  deleteCategory: async (id: string): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(
      `/inventory-service/api/v1/categories/${id}`
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

  getBrands: async (): Promise<ApiResponse<BrandResponse[]>> => {
    return inventoryApi.listBrands();
  },

  createBrand: async (data: BrandRequest): Promise<ApiResponse<BrandResponse>> => {
    const response = await apiClient.post<ApiResponse<BrandResponse>>(
      '/inventory-service/api/v1/brands',
      data
    );
    return response.data;
  },

  updateBrand: async (id: string, data: BrandRequest): Promise<ApiResponse<BrandResponse>> => {
    const response = await apiClient.put<ApiResponse<BrandResponse>>(
      `/inventory-service/api/v1/brands/${id}`,
      data
    );
    return response.data;
  },

  deleteBrand: async (id: string): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(
      `/inventory-service/api/v1/brands/${id}`
    );
    return response.data;
  },

  // Stock movements
  recordStockMovement: async (data: StockMovementRequest): Promise<ApiResponse<void>> => {
    const response = await apiClient.post<ApiResponse<void>>(
      '/inventory-service/api/v1/stock/movements',
      data
    );
    return response.data;
  },

  // Products with pagination
  getProducts: async (params: { page: number; size: number }): Promise<ApiResponse<PageResponse<ProductResponse>>> => {
    return inventoryApi.listProducts(params.page, params.size);
  },
};
