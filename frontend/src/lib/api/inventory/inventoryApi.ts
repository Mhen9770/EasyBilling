import apiClient from '../client';
import { ApiResponse, PageResponse } from '../types';

export interface ProductResponse {
  id: string | number;
  name: string;
  description?: string;
  sku: string;
  barcode?: string;
  category?: any;
  brand?: any;
  costPrice?: number;
  sellingPrice: number;
  mrp?: number;
  taxRate: number;
  unit: string;
  isActive?: boolean;
  trackStock?: boolean;
  lowStockThreshold?: number;
  imageUrl?: string;
  createdAt?: string;
  updatedAt?: string;
  // Legacy fields for backward compatibility
  basePrice?: number;
  active?: boolean;
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
  costPrice: number;
  sellingPrice: number;
  mrp: number;
  taxRate?: number;
  unit: string;
  trackStock?: boolean;
  lowStockThreshold?: number;
  imageUrl?: string;
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
      '/api/v1/products',
      data
    );
    return response.data;
  },

  listProducts: async (page = 0, size = 20): Promise<ApiResponse<PageResponse<ProductResponse>>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<ProductResponse>>>(
      '/api/v1/products',
      { params: { page, size } }
    );
    return response.data;
  },

  getProduct: async (id: string): Promise<ApiResponse<ProductResponse>> => {
    const response = await apiClient.get<ApiResponse<ProductResponse>>(
      `/api/v1/products/${id}`
    );
    return response.data;
  },

  findByBarcode: async (barcode: string): Promise<ApiResponse<ProductResponse>> => {
    const response = await apiClient.get<ApiResponse<ProductResponse>>(
      `/api/v1/products/barcode/${barcode}`
    );
    return response.data;
  },

  updateProduct: async (id: string, data: ProductRequest): Promise<ApiResponse<ProductResponse>> => {
    const response = await apiClient.put<ApiResponse<ProductResponse>>(
      `/api/v1/products/${id}`,
      data
    );
    return response.data;
  },

  deleteProduct: async (id: string): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(
      `/api/v1/products/${id}`
    );
    return response.data;
  },

  // Stock management
  getStock: async (productId: string): Promise<ApiResponse<StockResponse[]>> => {
    const response = await apiClient.get<ApiResponse<StockResponse[]>>(
      `/api/v1/stock/product/${productId}`
    );
    return response.data;
  },

  // Categories
  listCategories: async (): Promise<ApiResponse<CategoryResponse[]>> => {
    const response = await apiClient.get<ApiResponse<CategoryResponse[]>>(
      '/api/v1/categories'
    );
    return response.data;
  },

  // Categories
  createCategory: async (data: CategoryRequest): Promise<ApiResponse<CategoryResponse>> => {
    const response = await apiClient.post<ApiResponse<CategoryResponse>>(
      '/api/v1/categories',
      data
    );
    return response.data;
  },

  getCategories: async (): Promise<ApiResponse<CategoryResponse[]>> => {
    return inventoryApi.listCategories();
  },

  updateCategory: async (id: string, data: CategoryRequest): Promise<ApiResponse<CategoryResponse>> => {
    const response = await apiClient.put<ApiResponse<CategoryResponse>>(
      `/api/v1/categories/${id}`,
      data
    );
    return response.data;
  },

  deleteCategory: async (id: string): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(
      `/api/v1/categories/${id}`
    );
    return response.data;
  },

  // Brands
  listBrands: async (): Promise<ApiResponse<BrandResponse[]>> => {
    const response = await apiClient.get<ApiResponse<BrandResponse[]>>(
      '/api/v1/brands'
    );
    return response.data;
  },

  getBrands: async (): Promise<ApiResponse<BrandResponse[]>> => {
    return inventoryApi.listBrands();
  },

  createBrand: async (data: BrandRequest): Promise<ApiResponse<BrandResponse>> => {
    const response = await apiClient.post<ApiResponse<BrandResponse>>(
      '/api/v1/brands',
      data
    );
    return response.data;
  },

  updateBrand: async (id: string, data: BrandRequest): Promise<ApiResponse<BrandResponse>> => {
    const response = await apiClient.put<ApiResponse<BrandResponse>>(
      `/api/v1/brands/${id}`,
      data
    );
    return response.data;
  },

  deleteBrand: async (id: string): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(
      `/api/v1/brands/${id}`
    );
    return response.data;
  },

  // Stock movements
  recordStockMovement: async (data: StockMovementRequest): Promise<ApiResponse<void>> => {
    const response = await apiClient.post<ApiResponse<void>>(
      '/api/v1/stock/movements',
      data
    );
    return response.data;
  },

  // Products with pagination
  getProducts: async (params: { page: number; size: number }): Promise<ApiResponse<PageResponse<ProductResponse>>> => {
    return inventoryApi.listProducts(params.page, params.size);
  },

  // Advanced features
  getStockSummary: async (): Promise<ApiResponse<any>> => {
    const response = await apiClient.get<ApiResponse<any>>('/api/v1/inventory/dashboard');
    return response.data;
  },

  getLowStockAlerts: async (locationId?: string): Promise<ApiResponse<StockResponse[]>> => {
    const response = await apiClient.get<ApiResponse<StockResponse[]>>(
      '/api/v1/stock/low-stock-alerts',
      { params: locationId ? { locationId } : {} }
    );
    return response.data;
  },

  // Bulk operations
  bulkUpdateProducts: async (updates: Array<{ id: string; updates: Partial<ProductRequest> }>): Promise<ApiResponse<ProductResponse[]>> => {
    const response = await apiClient.post<ApiResponse<ProductResponse[]>>(
      '/api/v1/products/bulk-update',
      updates
    );
    return response.data;
  },

  searchProducts: async (filters: {
    name?: string;
    category?: string;
    brand?: string;
    minPrice?: number;
    maxPrice?: number;
    inStock?: boolean;
  }): Promise<ApiResponse<PageResponse<ProductResponse>>> => {
    const response = await apiClient.post<ApiResponse<PageResponse<ProductResponse>>>(
      '/api/v1/products/search',
      filters
    );
    return response.data;
  },

  // Stock adjustment
  adjustStock: async (
    productId: string,
    locationId: string,
    quantity: number,
    reason: string,
    notes?: string
  ): Promise<ApiResponse<void>> => {
    const response = await apiClient.post<ApiResponse<void>>(
      '/api/v1/stock/adjust',
      { productId, locationId, quantity, reason, notes }
    );
    return response.data;
  },

  // Stock transfer
  transferStock: async (
    productId: string,
    fromLocationId: string,
    toLocationId: string,
    quantity: number,
    notes?: string
  ): Promise<ApiResponse<void>> => {
    const response = await apiClient.post<ApiResponse<void>>(
      '/api/v1/stock/transfer',
      { productId, fromLocationId, toLocationId, quantity, notes }
    );
    return response.data;
  },
};
