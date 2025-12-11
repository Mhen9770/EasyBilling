'use client';

import { useState } from 'react';
import { Card, CardHeader, CardTitle, CardContent, Button, Input, Select, Badge } from '@/components/ui';
import { useToastStore } from '@/components/ui/toast';

export default function EnhancedSettingsPage() {
  const { addToast } = useToastStore();
  const [activeTab, setActiveTab] = useState<'business' | 'billing' | 'notifications' | 'security'>('business');

  const tabs = [
    { id: 'business', label: 'Business Profile', icon: '🏢' },
    { id: 'billing', label: 'Billing Settings', icon: '💰' },
    { id: 'notifications', label: 'Notifications', icon: '🔔' },
    { id: 'security', label: 'Security', icon: '🔒' },
  ];

  const handleSave = () => {
    addToast('Settings saved successfully', 'success');
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Settings</h1>
        <p className="text-gray-600 mt-2">Manage your business configuration and preferences</p>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 overflow-x-auto pb-2">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id as any)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg font-medium whitespace-nowrap transition-all ${
              activeTab === tab.id
                ? 'bg-blue-600 text-white shadow-md'
                : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'
            }`}
          >
            <span>{tab.icon}</span>
            <span>{tab.label}</span>
          </button>
        ))}
      </div>

      {/* Business Profile */}
      {activeTab === 'business' && (
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Business Information</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <Input
                    label="Business Name"
                    placeholder="Enter business name"
                    defaultValue="EasyBilling Store"
                    required
                    fullWidth
                  />
                  <Input
                    label="GSTIN"
                    placeholder="Enter GSTIN"
                    defaultValue="27AABCU9603R1ZX"
                    fullWidth
                  />
                </div>

                <Input
                  label="Address"
                  placeholder="Enter business address"
                  defaultValue="123 Main Street, Mumbai"
                  fullWidth
                />

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <Input
                    label="City"
                    placeholder="City"
                    defaultValue="Mumbai"
                    fullWidth
                  />
                  <Input
                    label="State"
                    placeholder="State"
                    defaultValue="Maharashtra"
                    fullWidth
                  />
                  <Input
                    label="PIN Code"
                    placeholder="PIN"
                    defaultValue="400001"
                    fullWidth
                  />
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <Input
                    label="Phone"
                    type="tel"
                    placeholder="Contact number"
                    defaultValue="+91 9876543210"
                    fullWidth
                  />
                  <Input
                    label="Email"
                    type="email"
                    placeholder="Business email"
                    defaultValue="contact@easybilling.com"
                    fullWidth
                  />
                </div>

                <Button variant="primary" onClick={handleSave}>
                  Save Business Information
                </Button>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Store Settings</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <Select
                  label="Default Currency"
                  options={[
                    { value: 'INR', label: 'Indian Rupee (₹)' },
                    { value: 'USD', label: 'US Dollar ($)' },
                    { value: 'EUR', label: 'Euro (€)' },
                  ]}
                  defaultValue="INR"
                  fullWidth
                />

                <Select
                  label="Tax Type"
                  options={[
                    { value: 'GST', label: 'GST (India)' },
                    { value: 'VAT', label: 'VAT' },
                    { value: 'SALES_TAX', label: 'Sales Tax' },
                  ]}
                  defaultValue="GST"
                  fullWidth
                />

                <Select
                  label="Financial Year Start"
                  options={[
                    { value: '1', label: 'January' },
                    { value: '4', label: 'April' },
                    { value: '7', label: 'July' },
                  ]}
                  defaultValue="4"
                  fullWidth
                />

                <Button variant="primary" onClick={handleSave}>
                  Save Store Settings
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Billing Settings */}
      {activeTab === 'billing' && (
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Invoice Configuration</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <Input
                  label="Invoice Prefix"
                  placeholder="INV"
                  defaultValue="INV"
                  helperText="Prefix for invoice numbers (e.g., INV-2024-001)"
                  fullWidth
                />

                <Input
                  label="Invoice Starting Number"
                  type="number"
                  placeholder="1"
                  defaultValue="1"
                  fullWidth
                />

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <Select
                    label="Default Payment Terms"
                    options={[
                      { value: 'immediate', label: 'Immediate' },
                      { value: 'net15', label: 'Net 15 Days' },
                      { value: 'net30', label: 'Net 30 Days' },
                      { value: 'net60', label: 'Net 60 Days' },
                    ]}
                    defaultValue="immediate"
                    fullWidth
                  />

                  <Select
                    label="Invoice Template"
                    options={[
                      { value: 'standard', label: 'Standard' },
                      { value: 'detailed', label: 'Detailed' },
                      { value: 'minimal', label: 'Minimal' },
                    ]}
                    defaultValue="standard"
                    fullWidth
                  />
                </div>

                <div className="space-y-2">
                  <label className="flex items-center gap-2">
                    <input type="checkbox" defaultChecked className="rounded" />
                    <span className="text-sm font-medium text-gray-700">Show company logo on invoice</span>
                  </label>
                  <label className="flex items-center gap-2">
                    <input type="checkbox" defaultChecked className="rounded" />
                    <span className="text-sm font-medium text-gray-700">Auto-generate invoice number</span>
                  </label>
                  <label className="flex items-center gap-2">
                    <input type="checkbox" className="rounded" />
                    <span className="text-sm font-medium text-gray-700">Enable recurring invoices</span>
                  </label>
                </div>

                <Button variant="primary" onClick={handleSave}>
                  Save Invoice Settings
                </Button>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Payment Methods</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {[
                  { name: 'Cash', enabled: true, icon: '💵' },
                  { name: 'Card', enabled: true, icon: '💳' },
                  { name: 'UPI', enabled: true, icon: '📱' },
                  { name: 'Bank Transfer', enabled: false, icon: '🏦' },
                  { name: 'Wallet', enabled: true, icon: '👛' },
                  { name: 'Credit', enabled: false, icon: '📋' },
                ].map((method) => (
                  <div key={method.name} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                    <div className="flex items-center gap-3">
                      <span className="text-2xl">{method.icon}</span>
                      <span className="font-medium text-gray-900">{method.name}</span>
                    </div>
                    <Badge variant={method.enabled ? 'success' : 'default'}>
                      {method.enabled ? 'Enabled' : 'Disabled'}
                    </Badge>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Notifications */}
      {activeTab === 'notifications' && (
        <Card>
          <CardHeader>
            <CardTitle>Notification Preferences</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div>
                <h3 className="font-semibold text-gray-900 mb-3">Email Notifications</h3>
                <div className="space-y-2">
                  {[
                    'New order placed',
                    'Low stock alerts',
                    'Daily sales summary',
                    'Weekly reports',
                    'Customer feedback',
                  ].map((item) => (
                    <label key={item} className="flex items-center gap-2">
                      <input type="checkbox" defaultChecked className="rounded" />
                      <span className="text-sm text-gray-700">{item}</span>
                    </label>
                  ))}
                </div>
              </div>

              <div>
                <h3 className="font-semibold text-gray-900 mb-3">SMS Notifications</h3>
                <div className="space-y-2">
                  {[
                    'Order confirmations',
                    'Payment receipts',
                    'Stock alerts',
                  ].map((item) => (
                    <label key={item} className="flex items-center gap-2">
                      <input type="checkbox" className="rounded" />
                      <span className="text-sm text-gray-700">{item}</span>
                    </label>
                  ))}
                </div>
              </div>

              <Button variant="primary" onClick={handleSave}>
                Save Notification Settings
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Security */}
      {activeTab === 'security' && (
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Change Password</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4 max-w-md">
                <Input
                  label="Current Password"
                  type="password"
                  placeholder="Enter current password"
                  required
                  fullWidth
                />
                <Input
                  label="New Password"
                  type="password"
                  placeholder="Enter new password"
                  required
                  fullWidth
                />
                <Input
                  label="Confirm Password"
                  type="password"
                  placeholder="Confirm new password"
                  required
                  fullWidth
                />
                <Button variant="primary" onClick={handleSave}>
                  Update Password
                </Button>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Security Options</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div>
                    <p className="font-medium text-gray-900">Two-Factor Authentication</p>
                    <p className="text-sm text-gray-500">Add an extra layer of security</p>
                  </div>
                  <Badge variant="default">Not Enabled</Badge>
                </div>
                
                <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div>
                    <p className="font-medium text-gray-900">Session Timeout</p>
                    <p className="text-sm text-gray-500">Auto-logout after inactivity</p>
                  </div>
                  <Badge variant="success">30 minutes</Badge>
                </div>

                <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div>
                    <p className="font-medium text-gray-900">Login Notifications</p>
                    <p className="text-sm text-gray-500">Get notified of new logins</p>
                  </div>
                  <Badge variant="success">Enabled</Badge>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}
