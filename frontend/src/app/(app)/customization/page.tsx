'use client';

import { useState, useEffect } from 'react';
import { useAuth } from '@/lib/hooks/useAuth';
import { customizationApi, CustomThemeDTO, CustomFieldDTO, TenantConfigurationDTO, CustomFieldEntityType, CustomFieldType } from '@/lib/api';

export default function EnhancedCustomizationPage() {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('theme');
  const [loading, setLoading] = useState(false);
  const [saveMessage, setSaveMessage] = useState('');
  
  // Theme state
  const [theme, setTheme] = useState<CustomThemeDTO>({
    themeName: 'Default Theme',
    primaryColor: '#3B82F6',
    secondaryColor: '#6B7280',
    accentColor: '#8B5CF6',
    backgroundColor: '#FFFFFF',
    textColor: '#1F2937',
    fontFamily: 'Inter',
    companyName: '',
    logoUrl: '',
    faviconUrl: '',
    invoiceHeaderColor: '#3B82F6',
    invoiceFooterColor: '#6B7280',
  });

  // Custom Fields state
  const [customFields, setCustomFields] = useState<CustomFieldDTO[]>([]);
  const [selectedEntityType, setSelectedEntityType] = useState<CustomFieldEntityType>('PRODUCT');
  const [isAddingField, setIsAddingField] = useState(false);
  const [newField, setNewField] = useState<Partial<CustomFieldDTO>>({
    entityType: 'PRODUCT',
    fieldName: '',
    fieldLabel: '',
    fieldType: 'TEXT',
    isRequired: false,
    displayOrder: 0,
  });

  // Configuration state
  const [configurations, setConfigurations] = useState<TenantConfigurationDTO[]>([]);
  const [isAddingConfig, setIsAddingConfig] = useState(false);
  const [newConfig, setNewConfig] = useState<TenantConfigurationDTO>({
    configKey: '',
    configValue: '',
    description: '',
  });

  const tabs = [
    { id: 'theme', label: 'Branding & Theme', icon: '🎨' },
    { id: 'custom-fields', label: 'Custom Fields', icon: '📝' },
    { id: 'configurations', label: 'Configurations', icon: '⚙️' },
  ];

  // Load active theme
  useEffect(() => {
    if (activeTab === 'theme') {
      loadActiveTheme();
    }
  }, [activeTab]);

  // Load custom fields when entity type changes
  useEffect(() => {
    if (activeTab === 'custom-fields') {
      loadCustomFields();
    }
  }, [activeTab, selectedEntityType]);

  // Load configurations
  useEffect(() => {
    if (activeTab === 'configurations') {
      loadConfigurations();
    }
  }, [activeTab]);

  const loadActiveTheme = async () => {
    try {
      setLoading(true);
      const response = await customizationApi.getActiveTheme();
      if (response.success && response.data) {
        setTheme(response.data);
      }
    } catch (error: any) {
      console.log('No active theme, using defaults');
    } finally {
      setLoading(false);
    }
  };

  const loadCustomFields = async () => {
    try {
      setLoading(true);
      const response = await customizationApi.getCustomFields(selectedEntityType);
      if (response.success && response.data) {
        setCustomFields(response.data);
      }
    } catch (error) {
      console.error('Error loading custom fields:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadConfigurations = async () => {
    try {
      setLoading(true);
      const response = await customizationApi.getTenantConfigurations();
      if (response.success && response.data) {
        setConfigurations(response.data);
      }
    } catch (error) {
      console.error('Error loading configurations:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSaveTheme = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setLoading(true);
      setSaveMessage('');
      
      if (theme.id) {
        // Update existing theme
        const response = await customizationApi.updateTheme(theme.id, theme);
        if (response.success) {
          setSaveMessage('Theme updated successfully!');
          await customizationApi.activateTheme(theme.id);
        }
      } else {
        // Create new theme
        const response = await customizationApi.createTheme({ ...theme, isActive: true });
        if (response.success && response.data) {
          setTheme(response.data);
          setSaveMessage('Theme created and activated successfully!');
        }
      }
      
      setTimeout(() => setSaveMessage(''), 3000);
    } catch (error: any) {
      setSaveMessage('Error saving theme: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const handleAddCustomField = async () => {
    try {
      setLoading(true);
      const fieldData: CustomFieldDTO = {
        entityType: selectedEntityType,
        fieldName: newField.fieldName || '',
        fieldLabel: newField.fieldLabel || '',
        fieldType: newField.fieldType || 'TEXT',
        isRequired: newField.isRequired || false,
        defaultValue: newField.defaultValue,
        displayOrder: newField.displayOrder || 0,
        isActive: true,
      };

      const response = await customizationApi.createCustomField(fieldData);
      if (response.success) {
        setSaveMessage('Custom field added successfully!');
        setIsAddingField(false);
        setNewField({
          entityType: selectedEntityType,
          fieldName: '',
          fieldLabel: '',
          fieldType: 'TEXT',
          isRequired: false,
          displayOrder: 0,
        });
        await loadCustomFields();
      }
      setTimeout(() => setSaveMessage(''), 3000);
    } catch (error: any) {
      setSaveMessage('Error adding field: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteCustomField = async (id: number) => {
    if (!confirm('Are you sure you want to delete this custom field?')) return;
    
    try {
      setLoading(true);
      const response = await customizationApi.deleteCustomField(id);
      if (response.success) {
        setSaveMessage('Custom field deleted successfully!');
        await loadCustomFields();
      }
      setTimeout(() => setSaveMessage(''), 3000);
    } catch (error: any) {
      setSaveMessage('Error deleting field: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const handleAddConfiguration = async () => {
    try {
      setLoading(true);
      const response = await customizationApi.createTenantConfiguration(newConfig);
      if (response.success) {
        setSaveMessage('Configuration added successfully!');
        setIsAddingConfig(false);
        setNewConfig({ configKey: '', configValue: '', description: '' });
        await loadConfigurations();
      }
      setTimeout(() => setSaveMessage(''), 3000);
    } catch (error: any) {
      setSaveMessage('Error adding configuration: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateConfiguration = async (key: string, value: string) => {
    try {
      const config = configurations.find(c => c.configKey === key);
      if (!config) return;

      const response = await customizationApi.updateTenantConfiguration(key, {
        ...config,
        configValue: value,
      });
      
      if (response.success) {
        setSaveMessage('Configuration updated!');
        await loadConfigurations();
      }
      setTimeout(() => setSaveMessage(''), 3000);
    } catch (error: any) {
      setSaveMessage('Error updating configuration: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleDeleteConfiguration = async (key: string) => {
    if (!confirm('Are you sure you want to delete this configuration?')) return;
    
    try {
      setLoading(true);
      const response = await customizationApi.deleteTenantConfiguration(key);
      if (response.success) {
        setSaveMessage('Configuration deleted successfully!');
        await loadConfigurations();
      }
      setTimeout(() => setSaveMessage(''), 3000);
    } catch (error: any) {
      setSaveMessage('Error deleting configuration: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const fieldTypes: CustomFieldType[] = ['TEXT', 'NUMBER', 'DATE', 'BOOLEAN', 'DROPDOWN', 'TEXTAREA', 'EMAIL', 'PHONE', 'URL'];
  const entityTypes: CustomFieldEntityType[] = ['PRODUCT', 'CUSTOMER', 'SUPPLIER', 'INVOICE', 'ORDER'];

  return (
    <div className="max-w-7xl mx-auto p-6">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Customization</h1>
        <p className="text-gray-600 mt-2">Customize EasyBilling to match your business needs</p>
      </div>

      {/* Save Message */}
      {saveMessage && (
        <div className={`mb-4 p-4 rounded-lg ${saveMessage.includes('Error') ? 'bg-red-50 text-red-800' : 'bg-green-50 text-green-800'}`}>
          {saveMessage}
        </div>
      )}

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
            
            {/* Branding & Theme Tab */}
            {activeTab === 'theme' && (
              <div>
                <h2 className="text-2xl font-bold text-gray-900 mb-6">Branding & Theme Customization</h2>
                <form onSubmit={handleSaveTheme} className="space-y-6">
                  
                  {/* Brand Identity */}
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900 mb-4">Brand Identity</h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Theme Name</label>
                        <input
                          type="text"
                          value={theme.themeName}
                          onChange={(e) => setTheme(prev => ({ ...prev, themeName: e.target.value }))}
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Company Name</label>
                        <input
                          type="text"
                          value={theme.companyName || ''}
                          onChange={(e) => setTheme(prev => ({ ...prev, companyName: e.target.value }))}
                          placeholder="Your Company Name"
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Logo URL</label>
                        <input
                          type="url"
                          value={theme.logoUrl || ''}
                          onChange={(e) => setTheme(prev => ({ ...prev, logoUrl: e.target.value }))}
                          placeholder="https://example.com/logo.png"
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Favicon URL</label>
                        <input
                          type="url"
                          value={theme.faviconUrl || ''}
                          onChange={(e) => setTheme(prev => ({ ...prev, faviconUrl: e.target.value }))}
                          placeholder="https://example.com/favicon.ico"
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                        />
                      </div>
                    </div>
                  </div>

                  {/* Color Scheme */}
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900 mb-4">Color Scheme</h3>
                    <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                      {[
                        { key: 'primaryColor', label: 'Primary Color' },
                        { key: 'secondaryColor', label: 'Secondary Color' },
                        { key: 'accentColor', label: 'Accent Color' },
                        { key: 'backgroundColor', label: 'Background Color' },
                        { key: 'textColor', label: 'Text Color' },
                        { key: 'invoiceHeaderColor', label: 'Invoice Header' },
                      ].map((color) => (
                        <div key={color.key}>
                          <label className="block text-sm font-medium text-gray-700 mb-1">{color.label}</label>
                          <div className="flex items-center space-x-2">
                            <input
                              type="color"
                              value={theme[color.key as keyof CustomThemeDTO] as string || '#000000'}
                              onChange={(e) => setTheme(prev => ({ ...prev, [color.key]: e.target.value }))}
                              className="h-10 w-16 border border-gray-300 rounded cursor-pointer"
                            />
                            <input
                              type="text"
                              value={theme[color.key as keyof CustomThemeDTO] as string || ''}
                              onChange={(e) => setTheme(prev => ({ ...prev, [color.key]: e.target.value }))}
                              className="flex-1 px-3 py-2 border border-gray-300 rounded-lg"
                              placeholder="#000000"
                            />
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Typography */}
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900 mb-4">Typography</h3>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Font Family</label>
                      <select
                        value={theme.fontFamily}
                        onChange={(e) => setTheme(prev => ({ ...prev, fontFamily: e.target.value }))}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                      >
                        <option value="Inter">Inter</option>
                        <option value="Roboto">Roboto</option>
                        <option value="Arial">Arial</option>
                        <option value="Helvetica">Helvetica</option>
                        <option value="Georgia">Georgia</option>
                        <option value="Times New Roman">Times New Roman</option>
                      </select>
                    </div>
                  </div>

                  {/* Custom CSS */}
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900 mb-4">Custom CSS (Advanced)</h3>
                    <textarea
                      value={theme.customCss || ''}
                      onChange={(e) => setTheme(prev => ({ ...prev, customCss: e.target.value }))}
                      placeholder="/* Add custom CSS here */"
                      rows={6}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 font-mono text-sm"
                    />
                  </div>

                  {/* Save Button */}
                  <div className="flex justify-end">
                    <button
                      type="submit"
                      disabled={loading}
                      className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
                    >
                      {loading ? 'Saving...' : 'Save Theme'}
                    </button>
                  </div>
                </form>

                {/* Preview */}
                <div className="mt-8 p-6 rounded-lg border-2 border-gray-200">
                  <h3 className="text-lg font-semibold mb-4">Live Preview</h3>
                  <div 
                    className="p-6 rounded-lg"
                    style={{
                      backgroundColor: theme.backgroundColor,
                      color: theme.textColor,
                      fontFamily: theme.fontFamily,
                    }}
                  >
                    <h4 style={{ color: theme.primaryColor }} className="text-2xl font-bold mb-2">
                      {theme.companyName || 'Your Company Name'}
                    </h4>
                    <p style={{ color: theme.secondaryColor }}>
                      This is how your customized theme will look across the application.
                    </p>
                    <button
                      style={{ backgroundColor: theme.accentColor }}
                      className="mt-4 px-4 py-2 text-white rounded-lg"
                    >
                      Sample Button
                    </button>
                  </div>
                </div>
              </div>
            )}

            {/* Custom Fields Tab */}
            {activeTab === 'custom-fields' && (
              <div>
                <div className="flex justify-between items-center mb-6">
                  <h2 className="text-2xl font-bold text-gray-900">Custom Fields Management</h2>
                  <button
                    onClick={() => setIsAddingField(true)}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                  >
                    + Add Custom Field
                  </button>
                </div>

                {/* Entity Type Selector */}
                <div className="mb-6">
                  <label className="block text-sm font-medium text-gray-700 mb-2">Entity Type</label>
                  <div className="flex space-x-2">
                    {entityTypes.map((type) => (
                      <button
                        key={type}
                        onClick={() => setSelectedEntityType(type)}
                        className={`px-4 py-2 rounded-lg ${
                          selectedEntityType === type
                            ? 'bg-blue-600 text-white'
                            : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                        }`}
                      >
                        {type}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Add Field Form */}
                {isAddingField && (
                  <div className="mb-6 p-4 bg-gray-50 rounded-lg">
                    <h3 className="text-lg font-semibold mb-4">Add New Custom Field</h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Field Name (Internal)</label>
                        <input
                          type="text"
                          value={newField.fieldName || ''}
                          onChange={(e) => setNewField(prev => ({ ...prev, fieldName: e.target.value }))}
                          placeholder="e.g., manufacturer_part_number"
                          className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Field Label (Display)</label>
                        <input
                          type="text"
                          value={newField.fieldLabel || ''}
                          onChange={(e) => setNewField(prev => ({ ...prev, fieldLabel: e.target.value }))}
                          placeholder="e.g., Manufacturer Part Number"
                          className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Field Type</label>
                        <select
                          value={newField.fieldType || 'TEXT'}
                          onChange={(e) => setNewField(prev => ({ ...prev, fieldType: e.target.value as CustomFieldType }))}
                          className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                        >
                          {fieldTypes.map((type) => (
                            <option key={type} value={type}>{type}</option>
                          ))}
                        </select>
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Default Value</label>
                        <input
                          type="text"
                          value={newField.defaultValue || ''}
                          onChange={(e) => setNewField(prev => ({ ...prev, defaultValue: e.target.value }))}
                          placeholder="Optional default value"
                          className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                        />
                      </div>
                      <div className="flex items-center">
                        <input
                          type="checkbox"
                          checked={newField.isRequired || false}
                          onChange={(e) => setNewField(prev => ({ ...prev, isRequired: e.target.checked }))}
                          className="mr-2"
                        />
                        <label className="text-sm font-medium text-gray-700">Required Field</label>
                      </div>
                    </div>
                    <div className="mt-4 flex space-x-2">
                      <button
                        onClick={handleAddCustomField}
                        disabled={loading || !newField.fieldName || !newField.fieldLabel}
                        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
                      >
                        Add Field
                      </button>
                      <button
                        onClick={() => {
                          setIsAddingField(false);
                          setNewField({
                            entityType: selectedEntityType,
                            fieldName: '',
                            fieldLabel: '',
                            fieldType: 'TEXT',
                            isRequired: false,
                            displayOrder: 0,
                          });
                        }}
                        className="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400"
                      >
                        Cancel
                      </button>
                    </div>
                  </div>
                )}

                {/* Custom Fields List */}
                <div className="space-y-3">
                  {loading ? (
                    <div className="text-center py-8 text-gray-500">Loading custom fields...</div>
                  ) : customFields.length === 0 ? (
                    <div className="text-center py-8 text-gray-500">
                      No custom fields defined for {selectedEntityType}. Click "Add Custom Field" to create one.
                    </div>
                  ) : (
                    customFields.map((field) => (
                      <div key={field.id} className="p-4 border border-gray-200 rounded-lg flex justify-between items-start">
                        <div className="flex-1">
                          <div className="flex items-center space-x-2">
                            <h4 className="font-semibold text-gray-900">{field.fieldLabel}</h4>
                            <span className="px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded">{field.fieldType}</span>
                            {field.isRequired && (
                              <span className="px-2 py-1 text-xs bg-red-100 text-red-800 rounded">Required</span>
                            )}
                          </div>
                          <p className="text-sm text-gray-600 mt-1">Internal Name: {field.fieldName}</p>
                          {field.defaultValue && (
                            <p className="text-sm text-gray-600">Default: {field.defaultValue}</p>
                          )}
                        </div>
                        <button
                          onClick={() => field.id && handleDeleteCustomField(field.id)}
                          className="ml-4 px-3 py-1 bg-red-100 text-red-700 rounded hover:bg-red-200"
                        >
                          Delete
                        </button>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}

            {/* Configurations Tab */}
            {activeTab === 'configurations' && (
              <div>
                <div className="flex justify-between items-center mb-6">
                  <h2 className="text-2xl font-bold text-gray-900">Configuration Management</h2>
                  <button
                    onClick={() => setIsAddingConfig(true)}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                  >
                    + Add Configuration
                  </button>
                </div>

                {/* Add Config Form */}
                {isAddingConfig && (
                  <div className="mb-6 p-4 bg-gray-50 rounded-lg">
                    <h3 className="text-lg font-semibold mb-4">Add New Configuration</h3>
                    <div className="space-y-4">
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Configuration Key</label>
                        <input
                          type="text"
                          value={newConfig.configKey}
                          onChange={(e) => setNewConfig(prev => ({ ...prev, configKey: e.target.value }))}
                          placeholder="e.g., billing.invoice_prefix"
                          className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Configuration Value</label>
                        <input
                          type="text"
                          value={newConfig.configValue}
                          onChange={(e) => setNewConfig(prev => ({ ...prev, configValue: e.target.value }))}
                          placeholder="e.g., INV"
                          className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                        <input
                          type="text"
                          value={newConfig.description || ''}
                          onChange={(e) => setNewConfig(prev => ({ ...prev, description: e.target.value }))}
                          placeholder="Optional description"
                          className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                        />
                      </div>
                    </div>
                    <div className="mt-4 flex space-x-2">
                      <button
                        onClick={handleAddConfiguration}
                        disabled={loading || !newConfig.configKey || !newConfig.configValue}
                        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
                      >
                        Add Configuration
                      </button>
                      <button
                        onClick={() => {
                          setIsAddingConfig(false);
                          setNewConfig({ configKey: '', configValue: '', description: '' });
                        }}
                        className="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400"
                      >
                        Cancel
                      </button>
                    </div>
                  </div>
                )}

                {/* Configurations List */}
                <div className="space-y-3">
                  {loading ? (
                    <div className="text-center py-8 text-gray-500">Loading configurations...</div>
                  ) : configurations.length === 0 ? (
                    <div className="text-center py-8 text-gray-500">
                      No configurations defined. Click "Add Configuration" to create one.
                    </div>
                  ) : (
                    <>
                      {/* Predefined configs with descriptions */}
                      <div className="mb-4">
                        <h3 className="text-sm font-semibold text-gray-700 mb-2">Quick Configurations</h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                          {[
                            { key: 'billing.invoice_prefix', label: 'Invoice Prefix', placeholder: 'INV' },
                            { key: 'customer.loyalty.points_per_rupee', label: 'Loyalty Points/₹', placeholder: '0.01' },
                            { key: 'customer.segment.vip_spending_threshold', label: 'VIP Threshold (₹)', placeholder: '50000' },
                            { key: 'inventory.default_low_stock_threshold', label: 'Low Stock Alert', placeholder: '10' },
                            { key: 'supplier.default_credit_days', label: 'Default Credit Days', placeholder: '30' },
                          ].map((preset) => {
                            const existing = configurations.find(c => c.configKey === preset.key);
                            return (
                              <div key={preset.key} className="p-3 border border-gray-200 rounded-lg">
                                <label className="block text-sm font-medium text-gray-700 mb-1">{preset.label}</label>
                                <div className="flex space-x-2">
                                  <input
                                    type="text"
                                    value={existing?.configValue || ''}
                                    onChange={(e) => handleUpdateConfiguration(preset.key, e.target.value)}
                                    placeholder={preset.placeholder}
                                    className="flex-1 px-3 py-1 border border-gray-300 rounded text-sm"
                                  />
                                  <span className="text-xs text-gray-500 self-center">{preset.key}</span>
                                </div>
                              </div>
                            );
                          })}
                        </div>
                      </div>

                      {/* All configurations */}
                      <h3 className="text-sm font-semibold text-gray-700 mb-2 mt-6">All Configurations</h3>
                      {configurations.map((config) => (
                        <div key={config.id} className="p-4 border border-gray-200 rounded-lg flex justify-between items-start">
                          <div className="flex-1">
                            <div className="flex items-center space-x-2">
                              <h4 className="font-semibold text-gray-900">{config.configKey}</h4>
                            </div>
                            <p className="text-sm text-gray-600 mt-1">Value: {config.configValue}</p>
                            {config.description && (
                              <p className="text-sm text-gray-500 italic">{config.description}</p>
                            )}
                          </div>
                          <button
                            onClick={() => handleDeleteConfiguration(config.configKey)}
                            className="ml-4 px-3 py-1 bg-red-100 text-red-700 rounded hover:bg-red-200"
                          >
                            Delete
                          </button>
                        </div>
                      ))}
                    </>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
