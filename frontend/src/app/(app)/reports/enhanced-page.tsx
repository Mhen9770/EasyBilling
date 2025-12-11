'use client';

import { useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { reportsApi, type ReportType } from '@/lib/api/reports/reportsApi';
import { useToastStore } from '@/components/ui/toast';
import { Card, CardHeader, CardTitle, CardContent, Button, Badge, StatCard } from '@/components/ui';
import { SalesLineChart, RevenueBarChart } from '@/components/ui/Charts';
import { PageLoader } from '@/components/ui/Loader';

export default function EnhancedReportsPage() {
  const [selectedReport, setSelectedReport] = useState<ReportType>('SALES');
  const [dateRange, setDateRange] = useState({ 
    from: new Date(new Date().setDate(1)).toISOString().split('T')[0], 
    to: new Date().toISOString().split('T')[0] 
  });
  const { addToast } = useToastStore();

  // Fetch sales report
  const { data: salesReport, isLoading: loadingSales } = useQuery({
    queryKey: ['salesReport', dateRange.from, dateRange.to],
    queryFn: () => reportsApi.generateSalesReport(dateRange.from, dateRange.to),
    enabled: selectedReport === 'SALES',
  });

  // Fetch inventory report
  const { data: inventoryReport, isLoading: loadingInventory } = useQuery({
    queryKey: ['inventoryReport'],
    queryFn: () => reportsApi.generateInventoryReport(),
    enabled: selectedReport === 'INVENTORY',
  });

  const reportTypes = [
    { id: 'SALES' as ReportType, name: 'Sales Report', icon: '💰', color: 'green', description: 'Revenue and sales analysis' },
    { id: 'INVENTORY' as ReportType, name: 'Inventory Report', icon: '📦', color: 'blue', description: 'Stock levels and movements' },
    { id: 'CUSTOMER' as ReportType, name: 'Customer Report', icon: '👥', color: 'purple', description: 'Customer analytics' },
    { id: 'TAX' as ReportType, name: 'Tax Report', icon: '📝', color: 'yellow', description: 'GST/VAT summary' },
    { id: 'PROFIT_LOSS' as ReportType, name: 'P&L Statement', icon: '📊', color: 'indigo', description: 'Profitability analysis' },
  ];

  const handleExport = (format: 'PDF' | 'EXCEL') => {
    addToast(`Exporting report as ${format}...`, 'info');
    // TODO: Implement export functionality
  };

  const handlePrint = () => {
    window.print();
  };

  const salesData = salesReport?.data;
  const inventoryData = inventoryReport?.data;

  // Prepare chart data
  const chartData = salesData?.dailySales?.slice(-7).map((sale: any) => ({
    name: new Date(sale.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
    value: sale.totalSales || 0,
  })) || [];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Reports & Analytics</h1>
        <p className="text-gray-600 mt-2">Comprehensive insights into your business performance</p>
      </div>

      {/* Report Type Selection */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
        {reportTypes.map((report) => (
          <button
            key={report.id}
            onClick={() => setSelectedReport(report.id)}
            className={`text-left p-4 rounded-xl border-2 transition-all ${
              selectedReport === report.id
                ? 'border-blue-600 bg-blue-50 shadow-md scale-105'
                : 'border-gray-200 bg-white hover:border-blue-300 hover:shadow-sm'
            }`}
          >
            <div className="text-3xl mb-2">{report.icon}</div>
            <h3 className="text-sm font-semibold text-gray-900 mb-1">{report.name}</h3>
            <p className="text-xs text-gray-600">{report.description}</p>
          </button>
        ))}
      </div>

      {/* Date Range & Actions */}
      <Card>
        <CardContent className="p-4">
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
            <div className="flex flex-col sm:flex-row gap-3">
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">From</label>
                <input
                  type="date"
                  value={dateRange.from}
                  onChange={(e) => setDateRange({ ...dateRange, from: e.target.value })}
                  className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">To</label>
                <input
                  type="date"
                  value={dateRange.to}
                  onChange={(e) => setDateRange({ ...dateRange, to: e.target.value })}
                  className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>
            
            <div className="flex gap-2">
              <Button variant="ghost" size="sm" onClick={handlePrint} icon="🖨️">
                Print
              </Button>
              <Button variant="ghost" size="sm" onClick={() => handleExport('PDF')} icon="📄">
                PDF
              </Button>
              <Button variant="ghost" size="sm" onClick={() => handleExport('EXCEL')} icon="📊">
                Excel
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Report Content */}
      {selectedReport === 'SALES' && (
        <>
          {loadingSales ? (
            <PageLoader text="Loading sales report..." />
          ) : salesData ? (
            <div className="space-y-6">
              {/* KPI Cards */}
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <StatCard
                  title="Total Revenue"
                  value={`₹${salesData.summary?.totalSales?.toFixed(2) || '0.00'}`}
                  icon="💰"
                  color="green"
                  trend={{ value: 15.3, isPositive: true, label: 'vs last period' }}
                />
                <StatCard
                  title="Total Orders"
                  value={salesData.summary?.totalOrders || 0}
                  icon="🛒"
                  color="blue"
                  trend={{ value: 8.7, isPositive: true, label: 'vs last period' }}
                />
                <StatCard
                  title="Avg Order Value"
                  value={`₹${salesData.summary?.averageOrderValue?.toFixed(2) || '0.00'}`}
                  icon="💳"
                  color="purple"
                  trend={{ value: 3.2, isPositive: false, label: 'vs last period' }}
                />
                <StatCard
                  title="Profit Margin"
                  value={`${salesData.summary?.profitMargin?.toFixed(1) || '0.0'}%`}
                  icon="📊"
                  color="yellow"
                  trend={{ value: 2.1, isPositive: true, label: 'vs last period' }}
                />
              </div>

              {/* Charts */}
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <Card>
                  <CardHeader>
                    <CardTitle>Sales Trend</CardTitle>
                  </CardHeader>
                  <CardContent>
                    {chartData.length > 0 ? (
                      <SalesLineChart data={chartData} height={300} />
                    ) : (
                      <div className="text-center py-12 text-gray-500">
                        <p>No sales data available</p>
                      </div>
                    )}
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>Top Products</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      {salesData.topProducts?.slice(0, 5).map((product: any, index: number) => (
                        <div key={index} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                          <div className="flex items-center gap-3">
                            <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center text-sm font-bold text-blue-600">
                              {index + 1}
                            </div>
                            <div>
                              <p className="font-medium text-gray-900">{product.name}</p>
                              <p className="text-sm text-gray-500">{product.quantity} units sold</p>
                            </div>
                          </div>
                          <p className="font-semibold text-gray-900">₹{product.revenue?.toFixed(2)}</p>
                        </div>
                      )) || <p className="text-center text-gray-500 py-8">No product data available</p>}
                    </div>
                  </CardContent>
                </Card>
              </div>

              {/* Detailed Table */}
              <Card>
                <CardHeader>
                  <CardTitle>Daily Breakdown</CardTitle>
                </CardHeader>
                <CardContent className="p-0">
                  <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                      <thead className="bg-gray-50">
                        <tr>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Date</th>
                          <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Orders</th>
                          <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Revenue</th>
                          <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Avg Value</th>
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-200">
                        {salesData.dailySales?.slice(-10).reverse().map((day: any, index: number) => (
                          <tr key={index} className="hover:bg-gray-50">
                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                              {new Date(day.date).toLocaleDateString()}
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-gray-900">
                              {day.totalOrders}
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-right font-medium text-gray-900">
                              ₹{day.totalSales?.toFixed(2)}
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-gray-500">
                              ₹{(day.totalSales / day.totalOrders || 0).toFixed(2)}
                            </td>
                          </tr>
                        )) || (
                          <tr>
                            <td colSpan={4} className="px-6 py-8 text-center text-gray-500">
                              No sales data available
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>
                </CardContent>
              </Card>
            </div>
          ) : (
            <Card>
              <CardContent className="py-12 text-center text-gray-500">
                <div className="text-5xl mb-4">📊</div>
                <p>No data available for the selected period</p>
              </CardContent>
            </Card>
          )}
        </>
      )}

      {selectedReport === 'INVENTORY' && (
        <>
          {loadingInventory ? (
            <PageLoader text="Loading inventory report..." />
          ) : inventoryData ? (
            <div className="space-y-6">
              {/* Inventory KPIs */}
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <StatCard
                  title="Total Products"
                  value={inventoryData.summary?.totalProducts || 0}
                  icon="📦"
                  color="blue"
                />
                <StatCard
                  title="Low Stock Items"
                  value={inventoryData.summary?.lowStockItems || 0}
                  icon="⚠️"
                  color="yellow"
                />
                <StatCard
                  title="Out of Stock"
                  value={inventoryData.summary?.outOfStockItems || 0}
                  icon="❌"
                  color="red"
                />
                <StatCard
                  title="Stock Value"
                  value={`₹${inventoryData.summary?.totalStockValue?.toFixed(2) || '0.00'}`}
                  icon="💰"
                  color="green"
                />
              </div>

              {/* Low Stock Alerts */}
              <Card>
                <CardHeader>
                  <CardTitle>Low Stock Alerts</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    {inventoryData.lowStockAlerts?.slice(0, 10).map((alert: any, index: number) => (
                      <div key={index} className="flex items-center justify-between p-3 bg-red-50 border border-red-100 rounded-lg">
                        <div>
                          <p className="font-medium text-gray-900">{alert.productName}</p>
                          <p className="text-sm text-red-600">
                            Current: {alert.currentStock} | Min: {alert.threshold}
                          </p>
                        </div>
                        <Badge variant="danger">Low Stock</Badge>
                      </div>
                    )) || <p className="text-center text-gray-500 py-8">All products are well stocked!</p>}
                  </div>
                </CardContent>
              </Card>
            </div>
          ) : (
            <Card>
              <CardContent className="py-12 text-center text-gray-500">
                <div className="text-5xl mb-4">📦</div>
                <p>No inventory data available</p>
              </CardContent>
            </Card>
          )}
        </>
      )}

      {!['SALES', 'INVENTORY'].includes(selectedReport) && (
        <Card>
          <CardContent className="py-12 text-center">
            <div className="text-6xl mb-4">🚧</div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">Coming Soon</h3>
            <p className="text-gray-600">
              {reportTypes.find(r => r.id === selectedReport)?.name} will be available soon
            </p>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
