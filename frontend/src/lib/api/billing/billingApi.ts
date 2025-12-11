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
  // GST fields (India)
  hsnCode?: string;
  sacCode?: string;
  cgstRate?: number;
  sgstRate?: number;
  igstRate?: number;
  cessRate?: number;
}

export interface PaymentRequest {
  mode: 'CASH' | 'CARD' | 'UPI' | 'WALLET' | 'CREDIT' | 'BANK_TRANSFER';
  amount: number;
  reference?: string;
}

export interface InvoiceRequest {
  customerId?: string;
  customerName?: string;
  customerPhone?: string;
  customerEmail?: string;
  storeId: string;
  counterId: string;
  items: InvoiceItemRequest[];
  notes?: string;
  // GST fields (India)
  customerGstin?: string;
  placeOfSupply?: string;
  supplierGstin?: string;
  reverseCharge?: boolean;
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
  // GST fields (India)
  hsnCode?: string;
  sacCode?: string;
  cgstRate?: number;
  sgstRate?: number;
  igstRate?: number;
  cessRate?: number;
  cgstAmount?: number;
  sgstAmount?: number;
  igstAmount?: number;
  cessAmount?: number;
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
  // GST fields (India)
  customerGstin?: string;
  supplierGstin?: string;
  placeOfSupply?: string;
  reverseCharge?: boolean;
  isInterstate?: boolean;
  totalCgst?: number;
  totalSgst?: number;
  totalIgst?: number;
  totalCess?: number;
}

export const billingApi = {
  // Create a new invoice (draft)
  createInvoice: async (data: InvoiceRequest): Promise<ApiResponse<InvoiceResponse>> => {
    const response = await apiClient.post<ApiResponse<InvoiceResponse>>(
      '/api/v1/invoices',
      data
    );
    return response.data;
  },

  // Get invoice by ID
  getInvoice: async (id: string): Promise<ApiResponse<InvoiceResponse>> => {
    const response = await apiClient.get<ApiResponse<InvoiceResponse>>(
      `/api/v1/invoices/${id}`
    );
    return response.data;
  },

  // List all invoices
  listInvoices: async (page = 0, size = 20): Promise<ApiResponse<PageResponse<InvoiceResponse>>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<InvoiceResponse>>>(
      '/api/v1/invoices',
      { params: { page, size } }
    );
    return response.data;
  },

  // Complete an invoice with payments
  completeInvoice: async (
    id: string,
    payments: PaymentRequest[]
  ): Promise<ApiResponse<InvoiceResponse>> => {
    const response = await apiClient.post<ApiResponse<InvoiceResponse>>(
      `/api/v1/invoices/${id}/complete`,
      payments
    );
    return response.data;
  },

  // Hold an invoice for later
  holdInvoice: async (data: InvoiceRequest): Promise<ApiResponse<string>> => {
    const response = await apiClient.post<ApiResponse<string>>(
      '/api/v1/invoices/hold',
      data
    );
    return response.data;
  },

  // List held invoices
  listHeldInvoices: async (): Promise<ApiResponse<HeldInvoiceResponse[]>> => {
    const response = await apiClient.get<ApiResponse<HeldInvoiceResponse[]>>(
      '/api/v1/invoices/held'
    );
    return response.data;
  },

  // Resume a held invoice
  resumeHeldInvoice: async (holdReference: string): Promise<ApiResponse<InvoiceRequest>> => {
    const response = await apiClient.get<ApiResponse<InvoiceRequest>>(
      `/api/v1/invoices/held/${holdReference}`
    );
    return response.data;
  },

  // Delete a held invoice
  deleteHeldInvoice: async (holdReference: string): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(
      `/api/v1/invoices/held/${holdReference}`
    );
    return response.data;
  },

  // Cancel an invoice
  cancelInvoice: async (id: string, reason: string): Promise<ApiResponse<InvoiceResponse>> => {
    const response = await apiClient.post<ApiResponse<InvoiceResponse>>(
      `/api/v1/invoices/${id}/cancel`,
      { reason }
    );
    return response.data;
  },

  // Return an invoice
  returnInvoice: async (
    id: string,
    items: Array<{ productId: string; quantity: number }>,
    reason: string
  ): Promise<ApiResponse<InvoiceResponse>> => {
    const response = await apiClient.post<ApiResponse<InvoiceResponse>>(
      `/api/v1/invoices/${id}/return`,
      { items, reason }
    );
    return response.data;
  },
};

export interface HeldInvoiceResponse {
  holdReference: string;
  storeId: string;
  counterId: string;
  customerName?: string;
  customerPhone?: string;
  itemCount: number;
  totalAmount: number;
  heldAt: string;
  heldBy: string;
  notes?: string;
}
