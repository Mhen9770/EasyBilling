'use client';

import { useAuth } from '@/lib/hooks/useAuth';
import { useQuery } from '@tanstack/react-query';
import { reportsApi } from '@/lib/api/reports/reportsApi';
import { billingApi } from '@/lib/api/billing/billingApi';
import { customerApi } from '@/lib/api/customer/customerApi';
import { inventoryApi } from '@/lib/api/inventory/inventoryApi';
import { PageLoader } from '@/components/ui/Loader';
import { StatCard, Card, CardHeader, CardTitle, CardContent, Badge } from '@/components/ui';
import { SalesLineChart, RevenueBarChart } from '@/components/ui/Charts';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useMemo } from 'react';

export default function DashboardPage() {
  const { user } = useAuth();
  const router = useRouter();

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

  const isLoading = !salesReport && !inventoryReport && !recentInvoices && !customersData;

  // Generate chart data from sales report
  const salesChartData = useMemo(() => {
    if (!salesData?.dailySales) {
      // Return mock data if no data available
      return Array.from({ length: 7 }, (_, i) => ({
        name: new Date(Date.now() - (6 - i) * 24 * 60 * 60 * 1000).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
        value: 0,
        orders: 0,
      }));
    }
    return salesData.dailySales.slice(-7).map((sale: any) => ({
      name: new Date(sale.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
      value: sale.totalSales || 0,
      orders: sale.totalOrders || 0,
    }));
  }, [salesData]);

  // Generate revenue chart data - using mock data for now
  const revenueChartData = useMemo(() => {
    // Mock data for revenue by category
    return [
      { name: 'Electronics', value: salesData?.summary?.totalSales ? salesData.summary.totalSales * 0.4 : 0 },
      { name: 'Clothing', value: salesData?.summary?.totalSales ? salesData.summary.totalSales * 0.3 : 0 },
      { name: 'Food', value: salesData?.summary?.totalSales ? salesData.summary.totalSales * 0.2 : 0 },
      { name: 'Others', value: salesData?.summary?.totalSales ? salesData.summary.totalSales * 0.1 : 0 },
    ].filter(item => item.value > 0);
  }, [salesData]);

  if (isLoading) {
    return (
      <div className="space-y-6">
        <PageLoader text="Loading dashboard..." />
      </div>
    );
  }

  return (
    <div className="w-full space-y-4 sm:space-y-6">
      {/* Welcome Section */}
      <div className="bg-gradient-to-r from-blue-600 to-purple-600 rounded-xl sm:rounded-2xl shadow-xl p-4 sm:p-6 lg:p-8 text-white">
        <h2 className="text-2xl sm:text-3xl font-bold mb-2">
          Welcome back, {user?.firstName || user?.username}!
        </h2>
        <p className="text-sm sm:text-base text-blue-100">
          Here's what's happening with your business today
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        <StatCard
          title="Total Sales (Month)"
          value={`₹${salesData?.summary?.totalSales?.toFixed(2) || '0.00'}`}
          icon="💰"
          color="green"
          trend={{
            value: 12.5,
            isPositive: true,
            label: 'vs last month',
          }}
          onClick={() => router.push('/reports')}
        />
        
        <StatCard
          title="Total Products"
          value={inventoryData?.summary?.totalProducts || 0}
          icon="📦"
          color="blue"
          trend={{
            value: inventoryData?.summary?.lowStockItems || 0,
            isPositive: false,
            label: 'low stock items',
          }}
          onClick={() => router.push('/inventory/products')}
        />
        
        <StatCard
          title="Total Customers"
          value={totalCustomers}
          icon="👥"
          color="purple"
          trend={{
            value: 8.2,
            isPositive: true,
            label: 'new this month',
          }}
          onClick={() => router.push('/customers')}
        />
        
        <StatCard
          title="Avg Order Value"
          value={`₹${salesData?.summary?.averageOrderValue?.toFixed(2) || '0.00'}`}
          icon="📊"
          color="yellow"
          trend={{
            value: parseFloat(salesData?.summary?.profitMargin?.toFixed(1) || '0'),
            isPositive: true,
            label: 'profit margin',
          }}
        />
      </div>

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 sm:gap-6">
        <Card padding="lg">
          <CardHeader>
            <CardTitle>Sales Trend (Last 7 Days)</CardTitle>
          </CardHeader>
          <CardContent>
            {salesChartData.length > 0 ? (
              <SalesLineChart data={salesChartData} height={250} />
            ) : (
              <div className="text-center py-12 text-gray-500">
                <p className="text-4xl mb-2">📈</p>
                <p>No sales data available</p>
              </div>
            )}
          </CardContent>
        </Card>

        <Card padding="lg">
          <CardHeader>
            <CardTitle>Revenue by Category</CardTitle>
          </CardHeader>
          <CardContent>
            {revenueChartData.length > 0 ? (
              <RevenueBarChart data={revenueChartData} height={250} />
            ) : (
              <div className="text-center py-12 text-gray-500">
                <p className="text-4xl mb-2">📊</p>
                <p>No category data available</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Recent Activity & Alerts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 sm:gap-6">
        {/* Recent Invoices */}
        <Card padding="lg">
          <CardHeader
            actions={
              <Link href="/pos" className="text-sm text-blue-600 hover:text-blue-700 font-medium">
                View All →
              </Link>
            }
          >
            <CardTitle>Recent Invoices</CardTitle>
          </CardHeader>
          <CardContent>
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
                      <Badge
                        variant={
                          invoice.status === 'COMPLETED' ? 'success' :
                          invoice.status === 'DRAFT' ? 'warning' :
                          'default'
                        }
                        size="sm"
                      >
                        {invoice.status}
                      </Badge>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Low Stock Alerts */}
        <Card padding="lg">
          <CardHeader
            actions={
              <Link href="/inventory/stock" className="text-sm text-blue-600 hover:text-blue-700 font-medium">
                View All →
              </Link>
            }
          >
            <CardTitle>Low Stock Alerts</CardTitle>
          </CardHeader>
          <CardContent>
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
          </CardContent>
        </Card>
      </div>

      {/* Profile Info */}
      <Card padding="lg">
        <CardHeader>
          <CardTitle>Profile Information</CardTitle>
        </CardHeader>
        <CardContent>
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
                  <Badge key={role} variant="primary">
                    {role.replace('ROLE_', '')}
                  </Badge>
                ))}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Quick Actions */}
      <div>
        <h3 className="text-lg sm:text-xl font-bold text-gray-900 mb-4">Quick Actions</h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
          <Link
            href="/pos"
            className="group"
          >
            <Card hover padding="lg" className="h-full">
              <div className="text-4xl mb-4 group-hover:scale-110 transition-transform">💰</div>
              <h4 className="text-lg font-semibold text-gray-900 mb-2">Create Invoice</h4>
              <p className="text-sm text-gray-600">Start billing your customers</p>
            </Card>
          </Link>

          <Link
            href="/inventory/products"
            className="group"
          >
            <Card hover padding="lg" className="h-full">
              <div className="text-4xl mb-4 group-hover:scale-110 transition-transform">📦</div>
              <h4 className="text-lg font-semibold text-gray-900 mb-2">Manage Products</h4>
              <p className="text-sm text-gray-600">Add and update inventory</p>
            </Card>
          </Link>

          <Link
            href="/reports"
            className="group"
          >
            <Card hover padding="lg" className="h-full">
              <div className="text-4xl mb-4 group-hover:scale-110 transition-transform">📊</div>
              <h4 className="text-lg font-semibold text-gray-900 mb-2">View Reports</h4>
              <p className="text-sm text-gray-600">Analyze your business metrics</p>
            </Card>
          </Link>

          <Link
            href="/customers"
            className="group"
          >
            <Card hover padding="lg" className="h-full">
              <div className="text-4xl mb-4 group-hover:scale-110 transition-transform">👥</div>
              <h4 className="text-lg font-semibold text-gray-900 mb-2">Manage Customers</h4>
              <p className="text-sm text-gray-600">Add and manage your customers</p>
            </Card>
          </Link>
        </div>
      </div>
    </div>
  );
}
