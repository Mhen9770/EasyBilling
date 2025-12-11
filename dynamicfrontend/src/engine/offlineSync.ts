/**
 * Offline Sync Manager
 * 
 * Manages offline data persistence and synchronization with IndexedDB.
 */

import { openDB, DBSchema, IDBPDatabase } from 'idb';

interface OfflineDB extends DBSchema {
  'pending-actions': {
    key: string;
    value: {
      id: string;
      entity: string;
      action: 'create' | 'update' | 'delete';
      data: any;
      timestamp: number;
      retries: number;
    };
  };
  'cached-metadata': {
    key: string;
    value: {
      type: string;
      id: string;
      data: any;
      timestamp: number;
    };
  };
  'cached-entities': {
    key: string;
    value: {
      entity: string;
      id: string;
      data: any;
      timestamp: number;
    };
  };
}

class OfflineSyncManager {
  private db: IDBPDatabase<OfflineDB> | null = null;
  private syncInterval: NodeJS.Timeout | null = null;
  private isOnline: boolean = navigator.onLine;

  async init(): Promise<void> {
    this.db = await openDB<OfflineDB>('dynamic-frontend-offline', 1, {
      upgrade(db) {
        // Pending actions store
        if (!db.objectStoreNames.contains('pending-actions')) {
          db.createObjectStore('pending-actions', { keyPath: 'id' });
        }

        // Cached metadata store
        if (!db.objectStoreNames.contains('cached-metadata')) {
          db.createObjectStore('cached-metadata', { keyPath: ['type', 'id'] });
        }

        // Cached entities store
        if (!db.objectStoreNames.contains('cached-entities')) {
          db.createObjectStore('cached-entities', { keyPath: ['entity', 'id'] });
        }
      },
    });

    // Listen for online/offline events
    window.addEventListener('online', () => {
      this.isOnline = true;
      console.log('Back online - starting sync');
      this.syncPendingActions();
    });

    window.addEventListener('offline', () => {
      this.isOnline = false;
      console.log('Gone offline - queuing actions');
    });

    // Start periodic sync
    this.startPeriodicSync();
  }

  /**
   * Queue an action for later sync
   */
  async queueAction(
    entity: string,
    action: 'create' | 'update' | 'delete',
    data: any
  ): Promise<void> {
    if (!this.db) throw new Error('Database not initialized');

    const actionId = `${entity}_${action}_${Date.now()}_${Math.random()}`;
    
    await this.db.put('pending-actions', {
      id: actionId,
      entity,
      action,
      data,
      timestamp: Date.now(),
      retries: 0,
    });

    console.log('Action queued:', actionId);
  }

  /**
   * Get all pending actions
   */
  async getPendingActions(): Promise<any[]> {
    if (!this.db) throw new Error('Database not initialized');
    return await this.db.getAll('pending-actions');
  }

  /**
   * Sync pending actions with server
   */
  async syncPendingActions(): Promise<void> {
    if (!this.isOnline || !this.db) return;

    const pendingActions = await this.getPendingActions();
    
    if (pendingActions.length === 0) {
      console.log('No pending actions to sync');
      return;
    }

    console.log(`Syncing ${pendingActions.length} pending actions`);

    for (const action of pendingActions) {
      try {
        await this.executeAction(action);
        await this.db.delete('pending-actions', action.id);
        console.log('Action synced successfully:', action.id);
      } catch (error) {
        console.error('Failed to sync action:', action.id, error);
        
        // Increment retry count
        action.retries += 1;
        
        // Remove if too many retries
        if (action.retries > 5) {
          await this.db.delete('pending-actions', action.id);
          console.error('Action removed after 5 failed retries:', action.id);
        } else {
          await this.db.put('pending-actions', action);
        }
      }
    }
  }

  /**
   * Execute a pending action against the server
   */
  private async executeAction(action: any): Promise<void> {
    const baseUrl = 'http://localhost:8081';
    const url = `${baseUrl}/api/${action.entity}/${action.action}`;

    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Tenant-Id': 'default',
        'X-User-Id': 'admin',
      },
      body: JSON.stringify(action.data),
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }
  }

  /**
   * Cache metadata locally
   */
  async cacheMetadata(type: string, id: string, data: any): Promise<void> {
    if (!this.db) throw new Error('Database not initialized');

    await this.db.put('cached-metadata', {
      type,
      id,
      data,
      timestamp: Date.now(),
    });
  }

  /**
   * Get cached metadata
   */
  async getCachedMetadata(type: string, id: string): Promise<any | null> {
    if (!this.db) throw new Error('Database not initialized');

    const cached = await this.db.get('cached-metadata', [type, id]);
    return cached?.data || null;
  }

  /**
   * Cache entity data locally
   */
  async cacheEntity(entity: string, id: string, data: any): Promise<void> {
    if (!this.db) throw new Error('Database not initialized');

    await this.db.put('cached-entities', {
      entity,
      id,
      data,
      timestamp: Date.now(),
    });
  }

  /**
   * Get cached entity
   */
  async getCachedEntity(entity: string, id: string): Promise<any | null> {
    if (!this.db) throw new Error('Database not initialized');

    const cached = await this.db.get('cached-entities', [entity, id]);
    return cached?.data || null;
  }

  /**
   * Clear all cached data
   */
  async clearCache(): Promise<void> {
    if (!this.db) throw new Error('Database not initialized');

    await this.db.clear('cached-metadata');
    await this.db.clear('cached-entities');
    console.log('Cache cleared');
  }

  /**
   * Clear pending actions
   */
  async clearPendingActions(): Promise<void> {
    if (!this.db) throw new Error('Database not initialized');

    await this.db.clear('pending-actions');
    console.log('Pending actions cleared');
  }

  /**
   * Start periodic sync
   */
  private startPeriodicSync(): void {
    if (this.syncInterval) {
      clearInterval(this.syncInterval);
    }

    // Sync every 30 seconds
    this.syncInterval = setInterval(() => {
      if (this.isOnline) {
        this.syncPendingActions();
      }
    }, 30000);
  }

  /**
   * Stop periodic sync
   */
  stopPeriodicSync(): void {
    if (this.syncInterval) {
      clearInterval(this.syncInterval);
      this.syncInterval = null;
    }
  }

  /**
   * Check if online
   */
  getOnlineStatus(): boolean {
    return this.isOnline;
  }

  /**
   * Get pending action count
   */
  async getPendingCount(): Promise<number> {
    if (!this.db) return 0;
    const actions = await this.db.getAll('pending-actions');
    return actions.length;
  }
}

// Export singleton
export const offlineSyncManager = new OfflineSyncManager();

export default offlineSyncManager;
