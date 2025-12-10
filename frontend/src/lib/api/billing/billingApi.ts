import apiClient from '../client';
import { ApiResponse, PageResponse } from '../types';

export interface InvoiceItemRequest {
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  discountType?: 'PERCENTAGE' | 'FLAT';
  discountValue?: number;
  taxType?: 'CGST_SGST' | 'IGST' | 'NONE';
  taxRate?: number;
}

export interface PaymentRequest {
  mode: 'CASH' | 'CARD' | 'UPI' | 'WALLET' | 'CREDIT' | 'BANK_TRANSFER';
  amount: number;
  reference?: string;
}

export interface InvoiceRequest {
  customerName?: string;
  customerPhone?: string;
  customerEmail?: string;
  storeId?: string;
  counterNumber?: string;
  items: InvoiceItemRequest[];
}

export interface CompleteInvoiceRequest {
  payments: PaymentRequest[];
}

export interface InvoiceItemResponse {
  id: string;
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  discountType?: string;
  discountValue?: number;
  discountAmount: number;
  taxType?: string;
  taxRate?: number;
  taxAmount: number;
  lineTotal: number;
}

export interface PaymentResponse {
  id: string;
  mode: string;
  amount: number;
  reference?: string;
  paidAt: string;
}

export interface InvoiceResponse {
  id: string;
  invoiceNumber: string;
  status: 'DRAFT' | 'COMPLETED' | 'CANCELLED' | 'RETURNED';
  customerName?: string;
  customerPhone?: string;
  customerEmail?: string;
  storeId?: string;
  counterNumber?: string;
  items: InvoiceItemResponse[];
  payments: PaymentResponse[];
  subtotal: number;
  totalDiscount: number;
  totalTax: number;
  total: number;
  paidAmount: number;
  balanceAmount: number;
  createdBy?: string;
  completedBy?: string;
  createdAt: string;
  completedAt?: string;
}

export const billingApi = {
  // Create a new invoice (draft)
  createInvoice: async (data: InvoiceRequest): Promise<ApiResponse<InvoiceResponse>> => {
    const response = await apiClient.post<ApiResponse<InvoiceResponse>>(
      '/billing-service/api/v1/invoices',
      data
    );
    return response.data;
  },

  // Get invoice by ID
  getInvoice: async (id: string): Promise<ApiResponse<InvoiceResponse>> => {
    const response = await apiClient.get<ApiResponse<InvoiceResponse>>(
      `/billing-service/api/v1/invoices/${id}`
    );
    return response.data;
  },

  // List all invoices
  listInvoices: async (page = 0, size = 20): Promise<ApiResponse<PageResponse<InvoiceResponse>>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<InvoiceResponse>>>(
      '/billing-service/api/v1/invoices',
      { params: { page, size } }
    );
    return response.data;
  },

  // Complete an invoice with payments
  completeInvoice: async (
    id: string,
    data: CompleteInvoiceRequest
  ): Promise<ApiResponse<InvoiceResponse>> => {
    const response = await apiClient.post<ApiResponse<InvoiceResponse>>(
      `/billing-service/api/v1/invoices/${id}/complete`,
      data
    );
    return response.data;
  },

  // Hold an invoice for later
  holdInvoice: async (data: InvoiceRequest): Promise<ApiResponse<InvoiceResponse>> => {
    const response = await apiClient.post<ApiResponse<InvoiceResponse>>(
      '/billing-service/api/v1/invoices/hold',
      data
    );
    return response.data;
  },
};
