'use client';

import { useState } from 'react';
import { useAuth } from '@/lib/hooks/useAuth';

export default function CustomizationSettingsPage() {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('theme');
  const [theme, setTheme] = useState({
    primaryColor: '#3B82F6',
    secondaryColor: '#6B7280',
    accentColor: '#8B5CF6',
    companyName: '',
    logoUrl: '',
    fontFamily: 'Inter',
  });

  const tabs = [
    { id: 'theme', label: 'Branding & Theme', icon: '🎨' },
    { id: 'custom-fields', label: 'Custom Fields', icon: '📝' },
    { id: 'templates', label: 'Document Templates', icon: '📄' },
    { id: 'workflows', label: 'Workflows', icon: '⚡' },
    { id: 'webhooks', label: 'Webhooks', icon: '🔗' },
    { id: 'configurations', label: 'Configurations', icon: '⚙️' },
  ];

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    alert('Customization settings saved successfully!');
  };

  const handleThemeChange = (field: string, value: string) => {
    setTheme(prev => ({ ...prev, [field]: value }));
  };

  return (
    <div className="max-w-7xl mx-auto p-6">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Customization</h1>
        <p className="text-gray-600 mt-2">Customize EasyBilling to match your business needs</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        {/* Sidebar Navigation */}
        <div className="lg:col-span-1">
          <nav className="bg-white rounded-lg shadow-md p-4 space-y-2">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`w-full flex items-center space-x-3 px-4 py-3 rounded-lg text-left transition-colors ${
                  activeTab === tab.id
                    ? 'bg-blue-50 text-blue-700 font-medium'
                    : 'text-gray-700 hover:bg-gray-50'
                }`}
              >
                <span className="text-xl">{tab.icon}</span>
                <span>{tab.label}</span>
              </button>
            ))}
          </nav>
        </div>

        {/* Main Content */}
        <div className="lg:col-span-3">
          <div className="bg-white rounded-lg shadow-md p-6">
            
            {/* Branding & Theme */}
            {activeTab === 'theme' && (
              <div>
                <h2 className="text-2xl font-bold text-gray-900 mb-6">Branding & Theme Customization</h2>
                <form onSubmit={handleSave} className="space-y-6">
                  
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900 mb-4">Brand Identity</h3>
                    <div className="space-y-4">
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Company Name</label>
                        <input
                          type="text"
                          value={theme.companyName}
                          onChange={(e) => handleThemeChange('companyName', e.target.value)}
                          placeholder="Your Company Name"
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Logo URL</label>
                        <input
                          type="url"
                          value={theme.logoUrl}
                          onChange={(e) => handleThemeChange('logoUrl', e.target.value)}
                          placeholder="https://example.com/logo.png"
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                        />
                      </div>
                    </div>
                  </div>

                  <div>
                    <h3 className="text-lg font-semibold text-gray-900 mb-4">Color Scheme</h3>
                    <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Primary Color</label>
                        <div className="flex items-center space-x-2">
                          <input
                            type="color"
                            value={theme.primaryColor}
                            onChange={(e) => handleThemeChange('primaryColor', e.target.value)}
                            className="w-12 h-12 rounded border border-gray-300 cursor-pointer"
                          />
                          <input
                            type="text"
                            value={theme.primaryColor}
                            onChange={(e) => handleThemeChange('primaryColor', e.target.value)}
                            className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm"
                          />
                        </div>
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Secondary Color</label>
                        <div className="flex items-center space-x-2">
                          <input
                            type="color"
                            value={theme.secondaryColor}
                            onChange={(e) => handleThemeChange('secondaryColor', e.target.value)}
                            className="w-12 h-12 rounded border border-gray-300 cursor-pointer"
                          />
                          <input
                            type="text"
                            value={theme.secondaryColor}
                            onChange={(e) => handleThemeChange('secondaryColor', e.target.value)}
                            className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm"
                          />
                        </div>
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Accent Color</label>
                        <div className="flex items-center space-x-2">
                          <input
                            type="color"
                            value={theme.accentColor}
                            onChange={(e) => handleThemeChange('accentColor', e.target.value)}
                            className="w-12 h-12 rounded border border-gray-300 cursor-pointer"
                          />
                          <input
                            type="text"
                            value={theme.accentColor}
                            onChange={(e) => handleThemeChange('accentColor', e.target.value)}
                            className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm"
                          />
                        </div>
                      </div>
                    </div>
                  </div>

                  <div>
                    <h3 className="text-lg font-semibold text-gray-900 mb-4">Typography</h3>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Font Family</label>
                        <select 
                          value={theme.fontFamily}
                          onChange={(e) => handleThemeChange('fontFamily', e.target.value)}
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500">
                          <option value="Inter">Inter</option>
                          <option value="Roboto">Roboto</option>
                          <option value="Poppins">Poppins</option>
                          <option value="Open Sans">Open Sans</option>
                          <option value="Lato">Lato</option>
                        </select>
                      </div>
                    </div>
                  </div>

                  <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                    <h4 className="font-semibold text-blue-900 mb-2">Preview</h4>
                    <div className="bg-white rounded-lg p-4 border border-gray-200">
                      <div style={{ color: theme.primaryColor }} className="text-lg font-bold mb-2">
                        {theme.companyName || 'Your Company Name'}
                      </div>
                      <button 
                        style={{ backgroundColor: theme.primaryColor }}
                        className="px-4 py-2 text-white rounded-lg"
                      >
                        Primary Button
                      </button>
                      <button 
                        style={{ backgroundColor: theme.accentColor }}
                        className="px-4 py-2 text-white rounded-lg ml-2"
                      >
                        Accent Button
                      </button>
                    </div>
                  </div>

                  <button
                    type="submit"
                    className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium"
                  >
                    Save Theme Settings
                  </button>
                </form>
              </div>
            )}

            {/* Custom Fields */}
            {activeTab === 'custom-fields' && (
              <div>
                <h2 className="text-2xl font-bold text-gray-900 mb-6">Custom Fields Management</h2>
                <div className="space-y-4">
                  <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                    <p className="text-sm text-blue-800">
                      Add custom fields to invoices, customers, products, and more. Custom fields allow you to capture 
                      business-specific data without code changes.
                    </p>
                  </div>
                  
                  <div className="flex justify-between items-center">
                    <h3 className="text-lg font-semibold text-gray-900">Existing Custom Fields</h3>
                    <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
                      + Add Custom Field
                    </button>
                  </div>

                  <div className="border border-gray-200 rounded-lg overflow-hidden">
                    <table className="w-full">
                      <thead className="bg-gray-50">
                        <tr>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Field Name</th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Entity Type</th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Field Type</th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Required</th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-200">
                        <tr>
                          <td className="px-6 py-4 text-sm text-gray-500 text-center" colSpan={5}>
                            No custom fields defined yet. Click &quot;Add Custom Field&quot; to create one.
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            )}

            {/* Document Templates */}
            {activeTab === 'templates' && (
              <div>
                <h2 className="text-2xl font-bold text-gray-900 mb-6">Document Templates</h2>
                <div className="space-y-4">
                  <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                    <p className="text-sm text-blue-800">
                      Customize your invoice, receipt, and other document templates to match your branding 
                      and business requirements.
                    </p>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {['Invoice', 'Receipt', 'Quotation', 'Delivery Note'].map((templateType) => (
                      <div key={templateType} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
                        <h4 className="font-semibold text-gray-900 mb-2">{templateType} Template</h4>
                        <p className="text-sm text-gray-600 mb-4">Customize your {templateType.toLowerCase()} layout and design</p>
                        <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm">
                          Edit Template
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {/* Workflows */}
            {activeTab === 'workflows' && (
              <div>
                <h2 className="text-2xl font-bold text-gray-900 mb-6">Workflow Automation</h2>
                <div className="space-y-4">
                  <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                    <p className="text-sm text-blue-800">
                      Create automated workflows to streamline your business processes. Workflows can send emails, 
                      create tasks, update records, and more based on triggers and conditions.
                    </p>
                  </div>
                  
                  <div className="flex justify-between items-center">
                    <h3 className="text-lg font-semibold text-gray-900">Active Workflows</h3>
                    <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
                      + Create Workflow
                    </button>
                  </div>

                  <div className="space-y-3">
                    <div className="border border-gray-200 rounded-lg p-4">
                      <div className="flex justify-between items-start mb-2">
                        <div>
                          <h4 className="font-semibold text-gray-900">Send Invoice Email</h4>
                          <p className="text-sm text-gray-600">When: Invoice is created</p>
                        </div>
                        <span className="px-2 py-1 bg-green-100 text-green-800 text-xs rounded-full">Active</span>
                      </div>
                      <p className="text-sm text-gray-500">Action: Send email to customer with invoice PDF</p>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Webhooks */}
            {activeTab === 'webhooks' && (
              <div>
                <h2 className="text-2xl font-bold text-gray-900 mb-6">Webhooks</h2>
                <div className="space-y-4">
                  <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                    <p className="text-sm text-blue-800">
                      Configure webhooks to send real-time notifications to external systems when events occur 
                      in EasyBilling.
                    </p>
                  </div>
                  
                  <div className="flex justify-between items-center">
                    <h3 className="text-lg font-semibold text-gray-900">Configured Webhooks</h3>
                    <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
                      + Add Webhook
                    </button>
                  </div>

                  <div className="border border-gray-200 rounded-lg overflow-hidden">
                    <table className="w-full">
                      <thead className="bg-gray-50">
                        <tr>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name</th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Event</th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">URL</th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-200">
                        <tr>
                          <td className="px-6 py-4 text-sm text-gray-500 text-center" colSpan={5}>
                            No webhooks configured yet. Click &quot;Add Webhook&quot; to create one.
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            )}

            {/* Configurations */}
            {activeTab === 'configurations' && (
              <div>
                <h2 className="text-2xl font-bold text-gray-900 mb-6">Advanced Configurations</h2>
                <div className="space-y-6">
                  <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                    <p className="text-sm text-blue-800">
                      Fine-tune system behavior with advanced configuration options. These settings override 
                      system defaults for your tenant.
                    </p>
                  </div>

                  <div>
                    <h3 className="text-lg font-semibold text-gray-900 mb-4">Billing Configurations</h3>
                    <div className="space-y-4">
                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-1">Invoice Prefix</label>
                          <input
                            type="text"
                            defaultValue="INV"
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                          />
                        </div>
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-1">Default Payment Terms (Days)</label>
                          <input
                            type="number"
                            defaultValue="30"
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                          />
                        </div>
                      </div>
                    </div>
                  </div>

                  <div>
                    <h3 className="text-lg font-semibold text-gray-900 mb-4">Customer Configurations</h3>
                    <div className="space-y-4">
                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-1">Loyalty Points per Rupee</label>
                          <input
                            type="number"
                            step="0.01"
                            defaultValue="0.01"
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                          />
                        </div>
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-1">Minimum Redemption Points</label>
                          <input
                            type="number"
                            defaultValue="100"
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                          />
                        </div>
                      </div>
                    </div>
                  </div>

                  <button
                    onClick={handleSave}
                    className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium"
                  >
                    Save Configurations
                  </button>
                </div>
              </div>
            )}

          </div>
        </div>
      </div>
    </div>
  );
}
