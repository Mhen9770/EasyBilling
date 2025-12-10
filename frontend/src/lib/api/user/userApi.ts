import apiClient from '../client';
import { ApiResponse, PageResponse } from '../types';

export type UserRole = 'ADMIN' | 'MANAGER' | 'STAFF' | 'VIEWER';
export type UserStatus = 'ACTIVE' | 'SUSPENDED' | 'INACTIVE';

export interface UserRequest {
  username: string;
  email: string;
  fullName: string;
  phone?: string;
  role: UserRole;
  password?: string;
}

export interface UserResponse {
  id: string;
  username: string;
  email: string;
  fullName: string;
  phone?: string;
  role: UserRole;
  status: UserStatus;
  lastLogin?: string;
  createdAt: string;
}

export const userApi = {
  createUser: async (data: UserRequest): Promise<ApiResponse<UserResponse>> => {
    const response = await apiClient.post<ApiResponse<UserResponse>>(
      '/auth-service/api/v1/users',
      data
    );
    return response.data;
  },

  listUsers: async (page = 0, size = 20, role?: UserRole): Promise<ApiResponse<PageResponse<UserResponse>>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<UserResponse>>>(
      '/auth-service/api/v1/users',
      { params: { page, size, role } }
    );
    return response.data;
  },

  getUser: async (id: string): Promise<ApiResponse<UserResponse>> => {
    const response = await apiClient.get<ApiResponse<UserResponse>>(
      `/auth-service/api/v1/users/${id}`
    );
    return response.data;
  },

  updateUser: async (id: string, data: Partial<UserRequest>): Promise<ApiResponse<UserResponse>> => {
    const response = await apiClient.put<ApiResponse<UserResponse>>(
      `/auth-service/api/v1/users/${id}`,
      data
    );
    return response.data;
  },

  deleteUser: async (id: string): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(
      `/auth-service/api/v1/users/${id}`
    );
    return response.data;
  },

  suspendUser: async (id: string): Promise<ApiResponse<UserResponse>> => {
    const response = await apiClient.post<ApiResponse<UserResponse>>(
      `/auth-service/api/v1/users/${id}/suspend`
    );
    return response.data;
  },

  activateUser: async (id: string): Promise<ApiResponse<UserResponse>> => {
    const response = await apiClient.post<ApiResponse<UserResponse>>(
      `/auth-service/api/v1/users/${id}/activate`
    );
    return response.data;
  },

  updateRole: async (id: string, role: UserRole): Promise<ApiResponse<UserResponse>> => {
    const response = await apiClient.put<ApiResponse<UserResponse>>(
      `/auth-service/api/v1/users/${id}/role`,
      { role }
    );
    return response.data;
  },

  getCurrentUser: async (): Promise<ApiResponse<UserResponse>> => {
    const response = await apiClient.get<ApiResponse<UserResponse>>(
      '/auth-service/api/v1/users/me'
    );
    return response.data;
  },
};
