/**
 * API Client
 * 
 * Centralized API client for all backend communications.
 */

import axios, { AxiosInstance, AxiosRequestConfig } from 'axios';
import { offlineSyncManager } from '../engine/offlineSync';

export class APIClient {
  private client: AxiosInstance;
  private tenantId: string = 'default';
  private userId: string = 'admin';

  constructor(baseURL: string = 'http://localhost:8081') {
    this.client = axios.create({
      baseURL,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Request interceptor
    this.client.interceptors.request.use(
      (config) => {
        config.headers['X-Tenant-Id'] = this.tenantId;
        config.headers['X-User-Id'] = this.userId;
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Response interceptor
    this.client.interceptors.response.use(
      (response) => response,
      async (error) => {
        // If offline, queue the request
        if (!navigator.onLine && error.config.method !== 'get') {
          const { url, method, data } = error.config;
          const entity = this.extractEntityFromUrl(url);
          
          if (entity) {
            await offlineSyncManager.queueAction(entity, method as any, JSON.parse(data || '{}'));
            return Promise.resolve({ data: { queued: true } });
          }
        }
        return Promise.reject(error);
      }
    );
  }

  setCredentials(tenantId: string, userId: string): void {
    this.tenantId = tenantId;
    this.userId = userId;
  }

  private extractEntityFromUrl(url: string): string | null {
    const match = url.match(/\/api\/([^\/]+)\//);
    return match ? match[1] : null;
  }

  // Entity CRUD operations
  async createEntity(entity: string, data: any): Promise<any> {
    const response = await this.client.post(`/api/${entity}/create`, data);
    return response.data;
  }

  async updateEntity(entity: string, data: any): Promise<any> {
    const response = await this.client.post(`/api/${entity}/update`, data);
    return response.data;
  }

  async findEntities(entity: string, params?: Record<string, any>): Promise<any> {
    const response = await this.client.get(`/api/${entity}/find`, { params });
    return response.data;
  }

  async getEntityById(entity: string, id: string): Promise<any> {
    const response = await this.client.get(`/api/${entity}/${id}`);
    return response.data;
  }

  async deleteEntity(entity: string, id: string): Promise<any> {
    const response = await this.client.delete(`/api/${entity}/${id}`);
    return response.data;
  }

  async executeAction(entity: string, actionName: string, data: any): Promise<any> {
    const response = await this.client.post(`/api/${entity}/action/${actionName}`, data);
    return response.data;
  }

  // Metadata operations
  async getFormMetadata(id: string): Promise<any> {
    const response = await this.client.get(`/api/metadata/form/${id}`);
    return response.data;
  }

  async getListMetadata(id: string): Promise<any> {
    const response = await this.client.get(`/api/metadata/list/${id}`);
    return response.data;
  }

  async getPageMetadata(id: string): Promise<any> {
    const response = await this.client.get(`/api/metadata/page/${id}`);
    return response.data;
  }

  async getWorkflowMetadata(id: string): Promise<any> {
    const response = await this.client.get(`/api/metadata/workflow/${id}`);
    return response.data;
  }

  // Workflow operations
  async executeWorkflowStep(workflowId: string, stepId: string, data: any): Promise<any> {
    const response = await this.client.post(
      `/api/workflow/${workflowId}/step/${stepId}`,
      data
    );
    return response.data;
  }

  // Generic request
  async request<T = any>(config: AxiosRequestConfig): Promise<T> {
    const response = await this.client.request<T>(config);
    return response.data;
  }
}

// Export singleton
export const apiClient = new APIClient();

export default apiClient;
