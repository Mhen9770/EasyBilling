/**
 * Main Application Entry Point
 * 
 * Initializes the component registry and renders the app.
 */

import React, { useEffect, useState } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import componentRegistry from './engine/registry';
import { FormRenderer } from './engine/renderer';
import { ListRenderer } from './engine/listRenderer';
import { WorkflowRunner } from './engine/workflowUI';
import { metadataClient, FormMetadata, ListMetadata, WorkflowMetadata } from './engine/metadataClient';
import { usePermissionStore } from './engine/permissions';
import { MetadataEditor } from './pages/admin/MetadataEditor';
import { KPICard, QuickActionsWidget, ActivityFeedWidget } from './components/widgets/Widgets';

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
 * Demo Dashboard Component
 */
function DemoDashboard() {
  return (
    <div className="grid grid-cols-4 gap-6 mb-8">
      <KPICard title="Total Customers" value="1,234" change={12.5} icon="👥" />
      <KPICard title="Active Invoices" value="$45,678" change={-3.2} icon="📄" />
      <KPICard title="Pending Payments" value="23" change={5.7} icon="💰" />
      <KPICard title="Products" value="567" change={8.1} icon="📦" />
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
      {
        id: 'cancel',
        label: 'Cancel',
        type: 'action',
      },
    ],
  });

  const handleSuccess = (data: any) => {
    console.log('Form submitted successfully:', data);
    alert('Customer created successfully!\n\n' + JSON.stringify(data, null, 2));
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-lg">
      <FormRenderer
        formMeta={formMeta}
        onSuccess={handleSuccess}
      />
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
      { name: 'status', component: 'Select', label: 'Status' },
    ],
    rowActions: [
      { id: 'edit', label: 'Edit', type: 'navigate', target: '/customer/edit/{id}' },
      { id: 'delete', label: 'Delete', type: 'action' },
    ],
  });

  const handleRowClick = (row: any) => {
    console.log('Row clicked:', row);
  };

  const handleRowAction = (action: string, row: any) => {
    console.log('Action:', action, 'Row:', row);
    alert(`Action: ${action}\nRow: ${JSON.stringify(row, null, 2)}`);
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-lg">
      <ListRenderer
        listMeta={listMeta}
        onRowClick={handleRowClick}
        onRowAction={handleRowAction}
      />
    </div>
  );
}

/**
 * Main App Component
 */
function App() {
  const [activeTab, setActiveTab] = useState<'dashboard' | 'form' | 'list' | 'admin'>('dashboard');
  const setPermissions = usePermissionStore(state => state.setPermissions);

  useEffect(() => {
    // Set tenant and user for API calls
    metadataClient.setHeaders('default', 'admin');
    
    // Set user permissions (simulate admin user)
    setPermissions([
      'Customer:create',
      'Customer:update',
      'Customer:find',
      'Customer:delete',
      'Invoice:create',
      'Invoice:update',
      'Invoice:find',
      'Product:create',
      'Product:find',
    ]);
    
    // Log registered components
    console.log('Registered components:', componentRegistry.getAll());
  }, [setPermissions]);

  return (
    <QueryClientProvider client={queryClient}>
      <div className="min-h-screen bg-gray-100">
        {/* Header */}
        <header className="bg-white shadow-md">
          <div className="container mx-auto px-4 py-4">
            <h1 className="text-3xl font-bold text-gray-800">
              Dynamic Frontend Runtime Engine
            </h1>
            <p className="text-gray-600">Metadata-driven UI rendering</p>
          </div>
        </header>

        {/* Navigation */}
        <nav className="bg-white border-b border-gray-200 mb-6">
          <div className="container mx-auto px-4">
            <div className="flex gap-4">
              {(['dashboard', 'form', 'list', 'admin'] as const).map((tab) => (
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
          {activeTab === 'dashboard' && (
            <>
              <div className="mb-8 p-4 bg-blue-50 border border-blue-200 rounded-lg">
                <h2 className="text-lg font-semibold text-blue-900 mb-2">
                  ✅ Complete Implementation
                </h2>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <h3 className="font-semibold text-blue-800 mb-2">Phase 0-3 Complete:</h3>
                    <ul className="list-disc list-inside text-blue-800 space-y-1 text-sm">
                      <li>Component Registry ✓</li>
                      <li>Metadata Client with Zustand ✓</li>
                      <li>Form Renderer with Zod ✓</li>
                      <li>List/Table Renderer ✓</li>
                      <li>Workflow UI ✓</li>
                      <li>Permission System ✓</li>
                      <li>Plugin Loader ✓</li>
                      <li>Widgets (KPI, Chart, Actions) ✓</li>
                      <li>Admin Metadata Editor ✓</li>
                    </ul>
                  </div>
                  <div>
                    <h3 className="font-semibold text-blue-800 mb-2">Features:</h3>
                    <ul className="list-disc list-inside text-blue-800 space-y-1 text-sm">
                      <li>Pagination & Sorting ✓</li>
                      <li>Dynamic Validation ✓</li>
                      <li>Action Buttons ✓</li>
                      <li>Row Actions ✓</li>
                      <li>Filtering ✓</li>
                      <li>Import/Export Metadata ✓</li>
                      <li>Visual Form Builder ✓</li>
                    </ul>
                  </div>
                </div>
              </div>
              <DemoDashboard />
              <div className="grid grid-cols-2 gap-6">
                <QuickActionsWidget
                  actions={[
                    { label: 'New Customer', icon: '👤', onClick: () => setActiveTab('form'), color: 'primary' },
                    { label: 'View All', icon: '📋', onClick: () => setActiveTab('list') },
                    { label: 'New Invoice', icon: '📄', onClick: () => alert('Create Invoice') },
                    { label: 'Settings', icon: '⚙️', onClick: () => setActiveTab('admin') },
                  ]}
                />
                <ActivityFeedWidget
                  activities={[
                    { id: '1', message: 'Customer "Acme Corp" created', timestamp: new Date(), type: 'success' },
                    { id: '2', message: 'Invoice #1234 generated', timestamp: new Date(), type: 'info' },
                    { id: '3', message: 'Payment received for Invoice #1233', timestamp: new Date(), type: 'success' },
                  ]}
                />
              </div>
            </>
          )}

          {activeTab === 'form' && <DemoForm />}
          {activeTab === 'list' && <DemoList />}
          {activeTab === 'admin' && <MetadataEditor />}
        </div>
      </div>
    </QueryClientProvider>
  );
}

export default App;
