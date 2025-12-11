import { apiClient, ApiResponse } from '../client';

// Auth API Types
export interface LoginRequest {
  username: string;
  password: string;
  tenantId?: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  tenantId?: number; // Optional for onboarding, required for regular registration
}

export interface OnboardRequest {
  // Tenant information
  tenantName: string;
  businessName: string;
  businessType?: string;
  tenantEmail: string;
  tenantPhone: string;
  // Admin user information
  adminUsername: string;
  adminEmail: string;
  adminPassword: string;
  adminFirstName?: string;
  adminLastName?: string;
  adminPhone?: string;
  // Optional business details
  address?: string;
  city?: string;
  state?: string;
  pincode?: string;
  country?: string;
  gstin?: string;
}

export interface UserInfo {
  id: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  tenantId: string;
  roles: string[];
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserInfo;
}

export interface UserProfile {
  id: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  tenantId: string;
  status: string;
  roles: string[];
  lastLogin?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface UpdateProfileRequest {
  email?: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

/**
 * Auth API service
 */
export const authApi = {
  /**
   * Login user
   */
  async login(data: LoginRequest): Promise<LoginResponse> {
    const response = await apiClient.post<ApiResponse<LoginResponse>>(
      '/api/v1/auth/login',
      data
    );
    return response.data.data!;
  },

  /**
   * Tenant self-service onboarding
   * Creates a new tenant and admin user in one step
   */
  async onboard(data: OnboardRequest): Promise<LoginResponse> {
    const response = await apiClient.post<ApiResponse<LoginResponse>>(
      '/api/v1/auth/onboard',
      data
    );
    return response.data.data!;
  },

  /**
   * Register new user (requires authentication - admin only)
   * Adds a user to an existing tenant
   */
  async register(data: RegisterRequest): Promise<LoginResponse> {
    const response = await apiClient.post<ApiResponse<LoginResponse>>(
      '/api/v1/auth/register',
      data
    );
    return response.data.data!;
  },

  /**
   * Refresh access token
   */
  async refreshToken(refreshToken: string): Promise<LoginResponse> {
    const response = await apiClient.post<ApiResponse<LoginResponse>>(
      '/api/v1/auth/refresh',
      { refreshToken }
    );
    return response.data.data!;
  },

  /**
   * Logout user
   */
  async logout(): Promise<void> {
    await apiClient.post('/api/v1/auth/logout');
  },

  /**
   * Get current user profile
   */
  async getCurrentUser(userId: string): Promise<UserProfile> {
    const response = await apiClient.get<ApiResponse<UserProfile>>(
      '/api/v1/users/me',
      {
        headers: { 'X-User-Id': userId }
      }
    );
    return response.data.data!;
  },

  /**
   * Update current user profile
   */
  async updateProfile(userId: string, data: UpdateProfileRequest): Promise<UserProfile> {
    const response = await apiClient.put<ApiResponse<UserProfile>>(
      '/api/v1/users/me',
      data,
      {
        headers: { 'X-User-Id': userId }
      }
    );
    return response.data.data!;
  },

  /**
   * Change password
   */
  async changePassword(userId: string, data: ChangePasswordRequest): Promise<void> {
    await apiClient.post(
      '/api/v1/users/me/change-password',
      data,
      {
        headers: { 'X-User-Id': userId }
      }
    );
  },

  /**
   * Request password reset
   */
  async requestPasswordReset(email: string): Promise<void> {
    await apiClient.post('/api/v1/users/reset-password', { email });
  },
};
