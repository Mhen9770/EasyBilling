'use client';

import { useQuery } from '@tanstack/react-query';
import { inventoryApi } from '@/lib/api/inventory/inventoryApi';
import { PageLoader } from '@/components/ui/Loader';
import Link from 'next/link';
import { useState } from 'react';

export default function InventoryDashboardPage() {
  const [selectedLocation, setSelectedLocation] = useState<string>('all');

  // Fetch inventory summary
  const { data: productsData, isLoading: productsLoading } = useQuery({
    queryKey: ['products', 'summary'],
    queryFn: () => inventoryApi.getProducts({ page: 0, size: 1000 }),
  });

  const { data: stockData } = useQuery({
    queryKey: ['stock', 'summary'],
    queryFn: () => inventoryApi.getStockSummary(),
  });

  if (productsLoading) {
    return <PageLoader text="Loading inventory dashboard..." />;
  }

  const products = productsData?.data?.content || [];
  const totalProducts = products.length;
  const activeProducts = products.filter((p: any) => p.isActive).length;
  const lowStockProducts = products.filter((p: any) => {
    // This would need stock data - simplified for now
    return p.trackStock && p.lowStockThreshold > 0;
  }).length;

  // Calculate inventory value (simplified)
  const totalInventoryValue = products.reduce((sum: number, p: any) => {
    return sum + (parseFloat(p.costPrice) || 0);
  }, 0);

  const stats = [
    {
      title: 'Total Products',
      value: totalProducts,
      change: '+12%',
      trend: 'up',
      icon: '📦',
      color: 'blue',
      link: '/inventory/products',
    },
    {
      title: 'Active Products',
      value: activeProducts,
      change: '+5%',
      trend: 'up',
      icon: '✅',
      color: 'green',
      link: '/inventory/products?status=active',
    },
    {
      title: 'Low Stock Alerts',
      value: lowStockProducts,
      change: '-3%',
      trend: 'down',
      icon: '⚠️',
      color: 'yellow',
      link: '/inventory/products?filter=low_stock',
    },
    {
      title: 'Inventory Value',
      value: `₹${(totalInventoryValue / 1000).toFixed(1)}K`,
      change: '+8%',
      trend: 'up',
      icon: '💰',
      color: 'purple',
      link: '/inventory/valuation',
    },
  ];

  const quickActions = [
    {
      title: 'Add Product',
      description: 'Create a new product',
      icon: '➕',
      link: '/inventory/products?action=create',
      color: 'from-blue-500 to-blue-600',
    },
    {
      title: 'Stock Adjustment',
      description: 'Adjust stock levels',
      icon: '📊',
      link: '/inventory/stock/adjust',
      color: 'from-green-500 to-green-600',
    },
    {
      title: 'Transfer Stock',
      description: 'Move stock between locations',
      icon: '🚚',
      link: '/inventory/stock/transfer',
      color: 'from-purple-500 to-purple-600',
    },
    {
      title: 'Stock Report',
      description: 'View detailed stock reports',
      icon: '📈',
      link: '/inventory/reports',
      color: 'from-orange-500 to-orange-600',
    },
  ];

  return (
    <div className="w-full space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Inventory Dashboard</h1>
          <p className="text-gray-600 mt-1">Real-time inventory overview and management</p>
        </div>
        <div className="flex gap-2">
          <select
            value={selectedLocation}
            onChange={(e) => setSelectedLocation(e.target.value)}
            className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm"
          >
            <option value="all">All Locations</option>
            <option value="main">Main Store</option>
            <option value="warehouse">Warehouse</option>
          </select>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        {stats.map((stat, index) => (
          <Link
            key={index}
            href={stat.link}
            className="bg-white rounded-xl shadow-md hover:shadow-lg transition-all p-6 border-l-4 border-transparent hover:border-blue-500 cursor-pointer group"
          >
            <div className="flex items-center justify-between mb-4">
              <div className={`text-4xl p-3 rounded-lg bg-${stat.color}-50 group-hover:scale-110 transition-transform`}>
                {stat.icon}
              </div>
              <div className={`text-sm font-medium ${
                stat.trend === 'up' ? 'text-green-600' : 'text-red-600'
              }`}>
                {stat.change}
              </div>
            </div>
            <h3 className="text-sm font-medium text-gray-600 mb-1">{stat.title}</h3>
            <p className="text-3xl font-bold text-gray-900">{stat.value}</p>
          </Link>
        ))}
      </div>

      {/* Quick Actions */}
      <div>
        <h2 className="text-xl font-bold text-gray-900 mb-4">Quick Actions</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {quickActions.map((action, index) => (
            <Link
              key={index}
              href={action.link}
              className={`bg-gradient-to-br ${action.color} rounded-xl shadow-md hover:shadow-xl transition-all p-6 text-white cursor-pointer group transform hover:scale-105`}
            >
              <div className="text-4xl mb-3 group-hover:scale-110 transition-transform">
                {action.icon}
              </div>
              <h3 className="text-lg font-semibold mb-1">{action.title}</h3>
              <p className="text-sm opacity-90">{action.description}</p>
            </Link>
          ))}
        </div>
      </div>

      {/* Recent Activity & Low Stock Alerts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Low Stock Alerts */}
        <div className="bg-white rounded-xl shadow-md p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-bold text-gray-900">Low Stock Alerts</h2>
            <Link href="/inventory/products?filter=low_stock" className="text-sm text-blue-600 hover:text-blue-700">
              View All →
            </Link>
          </div>
          <div className="space-y-3">
            {lowStockProducts > 0 ? (
              <div className="text-center py-8">
                <div className="text-4xl mb-2">⚠️</div>
                <p className="text-gray-600">{lowStockProducts} products need attention</p>
                <Link
                  href="/inventory/products?filter=low_stock"
                  className="mt-4 inline-block px-4 py-2 bg-yellow-100 text-yellow-800 rounded-lg hover:bg-yellow-200 transition-colors text-sm font-medium"
                >
                  View Low Stock Items
                </Link>
              </div>
            ) : (
              <div className="text-center py-8 text-gray-500">
                <div className="text-4xl mb-2">✅</div>
                <p>All products are well stocked!</p>
              </div>
            )}
          </div>
        </div>

        {/* Recent Stock Movements */}
        <div className="bg-white rounded-xl shadow-md p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-bold text-gray-900">Recent Movements</h2>
            <Link href="/inventory/stock/movements" className="text-sm text-blue-600 hover:text-blue-700">
              View All →
            </Link>
          </div>
          <div className="space-y-3">
            <div className="text-center py-8 text-gray-500">
              <div className="text-4xl mb-2">📊</div>
              <p>No recent stock movements</p>
            </div>
          </div>
        </div>
      </div>

      {/* Navigation Cards */}
      <div>
        <h2 className="text-xl font-bold text-gray-900 mb-4">Inventory Modules</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <Link
            href="/inventory/products"
            className="bg-white rounded-xl shadow-md hover:shadow-lg transition-all p-6 border-2 border-transparent hover:border-blue-500 cursor-pointer group"
          >
            <div className="text-4xl mb-3 group-hover:scale-110 transition-transform">📦</div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Products</h3>
            <p className="text-sm text-gray-600">Manage your product catalog</p>
          </Link>

          <Link
            href="/inventory/categories"
            className="bg-white rounded-xl shadow-md hover:shadow-lg transition-all p-6 border-2 border-transparent hover:border-blue-500 cursor-pointer group"
          >
            <div className="text-4xl mb-3 group-hover:scale-110 transition-transform">🏷️</div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Categories</h3>
            <p className="text-sm text-gray-600">Organize products by category</p>
          </Link>

          <Link
            href="/inventory/brands"
            className="bg-white rounded-xl shadow-md hover:shadow-lg transition-all p-6 border-2 border-transparent hover:border-blue-500 cursor-pointer group"
          >
            <div className="text-4xl mb-3 group-hover:scale-110 transition-transform">⭐</div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Brands</h3>
            <p className="text-sm text-gray-600">Manage product brands</p>
          </Link>

          <Link
            href="/inventory/stock"
            className="bg-white rounded-xl shadow-md hover:shadow-lg transition-all p-6 border-2 border-transparent hover:border-blue-500 cursor-pointer group"
          >
            <div className="text-4xl mb-3 group-hover:scale-110 transition-transform">📊</div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Stock Management</h3>
            <p className="text-sm text-gray-600">Track and manage stock levels</p>
          </Link>

          <Link
            href="/inventory/stock/movements"
            className="bg-white rounded-xl shadow-md hover:shadow-lg transition-all p-6 border-2 border-transparent hover:border-blue-500 cursor-pointer group"
          >
            <div className="text-4xl mb-3 group-hover:scale-110 transition-transform">🔄</div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Stock Movements</h3>
            <p className="text-sm text-gray-600">View stock movement history</p>
          </Link>

          <Link
            href="/inventory/reports"
            className="bg-white rounded-xl shadow-md hover:shadow-lg transition-all p-6 border-2 border-transparent hover:border-blue-500 cursor-pointer group"
          >
            <div className="text-4xl mb-3 group-hover:scale-110 transition-transform">📈</div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Reports</h3>
            <p className="text-sm text-gray-600">Inventory analytics and reports</p>
          </Link>
        </div>
      </div>
    </div>
  );
}

