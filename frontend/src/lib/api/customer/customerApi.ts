import apiClient from '../client';
import { ApiResponse, PageResponse } from '../types';

export interface CustomerResponse {
  id: string;
  name: string;
  phone: string;
  email?: string;
  address?: string;
  city?: string;
  state?: string;
  pincode?: string;
  gstin?: string;
  totalPurchases: number;
  segment: 'REGULAR' | 'VIP' | 'PREMIUM';
  walletBalance: number;
  loyaltyPoints: number;
  visitCount: number;
  lastVisitDate?: string;
  active: boolean;
  createdAt: string;
}

export interface CustomerRequest {
  name: string;
  phone: string;
  email?: string;
  address?: string;
  city?: string;
  state?: string;
  pincode?: string;
  gstin?: string;
}

export const customerApi = {
  // CRUD operations
  createCustomer: async (data: CustomerRequest): Promise<ApiResponse<CustomerResponse>> => {
    const response = await apiClient.post<ApiResponse<CustomerResponse>>(
      '/api/v1/customers',
      data
    );
    return response.data;
  },

  listCustomers: async (page = 0, size = 20, search?: string): Promise<ApiResponse<PageResponse<CustomerResponse>>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<CustomerResponse>>>(
      '/api/v1/customers',
      { params: { page, size, search } }
    );
    return response.data;
  },

  getCustomer: async (id: string): Promise<ApiResponse<CustomerResponse>> => {
    const response = await apiClient.get<ApiResponse<CustomerResponse>>(
      `/api/v1/customers/${id}`
    );
    return response.data;
  },

  findByPhone: async (phone: string): Promise<ApiResponse<CustomerResponse>> => {
    const response = await apiClient.get<ApiResponse<CustomerResponse>>(
      `/api/v1/customers/phone/${phone}`
    );
    return response.data;
  },

  updateCustomer: async (id: string, data: CustomerRequest): Promise<ApiResponse<CustomerResponse>> => {
    const response = await apiClient.put<ApiResponse<CustomerResponse>>(
      `/api/v1/customers/${id}`,
      data
    );
    return response.data;
  },

  deleteCustomer: async (id: string): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(
      `/api/v1/customers/${id}`
    );
    return response.data;
  },

  // Business logic operations
  recordPurchase: async (id: string, amount: number): Promise<ApiResponse<CustomerResponse>> => {
    const response = await apiClient.post<ApiResponse<CustomerResponse>>(
      `/api/v1/customers/${id}/purchase`,
      null,
      { params: { amount } }
    );
    return response.data;
  },

  addToWallet: async (id: string, amount: number): Promise<ApiResponse<CustomerResponse>> => {
    const response = await apiClient.post<ApiResponse<CustomerResponse>>(
      `/api/v1/customers/${id}/wallet/add`,
      null,
      { params: { amount } }
    );
    return response.data;
  },

  deductFromWallet: async (id: string, amount: number): Promise<ApiResponse<CustomerResponse>> => {
    const response = await apiClient.post<ApiResponse<CustomerResponse>>(
      `/api/v1/customers/${id}/wallet/deduct`,
      null,
      { params: { amount } }
    );
    return response.data;
  },

  redeemLoyaltyPoints: async (id: string, points: number): Promise<ApiResponse<CustomerResponse>> => {
    const response = await apiClient.post<ApiResponse<CustomerResponse>>(
      `/api/v1/customers/${id}/loyalty/redeem`,
      null,
      { params: { points } }
    );
    return response.data;
  },

  deactivateCustomer: async (id: string): Promise<ApiResponse<CustomerResponse>> => {
    const response = await apiClient.post<ApiResponse<CustomerResponse>>(
      `/api/v1/customers/${id}/deactivate`
    );
    return response.data;
  },

  reactivateCustomer: async (id: string): Promise<ApiResponse<CustomerResponse>> => {
    const response = await apiClient.post<ApiResponse<CustomerResponse>>(
      `/api/v1/customers/${id}/reactivate`
    );
    return response.data;
  },
};
