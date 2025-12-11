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
 * Demo Form Component
 */
function DemoForm() {
  const [formMeta] = useState<FormMetadata>({
    id: 'customer.form.basic',
    entity: 'Customer',
    title: 'Create Customer',
    layout: {
      type: 'two-column',
      areas: [
        { name: 'left', width: 60 },
        { name: 'right', width: 40 },
      ],
    },
    fields: [
      {
        name: 'customerName',
        component: 'Text',
        label: 'Customer Name',
        required: true,
        validation: { minLength: 2, maxLength: 100 },
      },
      {
        name: 'email',
        component: 'Text',
        label: 'Email Address',
        required: true,
        validation: { pattern: '^[A-Za-z0-9+_.-]+@(.+)$' },
      },
      {
        name: 'phoneNumber',
        component: 'Text',
        label: 'Phone Number',
        required: true,
        validation: { maxLength: 20 },
      },
      {
        name: 'address',
        component: 'TextArea',
        label: 'Address',
      },
      {
        name: 'balance',
        component: 'Number',
        label: 'Account Balance',
        validation: { min: -999999999, max: 999999999 },
      },
      {
        name: 'creditLimit',
        component: 'Number',
        label: 'Credit Limit',
        validation: { min: 0, max: 999999999 },
      },
      {
        name: 'status',
        component: 'Select',
        label: 'Status',
        optionsSource: {
          type: 'data',
          data: [
            { value: 'active', label: 'Active' },
            { value: 'inactive', label: 'Inactive' },
            { value: 'suspended', label: 'Suspended' },
            { value: 'blocked', label: 'Blocked' },
          ],
        },
      },
    ],
    actions: [
      {
        id: 'save',
        label: 'Save Customer',
        type: 'submit',
        permission: 'entity:Customer:write',
      },
    ],
  });

  const handleSuccess = async (data: any) => {
    console.log('Form submitted successfully:', data);
    alert('Customer created!\n\n' + JSON.stringify(data, null, 2));
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-lg">
      <FormRenderer formMeta={formMeta} onSuccess={handleSuccess} />
    </div>
  );
}

/**
 * Demo List Component
 */
function DemoList() {
  const [listMeta] = useState<ListMetadata>({
    id: 'customer.list',
    entity: 'Customer',
    columns: [
      { field: 'customerName', label: 'Customer Name', sortable: true },
      { field: 'email', label: 'Email', sortable: true },
      { field: 'phoneNumber', label: 'Phone', sortable: false },
      { field: 'balance', label: 'Balance', sortable: true, format: 'currency' },
      { field: 'status', label: 'Status', sortable: true },
    ],
    pageSize: 10,
    filters: [
      { name: 'customerName', component: 'Text', label: 'Name' },
    ],
    rowActions: [
      { id: 'edit', label: 'Edit', type: 'navigate' },
      { id: 'delete', label: 'Delete', type: 'action' },
    ],
  });

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
