'use client';

import { useAuth } from '@/lib/hooks/useAuth';
import { useQuery } from '@tanstack/react-query';
import { reportsApi } from '@/lib/api/reports/reportsApi';
import { billingApi } from '@/lib/api/billing/billingApi';
import { customerApi } from '@/lib/api/customer/customerApi';
import { inventoryApi } from '@/lib/api/inventory/inventoryApi';
import Link from 'next/link';

export default function DashboardPage() {
  const { user } = useAuth();

  // Get today's date range
  const today = new Date();
  const startOfMonth = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0];
  const endOfMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0).toISOString().split('T')[0];

  // Fetch sales report
  const { data: salesReport } = useQuery({
    queryKey: ['salesReport', startOfMonth, endOfMonth],
    queryFn: () => reportsApi.generateSalesReport(startOfMonth, endOfMonth),
  });

  // Fetch inventory report
  const { data: inventoryReport } = useQuery({
    queryKey: ['inventoryReport'],
    queryFn: () => reportsApi.generateInventoryReport(),
  });

  // Fetch recent invoices
  const { data: recentInvoices } = useQuery({
    queryKey: ['recentInvoices'],
    queryFn: () => billingApi.listInvoices(0, 5),
  });

  // Fetch customer count
  const { data: customersData } = useQuery({
    queryKey: ['customersCount'],
    queryFn: () => customerApi.listCustomers(0, 1),
  });

  const salesData = salesReport?.data;
  const inventoryData = inventoryReport?.data;
  const invoices = recentInvoices?.data?.content || [];
  const totalCustomers = customersData?.data?.totalElements || 0;

  return (
    <div className="space-y-6">
      {/* Welcome Section */}
      <div className="bg-gradient-to-r from-blue-600 to-purple-600 rounded-2xl shadow-xl p-8 text-white">
        <h2 className="text-3xl font-bold mb-2">
          Welcome back, {user?.firstName || user?.username}!
        </h2>
        <p className="text-blue-100">
          Here's what's happening with your business today
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="bg-white rounded-xl shadow-md p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">Total Sales (Month)</p>
              <p className="text-2xl font-bold text-gray-900 mt-2">
                ₹{salesData?.summary?.totalSales?.toFixed(2) || '0.00'}
              </p>
              <p className="text-xs text-green-600 mt-1">
                {salesData?.summary?.totalOrders || 0} orders
              </p>
            </div>
            <div className="h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center text-2xl">
              💰
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-md p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">Total Products</p>
              <p className="text-2xl font-bold text-gray-900 mt-2">
                {inventoryData?.summary?.totalProducts || 0}
              </p>
              <p className="text-xs text-red-600 mt-1">
                {inventoryData?.summary?.lowStockItems || 0} low stock
              </p>
            </div>
            <div className="h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center text-2xl">
              📦
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-md p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">Total Customers</p>
              <p className="text-2xl font-bold text-gray-900 mt-2">
                {totalCustomers}
              </p>
              <p className="text-xs text-blue-600 mt-1">Active customers</p>
            </div>
            <div className="h-12 w-12 bg-purple-100 rounded-lg flex items-center justify-center text-2xl">
              👥
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-md p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">Avg Order Value</p>
              <p className="text-2xl font-bold text-gray-900 mt-2">
                ₹{salesData?.summary?.averageOrderValue?.toFixed(2) || '0.00'}
              </p>
              <p className="text-xs text-gray-600 mt-1">
                {salesData?.summary?.profitMargin?.toFixed(1) || 0}% margin
              </p>
            </div>
            <div className="h-12 w-12 bg-yellow-100 rounded-lg flex items-center justify-center text-2xl">
              📊
            </div>
          </div>
        </div>
      </div>

      {/* Recent Activity & Alerts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Invoices */}
        <div className="bg-white rounded-xl shadow-md p-6">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-xl font-bold text-gray-900">Recent Invoices</h3>
            <Link href="/pos" className="text-sm text-blue-600 hover:text-blue-700">
              View All →
            </Link>
          </div>
          {invoices.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              <p className="text-4xl mb-2">📄</p>
              <p>No invoices yet</p>
            </div>
          ) : (
            <div className="space-y-3">
              {invoices.map((invoice) => (
                <div key={invoice.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
                  <div>
                    <p className="font-medium text-gray-900">{invoice.invoiceNumber}</p>
                    <p className="text-sm text-gray-500">
                      {invoice.customerName || 'Walk-in Customer'}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="font-semibold text-gray-900">₹{invoice.total.toFixed(2)}</p>
                    <span className={`text-xs px-2 py-1 rounded-full ${
                      invoice.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                      invoice.status === 'DRAFT' ? 'bg-yellow-100 text-yellow-800' :
                      'bg-gray-100 text-gray-800'
                    }`}>
                      {invoice.status}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Low Stock Alerts */}
        <div className="bg-white rounded-xl shadow-md p-6">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-xl font-bold text-gray-900">Low Stock Alerts</h3>
            <Link href="/inventory/stock" className="text-sm text-blue-600 hover:text-blue-700">
              View All →
            </Link>
          </div>
          {!inventoryData?.lowStockAlerts || inventoryData.lowStockAlerts.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              <p className="text-4xl mb-2">✅</p>
              <p>All products well stocked!</p>
            </div>
          ) : (
            <div className="space-y-3">
              {inventoryData.lowStockAlerts.slice(0, 5).map((alert, index) => (
                <div key={index} className="flex items-center justify-between p-3 bg-red-50 rounded-lg border border-red-100">
                  <div>
                    <p className="font-medium text-gray-900">{alert.productName}</p>
                    <p className="text-sm text-red-600">
                      Only {alert.currentStock} left (Min: {alert.threshold})
                    </p>
                  </div>
                  <div className="text-2xl">⚠️</div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Profile Info */}
      <div className="bg-white rounded-xl shadow-md p-6">
        <h3 className="text-xl font-bold text-gray-900 mb-4">Profile Information</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <p className="text-sm font-medium text-gray-600">Username</p>
            <p className="text-lg text-gray-900 mt-1">{user?.username}</p>
          </div>
          <div>
            <p className="text-sm font-medium text-gray-600">Email</p>
            <p className="text-lg text-gray-900 mt-1">{user?.email}</p>
          </div>
          <div>
            <p className="text-sm font-medium text-gray-600">Full Name</p>
            <p className="text-lg text-gray-900 mt-1">
              {user?.firstName && user?.lastName
                ? `${user.firstName} ${user.lastName}`
                : 'Not provided'}
            </p>
          </div>
          <div>
            <p className="text-sm font-medium text-gray-600">Roles</p>
            <div className="flex flex-wrap gap-2 mt-1">
              {user?.roles?.map((role) => (
                <span
                  key={role}
                  className="px-3 py-1 bg-blue-100 text-blue-800 text-sm font-medium rounded-full"
                >
                  {role.replace('ROLE_', '')}
                </span>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div>
        <h3 className="text-xl font-bold text-gray-900 mb-4">Quick Actions</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Link
            href="/pos"
            className="bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition-shadow cursor-pointer"
          >
            <div className="text-4xl mb-4">💰</div>
            <h4 className="text-lg font-semibold text-gray-900 mb-2">Create Invoice</h4>
            <p className="text-sm text-gray-600">Start billing your customers</p>
          </Link>

          <Link
            href="/reports"
            className="bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition-shadow cursor-pointer"
          >
            <div className="text-4xl mb-4">📊</div>
            <h4 className="text-lg font-semibold text-gray-900 mb-2">View Reports</h4>
            <p className="text-sm text-gray-600">Analyze your business metrics</p>
          </Link>

          <Link
            href="/customers"
            className="bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition-shadow cursor-pointer"
          >
            <div className="text-4xl mb-4">👥</div>
            <h4 className="text-lg font-semibold text-gray-900 mb-2">Manage Customers</h4>
            <p className="text-sm text-gray-600">Add and manage your customers</p>
          </Link>
        </div>
      </div>
    </div>
  );
}
