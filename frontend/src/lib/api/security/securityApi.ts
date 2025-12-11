import apiClient from '../client';
import { ApiResponse } from '../types';

// Permission enum matching backend
export type Permission =
  // User Management
  | 'USER_CREATE' | 'USER_READ' | 'USER_UPDATE' | 'USER_DELETE' | 'USER_LIST'
  // Product Management
  | 'PRODUCT_CREATE' | 'PRODUCT_READ' | 'PRODUCT_UPDATE' | 'PRODUCT_DELETE' | 'PRODUCT_LIST'
  // Inventory Management
  | 'INVENTORY_CREATE' | 'INVENTORY_READ' | 'INVENTORY_UPDATE' | 'INVENTORY_DELETE' | 'INVENTORY_LIST' | 'STOCK_ADJUSTMENT'
  // Customer Management
  | 'CUSTOMER_CREATE' | 'CUSTOMER_READ' | 'CUSTOMER_UPDATE' | 'CUSTOMER_DELETE' | 'CUSTOMER_LIST'
  // Invoice/Billing Management
  | 'INVOICE_CREATE' | 'INVOICE_READ' | 'INVOICE_UPDATE' | 'INVOICE_DELETE' | 'INVOICE_LIST' | 'INVOICE_VOID'
  // Payment Management
  | 'PAYMENT_CREATE' | 'PAYMENT_READ' | 'PAYMENT_LIST' | 'PAYMENT_REFUND'
  // Reports
  | 'REPORT_SALES' | 'REPORT_INVENTORY' | 'REPORT_CUSTOMER' | 'REPORT_FINANCIAL'
  // Offers & Promotions
  | 'OFFER_CREATE' | 'OFFER_READ' | 'OFFER_UPDATE' | 'OFFER_DELETE' | 'OFFER_LIST'
  // Supplier Management
  | 'SUPPLIER_CREATE' | 'SUPPLIER_READ' | 'SUPPLIER_UPDATE' | 'SUPPLIER_DELETE' | 'SUPPLIER_LIST'
  // Notifications
  | 'NOTIFICATION_CREATE' | 'NOTIFICATION_READ' | 'NOTIFICATION_DELETE'
  // Settings & Configuration
  | 'SETTINGS_VIEW' | 'SETTINGS_UPDATE'
  // Security Group Management
  | 'SECURITY_GROUP_CREATE' | 'SECURITY_GROUP_READ' | 'SECURITY_GROUP_UPDATE' | 'SECURITY_GROUP_DELETE' | 'SECURITY_GROUP_LIST';

export interface SecurityGroupRequest {
  name: string;
  description?: string;
  permissions: Permission[];
  isActive?: boolean;
}

export interface SecurityGroupResponse {
  id: string;
  name: string;
  description?: string;
  tenantId: string;
  permissions: Permission[];
  isActive: boolean;
  createdAt: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
  userCount: number;
}

export interface AssignSecurityGroupRequest {
  securityGroupIds: string[];
}

export const securityApi = {
  /**
   * Create a new security group
   */
  createSecurityGroup: async (data: SecurityGroupRequest): Promise<ApiResponse<SecurityGroupResponse>> => {
    const response = await apiClient.post<ApiResponse<SecurityGroupResponse>>(
      '/api/v1/security-groups',
      data
    );
    return response.data;
  },

  /**
   * Update an existing security group
   */
  updateSecurityGroup: async (
    id: string,
    data: SecurityGroupRequest
  ): Promise<ApiResponse<SecurityGroupResponse>> => {
    const response = await apiClient.put<ApiResponse<SecurityGroupResponse>>(
      `/api/v1/security-groups/${id}`,
      data
    );
    return response.data;
  },

  /**
   * Get a security group by ID
   */
  getSecurityGroup: async (id: string): Promise<ApiResponse<SecurityGroupResponse>> => {
    const response = await apiClient.get<ApiResponse<SecurityGroupResponse>>(
      `/api/v1/security-groups/${id}`
    );
    return response.data;
  },

  /**
   * Get all security groups for current tenant
   */
  listSecurityGroups: async (): Promise<ApiResponse<SecurityGroupResponse[]>> => {
    const response = await apiClient.get<ApiResponse<SecurityGroupResponse[]>>(
      '/api/v1/security-groups'
    );
    return response.data;
  },

  /**
   * Get active security groups for current tenant
   */
  listActiveSecurityGroups: async (): Promise<ApiResponse<SecurityGroupResponse[]>> => {
    const response = await apiClient.get<ApiResponse<SecurityGroupResponse[]>>(
      '/api/v1/security-groups/active'
    );
    return response.data;
  },

  /**
   * Delete a security group
   */
  deleteSecurityGroup: async (id: string): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(
      `/api/v1/security-groups/${id}`
    );
    return response.data;
  },

  /**
   * Assign security groups to a user
   */
  assignSecurityGroups: async (
    userId: string,
    data: AssignSecurityGroupRequest
  ): Promise<ApiResponse<void>> => {
    const response = await apiClient.post<ApiResponse<void>>(
      `/api/v1/security-groups/users/${userId}/assign`,
      data
    );
    return response.data;
  },

  /**
   * Get security groups assigned to a user
   */
  getUserSecurityGroups: async (userId: string): Promise<ApiResponse<SecurityGroupResponse[]>> => {
    const response = await apiClient.get<ApiResponse<SecurityGroupResponse[]>>(
      `/api/v1/security-groups/users/${userId}`
    );
    return response.data;
  },

  /**
   * Get all available permissions
   */
  getAllPermissions: async (): Promise<ApiResponse<Permission[]>> => {
    const response = await apiClient.get<ApiResponse<Permission[]>>(
      '/api/v1/security-groups/permissions'
    );
    return response.data;
  },
};
