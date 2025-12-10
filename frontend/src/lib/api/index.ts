/**
 * Centralized API exports
 * Import all APIs from this file for consistency
 */

export { default as apiClient } from './client';
export type { ApiResponse, PageResponse } from './client';

// Auth
import { authApi } from './auth/authApi';
export { authApi };
export type { LoginRequest, RegisterRequest, LoginResponse, UserInfo, UserProfile } from './auth/authApi';

// Tenant
import { tenantApi } from './tenant/tenantApi';
export { tenantApi };
export type { TenantRequest, TenantResponse } from './tenant/tenantApi';

// User Management
import { userApi } from './user/userApi';
export { userApi };
export type { UserRequest, UserResponse, UserRole, UserStatus } from './user/userApi';

// Customer
import { customerApi } from './customer/customerApi';
export { customerApi };
export type { CustomerRequest, CustomerResponse } from './customer/customerApi';

// Supplier
import { supplierApi } from './supplier/supplierApi';
export { supplierApi };
export type { SupplierRequest, SupplierResponse } from './supplier/supplierApi';

// Inventory
import { inventoryApi } from './inventory/inventoryApi';
export { inventoryApi };
export type {
  ProductRequest,
  ProductResponse,
  CategoryRequest,
  CategoryResponse,
  BrandRequest,
  BrandResponse,
  StockResponse,
  StockMovementRequest,
} from './inventory/inventoryApi';

// Reports
import { reportsApi } from './reports/reportsApi';
export { reportsApi };
export type {
  ReportType,
  ReportRequest,
  ReportResponse,
  SalesReportResponse,
  InventoryReportResponse,
} from './reports/reportsApi';

// Notifications
import { notificationApi } from './notification/notificationApi';
export { notificationApi };
export type {
  NotificationRequest,
  NotificationResponse,
  NotificationType,
  NotificationStatus,
} from './notification/notificationApi';

// Offers/Promotions
import { offersApi } from './offers/offersApi';
export { offersApi };
export type {
  OfferRequest,
  OfferResponse,
  OfferType,
  OfferStatus,
  DiscountCalculationRequest,
  DiscountCalculationResponse,
} from './offers/offersApi';

/**
 * Combined API object for convenience
 */
export const api = {
  auth: authApi,
  tenant: tenantApi,
  user: userApi,
  customer: customerApi,
  supplier: supplierApi,
  inventory: inventoryApi,
  reports: reportsApi,
  notification: notificationApi,
  offers: offersApi,
};

export default api;
