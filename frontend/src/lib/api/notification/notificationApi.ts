import apiClient from '../client';
import { ApiResponse, PageResponse } from '../types';

export type NotificationType = 'EMAIL' | 'SMS' | 'WHATSAPP' | 'PUSH';
export type NotificationStatus = 'PENDING' | 'SENT' | 'FAILED' | 'DELIVERED';

export interface NotificationRequest {
  type: NotificationType;
  recipient: string;
  subject?: string;
  message: string;
  metadata?: Record<string, any>;
}

export interface NotificationResponse {
  id: string;
  type: NotificationType;
  recipient: string;
  subject?: string;
  message: string;
  status: NotificationStatus;
  retryCount: number;
  errorMessage?: string;
  sentAt?: string;
  deliveredAt?: string;
  createdAt: string;
}

export const notificationApi = {
  sendNotification: async (data: NotificationRequest): Promise<ApiResponse<NotificationResponse>> => {
    const response = await apiClient.post<ApiResponse<NotificationResponse>>(
      '/api/v1/notifications/send',
      data
    );
    return response.data;
  },

  listNotifications: async (
    page = 0,
    size = 20,
    status?: NotificationStatus
  ): Promise<ApiResponse<PageResponse<NotificationResponse>>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<NotificationResponse>>>(
      '/api/v1/notifications',
      { params: { page, size, status } }
    );
    return response.data;
  },

  // Convenience methods for specific notification types
  sendEmail: async (recipient: string, subject: string, message: string): Promise<ApiResponse<NotificationResponse>> => {
    return notificationApi.sendNotification({
      type: 'EMAIL',
      recipient,
      subject,
      message,
    });
  },

  sendSMS: async (recipient: string, message: string): Promise<ApiResponse<NotificationResponse>> => {
    return notificationApi.sendNotification({
      type: 'SMS',
      recipient,
      message,
    });
  },

  sendWhatsApp: async (recipient: string, message: string): Promise<ApiResponse<NotificationResponse>> => {
    return notificationApi.sendNotification({
      type: 'WHATSAPP',
      recipient,
      message,
    });
  },
};
