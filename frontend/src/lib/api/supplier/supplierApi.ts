import apiClient from '../client';
import { ApiResponse, PageResponse } from '../types';

export interface SupplierResponse {
  id: string;
  name: string;
  email?: string;
  phone: string;
  contactPerson?: string;
  address?: string;
  city?: string;
  state?: string;
  pincode?: string;
  country?: string;
  gstin?: string;
  totalPurchases: number;
  outstandingBalance: number;
  creditDays: number;
  bankName?: string;
  accountNumber?: string;
  ifscCode?: string;
  purchaseCount: number;
  lastPurchaseDate?: string;
  active: boolean;
  createdAt: string;
}

export interface SupplierRequest {
  name: string;
  email?: string;
  phone: string;
  contactPerson?: string;
  address?: string;
  city?: string;
  state?: string;
  pincode?: string;
  country?: string;
  gstin?: string;
  creditDays?: number;
  bankName?: string;
  accountNumber?: string;
  ifscCode?: string;
}

export const supplierApi = {
  // CRUD operations
  createSupplier: async (data: SupplierRequest): Promise<ApiResponse<SupplierResponse>> => {
    const response = await apiClient.post<ApiResponse<SupplierResponse>>(
      '/api/v1/suppliers',
      data
    );
    return response.data;
  },

  listSuppliers: async (page = 0, size = 20, search?: string): Promise<ApiResponse<PageResponse<SupplierResponse>>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<SupplierResponse>>>(
      '/api/v1/suppliers',
      { params: { page, size, search } }
    );
    return response.data;
  },

  getSupplier: async (id: string): Promise<ApiResponse<SupplierResponse>> => {
    const response = await apiClient.get<ApiResponse<SupplierResponse>>(
      `/api/v1/suppliers/${id}`
    );
    return response.data;
  },

  updateSupplier: async (id: string, data: SupplierRequest): Promise<ApiResponse<SupplierResponse>> => {
    const response = await apiClient.put<ApiResponse<SupplierResponse>>(
      `/api/v1/suppliers/${id}`,
      data
    );
    return response.data;
  },

  deleteSupplier: async (id: string): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(
      `/api/v1/suppliers/${id}`
    );
    return response.data;
  },

  // Business logic operations
  recordPurchase: async (id: string, amount: number, isPaid = false): Promise<ApiResponse<SupplierResponse>> => {
    const response = await apiClient.post<ApiResponse<SupplierResponse>>(
      `/api/v1/suppliers/${id}/purchase`,
      null,
      { params: { amount, isPaid } }
    );
    return response.data;
  },

  recordPayment: async (id: string, amount: number): Promise<ApiResponse<SupplierResponse>> => {
    const response = await apiClient.post<ApiResponse<SupplierResponse>>(
      `/api/v1/suppliers/${id}/payment`,
      null,
      { params: { amount } }
    );
    return response.data;
  },

  updateCreditTerms: async (id: string, creditDays: number): Promise<ApiResponse<SupplierResponse>> => {
    const response = await apiClient.put<ApiResponse<SupplierResponse>>(
      `/api/v1/suppliers/${id}/credit-terms`,
      null,
      { params: { creditDays } }
    );
    return response.data;
  },

  deactivateSupplier: async (id: string): Promise<ApiResponse<SupplierResponse>> => {
    const response = await apiClient.post<ApiResponse<SupplierResponse>>(
      `/api/v1/suppliers/${id}/deactivate`
    );
    return response.data;
  },

  reactivateSupplier: async (id: string): Promise<ApiResponse<SupplierResponse>> => {
    const response = await apiClient.post<ApiResponse<SupplierResponse>>(
      `/api/v1/suppliers/${id}/reactivate`
    );
    return response.data;
  },
};
