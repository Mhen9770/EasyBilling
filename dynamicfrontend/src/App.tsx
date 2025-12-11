/**
 * Main Application Entry Point
 * 
 * Initializes the component registry and renders the app.
 */

import React, { useEffect, useState } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import componentRegistry from './engine/registry';
import { FormRenderer } from './engine/renderer';
import { metadataClient, FormMetadata } from './engine/metadataClient';

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

  const handleError = (errors: any) => {
    console.error('Form submission failed:', errors);
  };

  return (
    <div className="max-w-4xl mx-auto p-6 bg-white rounded-lg shadow-lg">
      <FormRenderer
        formMeta={formMeta}
        onSuccess={handleSuccess}
        onError={handleError}
      />
    </div>
  );
}

/**
 * Main App Component
 */
function App() {
  useEffect(() => {
    // Set tenant and user for API calls
    metadataClient.setHeaders('default', 'admin');
    
    // Log registered components
    console.log('Registered components:', componentRegistry.getAll());
  }, []);

  return (
    <QueryClientProvider client={queryClient}>
      <div className="min-h-screen bg-gray-100 py-8">
        <div className="container mx-auto px-4">
          <h1 className="text-4xl font-bold text-center mb-2 text-gray-800">
            Dynamic Frontend Runtime Engine
          </h1>
          <p className="text-center text-gray-600 mb-8">
            Metadata-driven UI rendering with React + TypeScript
          </p>
          
          <div className="mb-8 p-4 bg-blue-50 border border-blue-200 rounded-lg">
            <h2 className="text-lg font-semibold text-blue-900 mb-2">Phase 0 - Scaffolding Complete</h2>
            <ul className="list-disc list-inside text-blue-800 space-y-1">
              <li>✅ Component Registry initialized</li>
              <li>✅ Metadata Client with caching (Zustand)</li>
              <li>✅ Form Renderer with dynamic validation (Zod)</li>
              <li>✅ Primitive components (Text, Number, Select, Checkbox, Date)</li>
              <li>✅ React Hook Form integration</li>
              <li>✅ Tailwind CSS styling</li>
            </ul>
          </div>
          
          <DemoForm />
          
          <div className="mt-8 p-4 bg-gray-50 border border-gray-200 rounded-lg">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Next Steps</h3>
            <ul className="list-disc list-inside text-gray-700 space-y-1">
              <li>Phase 1: List/Table renderer with pagination</li>
              <li>Phase 2: Workflow UI and action buttons</li>
              <li>Phase 3: Plugin loader and widgets</li>
              <li>Phase 4: Admin UI for metadata editing</li>
            </ul>
          </div>
        </div>
      </div>
    </QueryClientProvider>
  );
}

export default App;
