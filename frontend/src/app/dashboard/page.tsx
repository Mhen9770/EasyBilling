'use client';

import { useAuth } from '@/lib/hooks/useAuth';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';
import Link from 'next/link';

export default function DashboardPage() {
  const { user, isAuthenticated, logout, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, isLoading, router]);

  if (isLoading || !user) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <div className="h-10 w-10 bg-gradient-to-br from-blue-600 to-purple-600 rounded-lg" />
              <h1 className="text-2xl font-bold text-gray-900">EasyBilling</h1>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-gray-700">Hello, {user.firstName || user.username}</span>
              <button
                onClick={() => logout()}
                className="px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-lg hover:bg-red-700 transition-colors"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Welcome Section */}
        <div className="bg-gradient-to-r from-blue-600 to-purple-600 rounded-2xl shadow-xl p-8 text-white mb-8">
          <h2 className="text-3xl font-bold mb-2">
            Welcome back, {user.firstName || user.username}!
          </h2>
          <p className="text-blue-100">
            You're logged in and ready to manage your billing operations
          </p>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div className="bg-white rounded-xl shadow-md p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Account Status</p>
                <p className="text-2xl font-bold text-gray-900 mt-2">Active</p>
              </div>
              <div className="h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center">
                ✓
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-md p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">User Role</p>
                <p className="text-2xl font-bold text-gray-900 mt-2">
                  {user.roles?.[0]?.replace('ROLE_', '') || 'User'}
                </p>
              </div>
              <div className="h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center">
                👤
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-md p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Tenant ID</p>
                <p className="text-sm font-bold text-gray-900 mt-2 truncate">
                  {user.tenantId}
                </p>
              </div>
              <div className="h-12 w-12 bg-purple-100 rounded-lg flex items-center justify-center">
                🏢
              </div>
            </div>
          </div>
        </div>

        {/* Profile Info */}
        <div className="bg-white rounded-xl shadow-md p-6 mb-8">
          <h3 className="text-xl font-bold text-gray-900 mb-4">Profile Information</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <p className="text-sm font-medium text-gray-600">Username</p>
              <p className="text-lg text-gray-900 mt-1">{user.username}</p>
            </div>
            <div>
              <p className="text-sm font-medium text-gray-600">Email</p>
              <p className="text-lg text-gray-900 mt-1">{user.email}</p>
            </div>
            <div>
              <p className="text-sm font-medium text-gray-600">Full Name</p>
              <p className="text-lg text-gray-900 mt-1">
                {user.firstName && user.lastName
                  ? `${user.firstName} ${user.lastName}`
                  : 'Not provided'}
              </p>
            </div>
            <div>
              <p className="text-sm font-medium text-gray-600">Roles</p>
              <div className="flex flex-wrap gap-2 mt-1">
                {user.roles?.map((role) => (
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
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition-shadow">
            <div className="text-4xl mb-4">💰</div>
            <h4 className="text-lg font-semibold text-gray-900 mb-2">Create Invoice</h4>
            <p className="text-sm text-gray-600">Start billing your customers</p>
          </div>

          <div className="bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition-shadow">
            <div className="text-4xl mb-4">📊</div>
            <h4 className="text-lg font-semibold text-gray-900 mb-2">View Reports</h4>
            <p className="text-sm text-gray-600">Analyze your business metrics</p>
          </div>

          <div className="bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition-shadow">
            <div className="text-4xl mb-4">👥</div>
            <h4 className="text-lg font-semibold text-gray-900 mb-2">Manage Customers</h4>
            <p className="text-sm text-gray-600">Add and manage your customers</p>
          </div>
        </div>
      </main>
    </div>
  );
}
