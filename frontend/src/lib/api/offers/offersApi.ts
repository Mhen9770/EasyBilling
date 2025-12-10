import apiClient from '../client';
import { ApiResponse, PageResponse } from '../types';

export type OfferType = 'PERCENTAGE' | 'FIXED_AMOUNT' | 'BUY_X_GET_Y' | 'BUNDLE' | 'MINIMUM_PURCHASE' | 'CASHBACK';
export type OfferStatus = 'DRAFT' | 'ACTIVE' | 'PAUSED' | 'EXPIRED';

export interface OfferRequest {
  name: string;
  description?: string;
  type: OfferType;
  discountValue: number;
  minPurchaseAmount?: number;
  maxDiscountAmount?: number;
  validFrom?: string;
  validTo?: string;
  usageLimit?: number;
  applicableProducts?: string[];
  applicableCategories?: string[];
  canStack: boolean;
  priority?: number;
  termsAndConditions?: string;
}

export interface OfferResponse {
  id: string;
  name: string;
  description?: string;
  type: OfferType;
  status: OfferStatus;
  discountValue: number;
  minPurchaseAmount?: number;
  maxDiscountAmount?: number;
  validFrom?: string;
  validTo?: string;
  usageLimit?: number;
  usageCount: number;
  applicableProducts?: string[];
  applicableCategories?: string[];
  canStack: boolean;
  priority: number;
  termsAndConditions?: string;
  createdAt: string;
}

export interface DiscountCalculationRequest {
  productIds: string[];
  categoryIds: string[];
  purchaseAmount: number;
}

export interface DiscountCalculationResponse {
  offerId: string;
  offerName: string;
  discountAmount: number;
  finalAmount: number;
}

export const offersApi = {
  // CRUD operations
  createOffer: async (data: OfferRequest): Promise<ApiResponse<OfferResponse>> => {
    const response = await apiClient.post<ApiResponse<OfferResponse>>(
      '/api/v1/offers',
      data
    );
    return response.data;
  },

  listOffers: async (page = 0, size = 20): Promise<ApiResponse<PageResponse<OfferResponse>>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<OfferResponse>>>(
      '/api/v1/offers',
      { params: { page, size } }
    );
    return response.data;
  },

  getActiveOffers: async (): Promise<ApiResponse<OfferResponse[]>> => {
    const response = await apiClient.get<ApiResponse<OfferResponse[]>>(
      '/api/v1/offers/active'
    );
    return response.data;
  },

  getOffer: async (id: string): Promise<ApiResponse<OfferResponse>> => {
    const response = await apiClient.get<ApiResponse<OfferResponse>>(
      `/api/v1/offers/${id}`
    );
    return response.data;
  },

  updateOffer: async (id: string, data: OfferRequest): Promise<ApiResponse<OfferResponse>> => {
    const response = await apiClient.put<ApiResponse<OfferResponse>>(
      `/api/v1/offers/${id}`,
      data
    );
    return response.data;
  },

  deleteOffer: async (id: string): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(
      `/api/v1/offers/${id}`
    );
    return response.data;
  },

  // Offer lifecycle operations
  activateOffer: async (id: string): Promise<ApiResponse<OfferResponse>> => {
    const response = await apiClient.post<ApiResponse<OfferResponse>>(
      `/api/v1/offers/${id}/activate`
    );
    return response.data;
  },

  pauseOffer: async (id: string): Promise<ApiResponse<OfferResponse>> => {
    const response = await apiClient.post<ApiResponse<OfferResponse>>(
      `/api/v1/offers/${id}/pause`
    );
    return response.data;
  },

  // Discount calculation operations
  calculateDiscount: async (
    id: string,
    request: DiscountCalculationRequest
  ): Promise<ApiResponse<DiscountCalculationResponse>> => {
    const response = await apiClient.post<ApiResponse<DiscountCalculationResponse>>(
      `/api/v1/offers/${id}/calculate-discount`,
      request
    );
    return response.data;
  },

  applyOffer: async (
    id: string,
    request: DiscountCalculationRequest
  ): Promise<ApiResponse<DiscountCalculationResponse>> => {
    const response = await apiClient.post<ApiResponse<DiscountCalculationResponse>>(
      `/api/v1/offers/${id}/apply`,
      request
    );
    return response.data;
  },

  getApplicableOffers: async (request: DiscountCalculationRequest): Promise<ApiResponse<OfferResponse[]>> => {
    const response = await apiClient.post<ApiResponse<OfferResponse[]>>(
      '/api/v1/offers/applicable',
      request
    );
    return response.data;
  },

  getBestOfferCombination: async (
    request: DiscountCalculationRequest
  ): Promise<ApiResponse<DiscountCalculationResponse[]>> => {
    const response = await apiClient.post<ApiResponse<DiscountCalculationResponse[]>>(
      '/api/v1/offers/best-combination',
      request
    );
    return response.data;
  },
};
