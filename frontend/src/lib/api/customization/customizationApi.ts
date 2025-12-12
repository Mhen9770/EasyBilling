import apiClient from '../client';
import { ApiResponse, PageResponse } from '../types';

// Configuration Types
export interface SystemConfigurationDTO {
  id?: number;
  configKey: string;
  configValue: string;
  description?: string;
  isEncrypted?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface TenantConfigurationDTO {
  id?: number;
  configKey: string;
  configValue: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

// Custom Theme Types
export interface CustomThemeDTO {
  id?: number;
  themeName: string;
  primaryColor: string;
  secondaryColor: string;
  accentColor: string;
  backgroundColor: string;
  textColor: string;
  fontFamily: string;
  logoUrl?: string;
  faviconUrl?: string;
  companyName?: string;
  invoiceHeaderColor?: string;
  invoiceFooterColor?: string;
  customCss?: string;
  isActive?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// Custom Field Types
export type CustomFieldType = 'TEXT' | 'NUMBER' | 'DATE' | 'BOOLEAN' | 'DROPDOWN' | 'TEXTAREA' | 'EMAIL' | 'PHONE' | 'URL';
export type CustomFieldEntityType = 'INVOICE' | 'CUSTOMER' | 'PRODUCT' | 'SUPPLIER' | 'ORDER';

export interface CustomFieldDTO {
  id?: number;
  entityType: CustomFieldEntityType;
  fieldName: string;
  fieldLabel: string;
  fieldType: CustomFieldType;
  isRequired: boolean;
  defaultValue?: string;
  dropdownOptions?: string; // JSON string for dropdown options
  validationRules?: string; // JSON string for validation rules
  displayOrder?: number;
  isActive?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// Document Template Types
export interface DocumentTemplateDTO {
  id?: number;
  templateName: string;
  templateType: 'INVOICE' | 'RECEIPT' | 'QUOTATION' | 'PURCHASE_ORDER' | 'REPORT';
  format: 'HTML' | 'PDF' | 'EXCEL' | 'CSV';
  templateContent: string; // HTML/template content
  headerContent?: string;
  footerContent?: string;
  pageSize?: string;
  pageOrientation?: string;
  marginTop?: number;
  marginRight?: number;
  marginBottom?: number;
  marginLeft?: number;
  isDefault?: boolean;
  isActive?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// Webhook Types
export interface WebhookDTO {
  id?: number;
  webhookName: string;
  webhookUrl: string;
  eventType: string;
  isActive?: boolean;
  headers?: string; // JSON string for headers
  payloadTemplate?: string;
  retryCount?: number;
  timeoutSeconds?: number;
  lastTriggeredAt?: string;
  successCount?: number;
  failureCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export const customizationApi = {
  // ===== Configuration APIs =====
  
  // System Configuration
  getSystemConfigurations: async (): Promise<ApiResponse<SystemConfigurationDTO[]>> => {
    const response = await apiClient.get<ApiResponse<SystemConfigurationDTO[]>>(
      '/api/v1/configuration/system'
    );
    return response.data;
  },

  createSystemConfiguration: async (data: SystemConfigurationDTO): Promise<ApiResponse<SystemConfigurationDTO>> => {
    const response = await apiClient.post<ApiResponse<SystemConfigurationDTO>>(
      '/api/v1/configuration/system',
      data
    );
    return response.data;
  },

  updateSystemConfiguration: async (key: string, data: SystemConfigurationDTO): Promise<ApiResponse<SystemConfigurationDTO>> => {
    const response = await apiClient.put<ApiResponse<SystemConfigurationDTO>>(
      `/api/v1/configuration/system/${key}`,
      data
    );
    return response.data;
  },

  deleteSystemConfiguration: async (key: string): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(
      `/api/v1/configuration/system/${key}`
    );
    return response.data;
  },

  // Tenant Configuration
  getTenantConfigurations: async (): Promise<ApiResponse<TenantConfigurationDTO[]>> => {
    const response = await apiClient.get<ApiResponse<TenantConfigurationDTO[]>>(
      '/api/v1/configuration/tenant'
    );
    return response.data;
  },

  getTenantConfiguration: async (key: string): Promise<ApiResponse<string>> => {
    const response = await apiClient.get<ApiResponse<string>>(
      `/api/v1/configuration/tenant/${key}`
    );
    return response.data;
  },

  createTenantConfiguration: async (data: TenantConfigurationDTO): Promise<ApiResponse<TenantConfigurationDTO>> => {
    const response = await apiClient.post<ApiResponse<TenantConfigurationDTO>>(
      '/api/v1/configuration/tenant',
      data
    );
    return response.data;
  },

  updateTenantConfiguration: async (key: string, data: TenantConfigurationDTO): Promise<ApiResponse<TenantConfigurationDTO>> => {
    const response = await apiClient.put<ApiResponse<TenantConfigurationDTO>>(
      `/api/v1/configuration/tenant/${key}`,
      data
    );
    return response.data;
  },

  deleteTenantConfiguration: async (key: string): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(
      `/api/v1/configuration/tenant/${key}`
    );
    return response.data;
  },

  // ===== Custom Theme APIs =====

  getActiveTheme: async (): Promise<ApiResponse<CustomThemeDTO>> => {
    const response = await apiClient.get<ApiResponse<CustomThemeDTO>>(
      '/api/v1/themes/active'
    );
    return response.data;
  },

  getAllThemes: async (): Promise<ApiResponse<CustomThemeDTO[]>> => {
    const response = await apiClient.get<ApiResponse<CustomThemeDTO[]>>(
      '/api/v1/themes'
    );
    return response.data;
  },

  getTheme: async (id: number): Promise<ApiResponse<CustomThemeDTO>> => {
    const response = await apiClient.get<ApiResponse<CustomThemeDTO>>(
      `/api/v1/themes/${id}`
    );
    return response.data;
  },

  createTheme: async (data: CustomThemeDTO): Promise<ApiResponse<CustomThemeDTO>> => {
    const response = await apiClient.post<ApiResponse<CustomThemeDTO>>(
      '/api/v1/themes',
      data
    );
    return response.data;
  },

  updateTheme: async (id: number, data: CustomThemeDTO): Promise<ApiResponse<CustomThemeDTO>> => {
    const response = await apiClient.put<ApiResponse<CustomThemeDTO>>(
      `/api/v1/themes/${id}`,
      data
    );
    return response.data;
  },

  deleteTheme: async (id: number): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(
      `/api/v1/themes/${id}`
    );
    return response.data;
  },

  activateTheme: async (id: number): Promise<ApiResponse<CustomThemeDTO>> => {
    const response = await apiClient.put<ApiResponse<CustomThemeDTO>>(
      `/api/v1/themes/${id}/activate`
    );
    return response.data;
  },

  // ===== Custom Field APIs =====

  getCustomFields: async (entityType?: CustomFieldEntityType): Promise<ApiResponse<CustomFieldDTO[]>> => {
    const params = entityType ? { entityType } : {};
    const response = await apiClient.get<ApiResponse<CustomFieldDTO[]>>(
      '/api/v1/custom-fields',
      { params }
    );
    return response.data;
  },

  getCustomField: async (id: number): Promise<ApiResponse<CustomFieldDTO>> => {
    const response = await apiClient.get<ApiResponse<CustomFieldDTO>>(
      `/api/v1/custom-fields/${id}`
    );
    return response.data;
  },

  createCustomField: async (data: CustomFieldDTO): Promise<ApiResponse<CustomFieldDTO>> => {
    const response = await apiClient.post<ApiResponse<CustomFieldDTO>>(
      '/api/v1/custom-fields',
      data
    );
    return response.data;
  },

  updateCustomField: async (id: number, data: CustomFieldDTO): Promise<ApiResponse<CustomFieldDTO>> => {
    const response = await apiClient.put<ApiResponse<CustomFieldDTO>>(
      `/api/v1/custom-fields/${id}`,
      data
    );
    return response.data;
  },

  deleteCustomField: async (id: number): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(
      `/api/v1/custom-fields/${id}`
    );
    return response.data;
  },

  // Custom Field Values
  getCustomFieldValues: async (entityType: string, entityId: string): Promise<ApiResponse<Record<number, string>>> => {
    const response = await apiClient.get<ApiResponse<Record<number, string>>>(
      `/api/v1/custom-fields/values/${entityType}/${entityId}`
    );
    return response.data;
  },

  saveCustomFieldValues: async (
    entityType: string,
    entityId: string,
    values: Record<number, string>
  ): Promise<ApiResponse<void>> => {
    const response = await apiClient.post<ApiResponse<void>>(
      `/api/v1/custom-fields/values/${entityType}/${entityId}`,
      values
    );
    return response.data;
  },
};

export default customizationApi;
