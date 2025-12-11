/**
 * Main Application Entry Point
 * 
 * Complete implementation with all features: forms, lists, workflows, plugins, admin UI, offline sync.
 */

import React, { useEffect, useState } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import componentRegistry from './engine/registry';
import { FormRenderer } from './engine/renderer';
import { ListRenderer } from './engine/listRenderer';
import { WorkflowRunner } from './engine/workflowUI';
import { PageRenderer } from './engine/pageRenderer';
import { metadataClient, FormMetadata, ListMetadata, WorkflowMetadata, PageMetadata } from './engine/metadataClient';
import { usePermissionStore } from './engine/permissions';
import { MetadataEditor } from './pages/admin/MetadataEditor';
import { LayoutBuilder } from './pages/admin/LayoutBuilder';
import { KPICard, QuickActionsWidget, ActivityFeedWidget } from './components/widgets/Widgets';
import { useOnlineStatus } from './hooks/useCustomHooks';
import { offlineSyncManager } from './engine/offlineSync';
import { apiClient } from './api/client';

// Import and register primitive components
import {
  TextInput,
  TextArea,
  NumberInput,
  SelectInput,
  CheckboxInput,
  DateInput,
} from './components/primitives/FormControls';

// Register all primitive components
componentRegistry.registerBatch({
  Text: TextInput,
  TextArea: TextArea,
  Number: NumberInput,
  Select: SelectInput,
  Checkbox: CheckboxInput,
  Date: DateInput,
});

// Register widgets
componentRegistry.registerBatch({
  KPICard: KPICard,
  QuickActionsWidget: QuickActionsWidget,
  ActivityFeedWidget: ActivityFeedWidget,
});

// Create React Query client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      retry: 1,
    },
  },
});

/**
 * Online/Offline Status Indicator
 */
function OnlineStatusBanner() {
  const isOnline = useOnlineStatus();
  const [pendingCount, setPendingCount] = useState(0);

  useEffect(() => {
    const updateCount = async () => {
      const count = await offlineSyncManager.getPendingCount();
      setPendingCount(count);
    };
    
    updateCount();
    const interval = setInterval(updateCount, 5000);
    return () => clearInterval(interval);
  }, []);

  if (isOnline && pendingCount === 0) return null;

  return (
    <div className={`${isOnline ? 'bg-yellow-50 border-yellow-200' : 'bg-red-50 border-red-200'} border-b px-4 py-2`}>
      <div className="container mx-auto flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span className={`h-3 w-3 rounded-full ${isOnline ? 'bg-yellow-500' : 'bg-red-500'} animate-pulse`} />
          <span className={`font-medium ${isOnline ? 'text-yellow-800' : 'text-red-800'}`}>
            {isOnline ? 'Online - Syncing...' : 'Offline Mode'}
          </span>
        </div>
        {pendingCount > 0 && (
          <span className="text-sm text-gray-700">
            {pendingCount} pending action{pendingCount > 1 ? 's' : ''}
          </span>
        )}
      </div>
    </div>
  );
}

/**
 * Demo Dashboard Component
 */
function DemoDashboard() {
  return (
    <div className="space-y-6">
      <div className="grid grid-cols-4 gap-6">
        <KPICard title="Total Customers" value="1,234" change={12.5} icon="👥" />
        <KPICard title="Active Invoices" value="$45,678" change={-3.2} icon="📄" />
        <KPICard title="Pending Payments" value="23" change={5.7} icon="💰" />
        <KPICard title="Products" value="567" change={8.1} icon="📦" />
      </div>

      <div className="grid grid-cols-2 gap-6">
        <QuickActionsWidget
          actions={[
            { label: 'New Customer', icon: '👤', onClick: () => {}, color: 'primary' },
            { label: 'New Invoice', icon: '📄', onClick: () => {} },
            { label: 'View Reports', icon: '📊', onClick: () => {} },
            { label: 'Settings', icon: '⚙️', onClick: () => {} },
          ]}
        />
        <ActivityFeedWidget
          activities={[
            { id: '1', message: 'Customer "Acme Corp" created', timestamp: new Date(), type: 'success' },
            { id: '2', message: 'Invoice #1234 generated', timestamp: new Date(), type: 'info' },
            { id: '3', message: 'Payment received $5,000', timestamp: new Date(), type: 'success' },
          ]}
        />
      </div>
    </div>
  );
}

/**
 * Demo Form Component - Fetches from backend
 */
