/**
 * WebSocket Client
 * 
 * Real-time metadata updates via WebSocket connection.
 */

import { io, Socket } from 'socket.io-client';
import { useMetadataStore } from './metadataClient';

class WebSocketClient {
  private socket: Socket | null = null;
  private url: string;
  private connected: boolean = false;

  constructor(url: string = 'http://localhost:8081') {
    this.url = url;
  }

  /**
   * Connect to WebSocket server
   */
  connect(tenantId: string, userId: string): void {
    if (this.socket) {
      console.warn('WebSocket already connected');
      return;
    }

    this.socket = io(this.url, {
      transports: ['websocket'],
      auth: {
        tenantId,
        userId,
      },
    });

    this.socket.on('connect', () => {
      console.log('WebSocket connected');
      this.connected = true;
    });

    this.socket.on('disconnect', () => {
      console.log('WebSocket disconnected');
      this.connected = false;
    });

    this.socket.on('metadata.updated', (data: { type: string; id: string }) => {
      console.log('Metadata updated:', data);
      this.handleMetadataUpdate(data.type, data.id);
    });

    this.socket.on('metadata.deleted', (data: { type: string; id: string }) => {
      console.log('Metadata deleted:', data);
      this.handleMetadataDelete(data.type, data.id);
    });

    this.socket.on('error', (error: any) => {
      console.error('WebSocket error:', error);
    });
  }

  /**
   * Disconnect from WebSocket server
   */
  disconnect(): void {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
      this.connected = false;
    }
  }

  /**
   * Handle metadata update event
   */
  private handleMetadataUpdate(type: string, id: string): void {
    const store = useMetadataStore.getState();
    
    // Invalidate cache to force refetch
    store.invalidate(type, id);
    
    // Emit custom event for components to react
    window.dispatchEvent(
      new CustomEvent('metadata-updated', {
        detail: { type, id },
      })
    );
  }

  /**
   * Handle metadata delete event
   */
  private handleMetadataDelete(type: string, id: string): void {
    const store = useMetadataStore.getState();
    store.invalidate(type, id);
    
    window.dispatchEvent(
      new CustomEvent('metadata-deleted', {
        detail: { type, id },
      })
    );
  }

  /**
   * Subscribe to custom metadata events
   */
  subscribe(eventName: string, callback: (data: any) => void): void {
    if (this.socket) {
      this.socket.on(eventName, callback);
    }
  }

  /**
   * Unsubscribe from custom metadata events
   */
  unsubscribe(eventName: string): void {
    if (this.socket) {
      this.socket.off(eventName);
    }
  }

  /**
   * Emit custom event
   */
  emit(eventName: string, data: any): void {
    if (this.socket && this.connected) {
      this.socket.emit(eventName, data);
    } else {
      console.warn('Cannot emit: WebSocket not connected');
    }
  }

  /**
   * Check if connected
   */
  isConnected(): boolean {
    return this.connected;
  }
}

// Export singleton
export const wsClient = new WebSocketClient();

export default wsClient;

/**
 * React hook for WebSocket metadata updates
 */
export function useMetadataUpdates(
  type: string,
  id: string,
  onUpdate: () => void
): void {
  React.useEffect(() => {
    const handler = (event: CustomEvent) => {
      if (event.detail.type === type && event.detail.id === id) {
        onUpdate();
      }
    };

    window.addEventListener('metadata-updated' as any, handler as any);
    
    return () => {
      window.removeEventListener('metadata-updated' as any, handler as any);
    };
  }, [type, id, onUpdate]);
}

import React from 'react';
