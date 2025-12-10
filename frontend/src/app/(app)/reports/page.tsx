'use client';

import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { reportsApi, type ReportType } from '@/lib/api/reports/reportsApi';
import { useToastStore } from '@/components/ui/toast';

export default function ReportsPage() {
  const [selectedReport, setSelectedReport] = useState<ReportType>('SALES');
  const [dateRange, setDateRange] = useState({ 
    from: new Date(new Date().setDate(1)).toISOString().split('T')[0], 
    to: new Date().toISOString().split('T')[0] 
  });
  const [reportData, setReportData] = useState<any>(null);
  const { addToast } = useToastStore();

  const reportTypes = [
    { id: 'SALES' as ReportType, name: 'Sales Report', icon: '💰', description: 'Daily, weekly, and monthly sales summary' },
    { id: 'INVENTORY' as ReportType, name: 'Inventory Report', icon: '📦', description: 'Stock levels, movements, and valuation' },
    { id: 'CUSTOMER' as ReportType, name: 'Customer Report', icon: '👥', description: 'Customer purchases and behavior analysis' },
    { id: 'TAX' as ReportType, name: 'Tax Report', icon: '📝', description: 'GST/VAT summary for tax filing' },
    { id: 'PROFIT_LOSS' as ReportType, name: 'Profit & Loss', icon: '📊', description: 'Revenue, costs, and profitability' },
    { id: 'PERFORMANCE' as ReportType, name: 'Performance Report', icon: '📈', description: 'Staff and counter performance' },
  ];

  const generateMutation = useMutation({
    mutationFn: () => reportsApi.generateReport({
      reportType: selectedReport,
      startDate: dateRange.from,
      endDate: dateRange.to,
    }),
    onSuccess: (response) => {
      setReportData(response.data);
      addToast('Report generated successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to generate report', 'error');
    },
  });

  const handleGenerateReport = () => {
    if (!dateRange.from || !dateRange.to) {
      addToast('Please select date range', 'warning');
      return;
    }
    generateMutation.mutate();
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
              disabled={generateMutation.isPending}
              className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium disabled:opacity-50"
            >
              {generateMutation.isPending ? 'Generating...' : 'Generate Report'}
            </button>
            {reportData && (
              <>
                <button className="px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 font-medium">
                  Export to Excel
                </button>
                <button className="px-6 py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 font-medium">
                  Export to PDF
                </button>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Report Preview */}
      {reportData && (
        <div className="bg-white rounded-lg shadow-md p-6 mt-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Report Preview</h2>
          
          {selectedReport === 'SALES' && reportData.summary && (
            <div className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div className="bg-blue-50 p-4 rounded-lg">
                  <div className="text-sm text-blue-600 font-medium">Total Sales</div>
                  <div className="text-2xl font-bold text-blue-900 mt-2">
                    ₹{reportData.summary.totalSales?.toFixed(2)}
                  </div>
                </div>
                <div className="bg-green-50 p-4 rounded-lg">
                  <div className="text-sm text-green-600 font-medium">Total Orders</div>
                  <div className="text-2xl font-bold text-green-900 mt-2">
                    {reportData.summary.totalOrders}
                  </div>
                </div>
                <div className="bg-purple-50 p-4 rounded-lg">
                  <div className="text-sm text-purple-600 font-medium">Avg Order Value</div>
                  <div className="text-2xl font-bold text-purple-900 mt-2">
                    ₹{reportData.summary.averageOrderValue?.toFixed(2)}
                  </div>
                </div>
                <div className="bg-yellow-50 p-4 rounded-lg">
                  <div className="text-sm text-yellow-600 font-medium">Profit Margin</div>
                  <div className="text-2xl font-bold text-yellow-900 mt-2">
                    {reportData.summary.profitMargin?.toFixed(1)}%
                  </div>
                </div>
              </div>
              
              {reportData.topProducts && reportData.topProducts.length > 0 && (
                <div>
                  <h3 className="text-lg font-semibold mb-3">Top Products</h3>
                  <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                      <thead className="bg-gray-50">
                        <tr>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Product</th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Quantity Sold</th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Revenue</th>
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-200">
                        {reportData.topProducts.map((product: any, idx: number) => (
                          <tr key={idx}>
                            <td className="px-6 py-4 whitespace-nowrap text-sm">{product.productName}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm">{product.quantitySold}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">₹{product.revenue?.toFixed(2)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </div>
          )}
          
          {selectedReport === 'INVENTORY' && reportData.summary && (
            <div className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div className="bg-blue-50 p-4 rounded-lg">
                  <div className="text-sm text-blue-600 font-medium">Total Products</div>
                  <div className="text-2xl font-bold text-blue-900 mt-2">
                    {reportData.summary.totalProducts}
                  </div>
                </div>
                <div className="bg-green-50 p-4 rounded-lg">
                  <div className="text-sm text-green-600 font-medium">Stock Value</div>
                  <div className="text-2xl font-bold text-green-900 mt-2">
                    ₹{reportData.summary.totalStockValue?.toFixed(2)}
                  </div>
                </div>
                <div className="bg-yellow-50 p-4 rounded-lg">
                  <div className="text-sm text-yellow-600 font-medium">Low Stock Items</div>
                  <div className="text-2xl font-bold text-yellow-900 mt-2">
                    {reportData.summary.lowStockItems}
                  </div>
                </div>
                <div className="bg-red-50 p-4 rounded-lg">
                  <div className="text-sm text-red-600 font-medium">Out of Stock</div>
                  <div className="text-2xl font-bold text-red-900 mt-2">
                    {reportData.summary.outOfStockItems}
                  </div>
                </div>
              </div>
            </div>
          )}
          
          {!reportData.summary && (
            <div className="text-center py-8 text-gray-500">
              <p>No data available for the selected date range</p>
            </div>
          )}
        </div>
      )}

      {!reportData && (
        <div className="bg-white rounded-lg shadow-md p-6 mt-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Report Preview</h2>
          <div className="text-center py-12 text-gray-500">
            <div className="text-6xl mb-4">📊</div>
            <p className="text-lg">Select date range and generate a report to see the results here</p>
          </div>
        </div>
      )}

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