function DemoForm() {
  const [formMeta, setFormMeta] = useState<FormMetadata | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadForm = async () => {
      try {
        setLoading(true);
        metadataClient.setHeaders('default', 'admin');
        const metadata = await metadataClient.fetchForm('Customer');
        setFormMeta(metadata);
      } catch (err: any) {
        console.error('Error loading form metadata:', err);
        setError(err.message || 'Failed to load form metadata');
      } finally {
        setLoading(false);
      }
    };
    
    loadForm();
  }, []);

  const handleSuccess = async (data: any) => {
    console.log('Form submitted:', data);
    try {
      apiClient.setCredentials('default', 'admin');
      const result = await apiClient.createEntity('Customer', data);
      alert('Customer created successfully!\n\n' + JSON.stringify(result, null, 2));
    } catch (err: any) {
      console.error('Error creating customer:', err);
      alert('Error: ' + (err.response?.data?.error || err.message));
    }
  };

  if (loading) {
    return (
      <div className="bg-white p-6 rounded-lg shadow-lg">
        <div className="flex items-center justify-center h-64">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
            <p className="text-gray-600">Loading form metadata from backend...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white p-6 rounded-lg shadow-lg">
        <div className="bg-red-50 border border-red-200 rounded p-4 text-red-800">
          <h3 className="font-semibold mb-2">Error Loading Form</h3>
          <p>{error}</p>
          <p className="text-sm mt-2">Make sure the backend is running at http://localhost:8081</p>
        </div>
      </div>
    );
  }

  if (!formMeta) {
    return (
      <div className="bg-white p-6 rounded-lg shadow-lg">
        <p className="text-gray-600">No form metadata available</p>
      </div>
    );
  }

  return (
    <div className="bg-white p-6 rounded-lg shadow-lg">
      <FormRenderer formMeta={formMeta} onSuccess={handleSuccess} />
    </div>
  );
}

/**
 * Demo List Component - Fetches from backend
 */
function DemoList() {
  const [listMeta, setListMeta] = useState<ListMetadata | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadList = async () => {
      try {
        setLoading(true);
        metadataClient.setHeaders('default', 'admin');
        const metadata = await metadataClient.fetchList('Customer');
        setListMeta(metadata);
      } catch (err: any) {
        console.error('Error loading list metadata:', err);
        setError(err.message || 'Failed to load list metadata');
      } finally {
        setLoading(false);
      }
    };
    
    loadList();
  }, []);

  if (loading) {
    return (
      <div className="bg-white p-6 rounded-lg shadow-lg">
        <div className="flex items-center justify-center h-64">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
            <p className="text-gray-600">Loading list metadata from backend...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white p-6 rounded-lg shadow-lg">
        <div className="bg-red-50 border border-red-200 rounded p-4 text-red-800">
          <h3 className="font-semibold mb-2">Error Loading List</h3>
          <p>{error}</p>
          <p className="text-sm mt-2">Make sure the backend is running at http://localhost:8081</p>
        </div>
      </div>
    );
  }

  if (!listMeta) {
    return (
      <div className="bg-white p-6 rounded-lg shadow-lg">
        <p className="text-gray-600">No list metadata available</p>
      </div>
    );
  }

  return (
    <div className="bg-white p-6 rounded-lg shadow-lg">
      <ListRenderer listMeta={listMeta} />
    </div>
  );
}

/**
 * Main App Component
 */
function App() {
  const [activeTab, setActiveTab] = useState<'dashboard' | 'form' | 'list' | 'metadata' | 'layout'>('dashboard');
  const setPermissions = usePermissionStore(state => state.setPermissions);

  useEffect(() => {
    // Initialize offline sync
    offlineSyncManager.init().then(() => {
      console.log('Offline sync initialized');
    });

    // Set tenant and user
    metadataClient.setHeaders('default', 'admin');
    apiClient.setCredentials('default', 'admin');
    
    // Set permissions
    setPermissions([
      'Customer:create',
      'Customer:update',
      'Customer:find',
      'Customer:delete',
      'Invoice:create',
      'Invoice:find',
    ]);
    
    console.log('Registered components:', componentRegistry.getAll());
  }, [setPermissions]);

  return (
    <QueryClientProvider client={queryClient}>
      <div className="min-h-screen bg-gray-100">
        <OnlineStatusBanner />
        
        {/* Header */}
        <header className="bg-white shadow-md">
          <div className="container mx-auto px-4 py-4">
            <h1 className="text-3xl font-bold text-gray-800">
              Dynamic Frontend Runtime Engine
            </h1>
            <p className="text-gray-600">Complete metadata-driven system</p>
          </div>
        </header>

        {/* Navigation */}
        <nav className="bg-white border-b border-gray-200 mb-6">
          <div className="container mx-auto px-4">
            <div className="flex gap-4">
              {(['dashboard', 'form', 'list', 'metadata', 'layout'] as const).map((tab) => (
                <button
                  key={tab}
                  onClick={() => setActiveTab(tab)}
                  className={`px-4 py-3 font-medium transition-colors ${
                    activeTab === tab
                      ? 'border-b-2 border-blue-600 text-blue-600'
                      : 'text-gray-600 hover:text-gray-900'
                  }`}
                >
                  {tab.charAt(0).toUpperCase() + tab.slice(1)}
                </button>
              ))}
            </div>
          </div>
        </nav>

        {/* Main Content */}
        <div className="container mx-auto px-4 pb-8">
          {activeTab === 'dashboard' && <DemoDashboard />}
          {activeTab === 'form' && <DemoForm />}
          {activeTab === 'list' && <DemoList />}
          {activeTab === 'metadata' && <MetadataEditor />}
          {activeTab === 'layout' && <LayoutBuilder />}
        </div>
      </div>
    </QueryClientProvider>
  );
}

export default App;
