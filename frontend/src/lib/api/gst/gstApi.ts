import apiClient from '../client';
import { ApiResponse } from '../types';

// GST Types
export interface GstRate {
  id: string;
  hsnCode?: string;
  sacCode?: string;
  taxCategory: string;
  cgstRate: number;
  sgstRate: number;
  igstRate: number;
  cessRate: number;
  effectiveFrom: string;
  effectiveTo?: string;
  isActive: boolean;
}

export interface GstCalculation {
  taxableAmount: number;
  cgst: number;
  sgst: number;
  igst: number;
  cess: number;
  totalTax: number;
  cgstRate: number;
  sgstRate: number;
  igstRate: number;
  cessRate: number;
  isInterstate: boolean;
}

export interface GstCalculationRequest {
  hsnOrSacCode: string;
  amount: number;
  supplierState: string;
  customerState: string;
}

export const gstApi = {
  /**
   * Calculate GST for given parameters
   */
  calculateGst: async (request: GstCalculationRequest): Promise<ApiResponse<GstCalculation>> => {
    const response = await apiClient.post<ApiResponse<GstCalculation>>(
      '/api/v1/gst/calculate',
      request
    );
    return response.data;
  },

  /**
   * Get all GST rates
   */
  listGstRates: async (): Promise<ApiResponse<GstRate[]>> => {
    const response = await apiClient.get<ApiResponse<GstRate[]>>(
      '/api/v1/gst/rates'
    );
    return response.data;
  },

  /**
   * Get active GST rates
   */
  listActiveGstRates: async (): Promise<ApiResponse<GstRate[]>> => {
    const response = await apiClient.get<ApiResponse<GstRate[]>>(
      '/api/v1/gst/rates/active'
    );
    return response.data;
  },

  /**
   * Validate GSTIN format
   */
  validateGstin: async (gstin: string): Promise<ApiResponse<boolean>> => {
    const response = await apiClient.post<ApiResponse<boolean>>(
      '/api/v1/gst/validate-gstin',
      { gstin }
    );
    return response.data;
  },
};
