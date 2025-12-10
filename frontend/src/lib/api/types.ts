// Re-export types from client for convenience
export type { ApiResponse, PageResponse } from './client';

// Re-export types from auth API
export type {
  LoginRequest,
  RegisterRequest,
  LoginResponse,
  UserInfo,
  UserProfile,
  UpdateProfileRequest,
  ChangePasswordRequest,
} from './auth/authApi';

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

// Re-export types from customer API
export type {
  CustomerRequest,
  CustomerResponse,
} from './customer/customerApi';

// Re-export types from supplier API
export type {
  SupplierRequest,
  SupplierResponse,
} from './supplier/supplierApi';

// Re-export types from reports API
export type {
  ReportType,
  ReportRequest,
  ReportResponse,
  SalesReportResponse,
  InventoryReportResponse,
} from './reports/reportsApi';

// Re-export types from notification API
export type {
  NotificationType,
  NotificationStatus,
  NotificationRequest,
  NotificationResponse,
} from './notification/notificationApi';

// Re-export types from offers API
export type {
  OfferType,
  OfferStatus,
  OfferRequest,
  OfferResponse,
  DiscountCalculationRequest,
  DiscountCalculationResponse,
} from './offers/offersApi';

// Re-export types from user API
export type {
  UserRole,
  UserStatus,
  UserRequest,
  UserResponse,
} from './user/userApi';

// Re-export types from tenant API
export type {
  TenantRequest,
  TenantResponse,
} from './tenant/tenantApi';
