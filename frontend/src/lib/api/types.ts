// Re-export types from client for convenience
export type { ApiResponse, PageResponse } from './client';

// Re-export types from inventory API
export type {
  ProductRequest,
  CategoryRequest,
  BrandRequest,
  StockMovementRequest,
  ProductResponse,
  CategoryResponse,
  BrandResponse,
  StockResponse,
} from './inventory/inventoryApi';
