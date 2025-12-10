import apiClient from '../client';
import { ApiResponse } from '../types';

export interface TenantRequest {
  name: string;
  email: string;
  phone: string;
  businessName: string;
  businessType?: string;
  gstin?: string;
  address?: string;
  city?: string;
  state?: string;
  pincode?: string;
  country?: string;
  adminUserName: string;
  adminUserEmail: string;
  adminUserPassword: string;
}

export interface TenantResponse {
  id: string;
  tenantId: string;
  name: string;
  email: string;
  phone: string;
  businessName: string;
  businessType?: string;
  gstin?: string;
  address?: string;
  city?: string;
  state?: string;
  pincode?: string;
  country?: string;
  status: 'ACTIVE' | 'SUSPENDED' | 'INACTIVE';
  subscriptionPlan?: string;
  subscriptionExpiry?: string;
  createdAt: string;
}

export const tenantApi = {
  createTenant: async (data: TenantRequest): Promise<ApiResponse<TenantResponse>> => {
    const response = await apiClient.post<ApiResponse<TenantResponse>>(
      '/api/v1/tenants',
      data
    );
    return response.data;
  },

  getTenant: async (id: string): Promise<ApiResponse<TenantResponse>> => {
    const response = await apiClient.get<ApiResponse<TenantResponse>>(
      `/api/v1/tenants/${id}`
    );
    return response.data;
  },

  updateTenant: async (id: string, data: Partial<TenantRequest>): Promise<ApiResponse<TenantResponse>> => {
    const response = await apiClient.put<ApiResponse<TenantResponse>>(
      `/api/v1/tenants/${id}`,
      data
    );
    return response.data;
  },

  getCurrentTenant: async (): Promise<ApiResponse<TenantResponse>> => {
    const response = await apiClient.get<ApiResponse<TenantResponse>>(
      '/api/v1/tenants/current'
    );
    return response.data;
  },
};
