import apiClient from '../client';
import { ApiResponse, PageResponse } from '../types';

export type UserRole = 'ADMIN' | 'STAFF';
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'LOCKED' | 'PENDING_VERIFICATION';

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  role: UserRole;
  securityGroupIds?: string[]; // Only for STAFF users
}

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
  firstName?: string;
  lastName?: string;
  phone?: string;
  tenantId: string;
  status: UserStatus;
  roles: string[];
  lastLogin?: string;
  createdAt: string;
  updatedAt?: string;
}

export const userApi = {
  createUser: async (data: CreateUserRequest): Promise<ApiResponse<UserResponse>> => {
    const response = await apiClient.post<ApiResponse<UserResponse>>(
      '/api/v1/users',
      data
    );
    return response.data;
  },

  listUsers: async (page = 0, size = 20, sortBy = 'createdAt', sortDirection = 'DESC'): Promise<ApiResponse<PageResponse<UserResponse>>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<UserResponse>>>(
      '/api/v1/users',
      { params: { page, size, sortBy, sortDirection } }
    );
    return response.data;
  },

  getUser: async (id: string): Promise<ApiResponse<UserResponse>> => {
    const response = await apiClient.get<ApiResponse<UserResponse>>(
      `/api/v1/users/${id}`
    );
    return response.data;
  },

  updateUser: async (id: string, data: Partial<UserRequest>): Promise<ApiResponse<UserResponse>> => {
    const response = await apiClient.put<ApiResponse<UserResponse>>(
      `/api/v1/users/${id}`,
      data
    );
    return response.data;
  },

  deleteUser: async (id: string): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(
      `/api/v1/users/${id}`
    );
    return response.data;
  },

  updateUserStatus: async (id: string, status: UserStatus): Promise<ApiResponse<UserResponse>> => {
    const response = await apiClient.put<ApiResponse<UserResponse>>(
      `/api/v1/users/${id}/status?status=${status}`
    );
    return response.data;
  },

  addRoleToUser: async (id: string, role: string): Promise<ApiResponse<UserResponse>> => {
    const response = await apiClient.post<ApiResponse<UserResponse>>(
      `/api/v1/users/${id}/roles/${role}`
    );
    return response.data;
  },

  removeRoleFromUser: async (id: string, role: string): Promise<ApiResponse<UserResponse>> => {
    const response = await apiClient.delete<ApiResponse<UserResponse>>(
      `/api/v1/users/${id}/roles/${role}`
    );
    return response.data;
  },

  getCurrentUser: async (): Promise<ApiResponse<UserResponse>> => {
    const response = await apiClient.get<ApiResponse<UserResponse>>(
      '/api/v1/users/me'
    );
    return response.data;
  },
};
