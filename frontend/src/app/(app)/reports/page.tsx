'use client';

import { useState } from 'react';

export default function ReportsPage() {
  const [selectedReport, setSelectedReport] = useState<string>('sales');
  const [dateRange, setDateRange] = useState({ from: '', to: '' });

  const reportTypes = [
    { id: 'sales', name: 'Sales Report', icon: '💰', description: 'Daily, weekly, and monthly sales summary' },
    { id: 'inventory', name: 'Inventory Report', icon: '📦', description: 'Stock levels, movements, and valuation' },
    { id: 'customer', name: 'Customer Report', icon: '👥', description: 'Customer purchases and behavior analysis' },
    { id: 'tax', name: 'Tax Report', icon: '📝', description: 'GST/VAT summary for tax filing' },
    { id: 'profit', name: 'Profit & Loss', icon: '📊', description: 'Revenue, costs, and profitability' },
    { id: 'performance', name: 'Performance Report', icon: '📈', description: 'Staff and counter performance' },
  ];

  const handleGenerateReport = () => {
    console.log('Generating report:', {
      type: selectedReport,
      dateRange,
    });
    alert('Report generation feature coming soon!');
  };

  return (
    <div className="max-w-7xl mx-auto">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Reports & Analytics</h1>
        <p className="text-gray-600 mt-2">Generate comprehensive reports to analyze your business performance</p>
      </div>

      {/* Report Type Selection */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
        {reportTypes.map((report) => (
          <button
            key={report.id}
            onClick={() => setSelectedReport(report.id)}
            className={`text-left p-6 rounded-xl border-2 transition-all ${
              selectedReport === report.id
                ? 'border-blue-600 bg-blue-50 shadow-md'
                : 'border-gray-200 bg-white hover:border-blue-300 hover:shadow-sm'
            }`}
          >
            <div className="text-4xl mb-3">{report.icon}</div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">{report.name}</h3>
            <p className="text-sm text-gray-600">{report.description}</p>
          </button>
        ))}
      </div>

      {/* Report Configuration */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Configure Report</h2>
        
        <div className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">From Date</label>
              <input
                type="date"
                value={dateRange.from}
                onChange={(e) => setDateRange({ ...dateRange, from: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">To Date</label>
              <input
                type="date"
                value={dateRange.to}
                onChange={(e) => setDateRange({ ...dateRange, to: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <div className="flex items-center gap-4 pt-4">
            <button
              onClick={handleGenerateReport}
              className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium"
            >
              Generate Report
            </button>
            <button className="px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 font-medium">
              Export to Excel
            </button>
            <button className="px-6 py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 font-medium">
              Export to PDF
            </button>
          </div>
        </div>
      </div>

      {/* Sample Report Preview */}
      <div className="bg-white rounded-lg shadow-md p-6 mt-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Report Preview</h2>
        <div className="text-center py-12 text-gray-500">
          <div className="text-6xl mb-4">📊</div>
          <p className="text-lg">Select date range and generate a report to see the results here</p>
        </div>
      </div>

      {/* Quick Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mt-6">
        <div className="bg-gradient-to-br from-blue-500 to-blue-600 rounded-lg shadow-md p-6 text-white">
          <p className="text-sm opacity-90">Today's Sales</p>
          <p className="text-3xl font-bold mt-2">₹0.00</p>
          <p className="text-xs opacity-75 mt-2">↑ 0% from yesterday</p>
        </div>
        <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-lg shadow-md p-6 text-white">
          <p className="text-sm opacity-90">This Month</p>
          <p className="text-3xl font-bold mt-2">₹0.00</p>
          <p className="text-xs opacity-75 mt-2">↑ 0% from last month</p>
        </div>
        <div className="bg-gradient-to-br from-purple-500 to-purple-600 rounded-lg shadow-md p-6 text-white">
          <p className="text-sm opacity-90">Total Customers</p>
          <p className="text-3xl font-bold mt-2">0</p>
          <p className="text-xs opacity-75 mt-2">Active customers</p>
        </div>
        <div className="bg-gradient-to-br from-orange-500 to-orange-600 rounded-lg shadow-md p-6 text-white">
          <p className="text-sm opacity-90">Low Stock Items</p>
          <p className="text-3xl font-bold mt-2">0</p>
          <p className="text-xs opacity-75 mt-2">Need reorder</p>
        </div>
      </div>
    </div>
  );
}
