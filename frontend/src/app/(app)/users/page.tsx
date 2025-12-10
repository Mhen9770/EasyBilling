'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { userApi, type UserRole, type UserResponse, type UserRequest } from '@/lib/api/user/userApi';
import { useToastStore } from '@/components/ui/toast';
import { PageLoader, TableLoader, ButtonLoader } from '@/components/ui/Loader';

const ROLES = [
  { value: 'ADMIN' as UserRole, label: 'Admin', description: 'Full system access' },
  { value: 'MANAGER' as UserRole, label: 'Manager', description: 'Manage operations and reports' },
  { value: 'STAFF' as UserRole, label: 'Staff', description: 'POS and basic operations' },
  { value: 'VIEWER' as UserRole, label: 'Viewer', description: 'Read-only access' },
];

export default function UsersPage() {
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<UserResponse | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterRole, setFilterRole] = useState<UserRole | 'ALL'>('ALL');
  const queryClient = useQueryClient();
  const { addToast } = useToastStore();

  const { data: usersResponse, isLoading } = useQuery({
    queryKey: ['users', filterRole],
    queryFn: () => userApi.listUsers(0, 50, filterRole === 'ALL' ? undefined : filterRole),
  });

  const users = usersResponse?.data?.content || [];

  const createMutation = useMutation({
    mutationFn: (data: any) => userApi.createUser(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setIsAddModalOpen(false);
      addToast('User created successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to create user', 'error');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: any }) => userApi.updateUser(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setEditingUser(null);
      addToast('User updated successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to update user', 'error');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => userApi.deleteUser(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      addToast('User deleted successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to delete user', 'error');
    },
  });

  const suspendMutation = useMutation({
    mutationFn: (id: string) => userApi.suspendUser(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      addToast('User suspended successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to suspend user', 'error');
    },
  });

  const activateMutation = useMutation({
    mutationFn: (id: string) => userApi.activateUser(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      addToast('User activated successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to activate user', 'error');
    },
  });

  const updateRoleMutation = useMutation({
    mutationFn: ({ id, role }: { id: string; role: UserRole }) => userApi.updateRole(id, role),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      addToast('User role updated successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to update user role', 'error');
    },
  });

  const filteredUsers = users.filter(user => {
    const matchesSearch = 
      user.username.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.fullName.toLowerCase().includes(searchQuery.toLowerCase());
    
    const matchesRole = filterRole === 'ALL' || user.role === filterRole;
    
    return matchesSearch && matchesRole;
  });

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    
    const userData: UserRequest = {
      username: formData.get('username') as string,
      email: formData.get('email') as string,
      fullName: formData.get('fullName') as string,
      phone: formData.get('phone') as string || undefined,
      role: formData.get('role') as UserRole,
      password: formData.get('password') as string || undefined,
    };

    if (editingUser) {
      updateMutation.mutate({ id: editingUser.id, data: userData });
    } else {
      createMutation.mutate(userData);
    }
  };

  const handleUpdateUserRole = (userId: string, role: string) => {
    updateRoleMutation.mutate({ id: userId, role: role as UserRole });
  };

  const handleUpdateUserStatus = (userId: string, status: 'ACTIVE' | 'SUSPENDED') => {
    if (status === 'ACTIVE') {
      activateMutation.mutate(userId);
    } else {
      suspendMutation.mutate(userId);
    }
  };

  const handleDeleteUser = (userId: string) => {
    handleDelete(userId);
  };

  const handleEdit = (user: UserResponse) => {
    setEditingUser(user);
    setIsAddModalOpen(true);
  };

  const handleDelete = (userId: string) => {
    if (confirm('Are you sure you want to delete this user?')) {
      deleteMutation.mutate(userId);
    }
  };

  const handleToggleStatus = (user: UserResponse) => {
    if (user.status === 'ACTIVE') {
      suspendMutation.mutate(user.id);
    } else {
      activateMutation.mutate(user.id);
    }
  };

  const handleCloseModal = () => {
    setIsAddModalOpen(false);
    setEditingUser(null);
  };

  if (isLoading) {
    return <PageLoader text="Loading users..." />;
  }

  return (
    <div className="w-full">
      <div className="w-full max-w-full">
        {/* Header */}
        <div className="mb-4 sm:mb-6 lg:mb-8">
          <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">User Management</h1>
          <p className="text-sm sm:text-base text-gray-600 mt-2">
            Manage team members, roles, and permissions
          </p>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6 mb-4 sm:mb-6 lg:mb-8">
          <div className="bg-white rounded-lg p-6 shadow-sm">
            <div className="text-sm text-gray-600 mb-1">Total Users</div>
            <div className="text-3xl font-bold text-gray-900">{users.length}</div>
          </div>
          <div className="bg-white rounded-lg p-6 shadow-sm">
            <div className="text-sm text-gray-600 mb-1">Active Users</div>
            <div className="text-3xl font-bold text-green-600">
              {users.filter(u => u.status === 'ACTIVE').length}
            </div>
          </div>
          <div className="bg-white rounded-lg p-6 shadow-sm">
            <div className="text-sm text-gray-600 mb-1">Admins</div>
            <div className="text-3xl font-bold text-blue-600">
              {users.filter(u => u.role === 'ADMIN').length}
            </div>
          </div>
          <div className="bg-white rounded-lg p-6 shadow-sm">
            <div className="text-sm text-gray-600 mb-1">Staff</div>
            <div className="text-3xl font-bold text-purple-600">
              {users.filter(u => u.role === 'STAFF').length}
            </div>
          </div>
        </div>

        {/* Toolbar */}
        <div className="bg-white rounded-lg shadow-sm p-3 sm:p-4 mb-4 sm:mb-6">
          <div className="flex flex-col sm:flex-row gap-3 sm:gap-4 justify-between">
            <div className="flex flex-col sm:flex-row gap-3 sm:gap-4 flex-1">
              {/* Search */}
              <div className="flex-1 min-w-0">
                <input
                  type="text"
                  placeholder="Search users..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full px-3 sm:px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm sm:text-base"
                />
              </div>

              {/* Role Filter */}
              <select
                value={filterRole}
                onChange={(e) => setFilterRole(e.target.value as UserRole | 'ALL')}
                className="px-3 sm:px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm sm:text-base w-full sm:w-auto"
              >
                <option value="ALL">All Roles</option>
                {ROLES.map(role => (
                  <option key={role.value} value={role.value}>
                    {role.label}
                  </option>
                ))}
              </select>
            </div>

            {/* Add User Button */}
            <button
              onClick={() => setIsAddModalOpen(true)}
              className="w-full sm:w-auto px-4 sm:px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium text-sm sm:text-base flex items-center justify-center gap-2"
            >
              <span>+</span>
              <span>Add User</span>
            </button>
          </div>
        </div>

        {/* Users Table */}
        <div className="bg-white rounded-lg shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-3 sm:px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    User
                  </th>
                  <th className="px-3 sm:px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Role
                  </th>
                  <th className="px-3 sm:px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="hidden sm:table-cell px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Last Login
                  </th>
                  <th className="px-3 sm:px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredUsers.map((user) => (
                  <tr key={user.id} className="hover:bg-gray-50">
                    <td className="px-3 sm:px-6 py-4">
                      <div className="flex items-center">
                        <div className="flex-shrink-0 h-8 w-8 sm:h-10 sm:w-10 bg-gradient-to-br from-blue-500 to-purple-600 rounded-full flex items-center justify-center text-white font-semibold text-xs sm:text-sm">
                          {user.fullName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)}
                        </div>
                        <div className="ml-2 sm:ml-4 min-w-0">
                          <div className="text-sm font-medium text-gray-900 truncate">
                            {user.fullName}
                          </div>
                          <div className="text-xs sm:text-sm text-gray-500 truncate">
                            {user.email}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="px-3 sm:px-6 py-4">
                      <select
                        value={user.role}
                        onChange={(e) => handleUpdateUserRole(user.id, e.target.value)}
                        disabled={updateRoleMutation.isPending}
                        className="text-xs sm:text-sm border border-gray-300 rounded px-2 py-1 focus:ring-2 focus:ring-blue-500 disabled:opacity-50 w-full sm:w-auto"
                      >
                        {ROLES.map(role => (
                          <option key={role.value} value={role.value}>
                            {role.label}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td className="px-3 sm:px-6 py-4">
                      <span
                        className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                          user.status === 'ACTIVE'
                            ? 'bg-green-100 text-green-800'
                            : user.status === 'SUSPENDED'
                            ? 'bg-yellow-100 text-yellow-800'
                            : 'bg-gray-100 text-gray-800'
                        }`}
                      >
                        {user.status}
                      </span>
                    </td>
                    <td className="hidden sm:table-cell px-6 py-4 text-sm text-gray-500">
                      {user.lastLogin
                        ? new Date(user.lastLogin).toLocaleDateString()
                        : 'Never'}
                    </td>
                    <td className="px-3 sm:px-6 py-4 text-right text-sm font-medium">
                      <div className="flex justify-end gap-1 sm:gap-2">
                        {user.status === 'ACTIVE' ? (
                          <button
                            onClick={() => handleUpdateUserStatus(user.id, 'SUSPENDED')}
                            disabled={suspendMutation.isPending}
                            className="text-yellow-600 hover:text-yellow-900 disabled:opacity-50 p-1"
                            title="Suspend User"
                          >
                            {suspendMutation.isPending ? (
                              <ButtonLoader size="sm" />
                            ) : (
                              <svg className="w-4 h-4 sm:w-5 sm:h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 9v6m4-6v6m7-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                              </svg>
                            )}
                          </button>
                        ) : (
                          <button
                            onClick={() => handleUpdateUserStatus(user.id, 'ACTIVE')}
                            disabled={activateMutation.isPending}
                            className="text-green-600 hover:text-green-900 disabled:opacity-50 p-1"
                            title="Activate User"
                          >
                            {activateMutation.isPending ? (
                              <ButtonLoader size="sm" />
                            ) : (
                              <svg className="w-4 h-4 sm:w-5 sm:h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                              </svg>
                            )}
                          </button>
                        )}
                        
                        <button
                          onClick={() => handleEdit(user)}
                          className="text-blue-600 hover:text-blue-900 p-1"
                          title="Edit User"
                        >
                          <svg className="w-4 h-4 sm:w-5 sm:h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                          </svg>
                        </button>
                        
                        <button
                          onClick={() => handleDelete(user.id)}
                          disabled={deleteMutation.isPending}
                          className="text-red-600 hover:text-red-900 disabled:opacity-50 p-1"
                          title="Delete User"
                        >
                          {deleteMutation.isPending ? (
                            <ButtonLoader size="sm" />
                          ) : (
                            <svg className="w-4 h-4 sm:w-5 sm:h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                            </svg>
                          )}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {filteredUsers.length === 0 && (
            <div className="text-center py-12">
              <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
              <h3 className="mt-2 text-sm font-medium text-gray-900">No users found</h3>
              <p className="mt-1 text-sm text-gray-500">
                Try adjusting your search or filter criteria
              </p>
            </div>
          )}
        </div>

        {/* Add/Edit User Modal */}
        {isAddModalOpen && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-lg max-w-md w-full max-h-[90vh] overflow-y-auto p-4 sm:p-6">
              <h2 className="text-xl sm:text-2xl font-bold text-gray-900 mb-4">
                {editingUser ? 'Edit User' : 'Add New User'}
              </h2>
              
              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Full Name *
                  </label>
                  <input
                    type="text"
                    name="fullName"
                    required
                    defaultValue={editingUser?.fullName}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm sm:text-base"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Username *
                  </label>
                  <input
                    type="text"
                    name="username"
                    required
                    defaultValue={editingUser?.username}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm sm:text-base"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Email *
                  </label>
                  <input
                    type="email"
                    name="email"
                    required
                    defaultValue={editingUser?.email}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm sm:text-base"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Phone
                  </label>
                  <input
                    type="tel"
                    name="phone"
                    defaultValue={editingUser?.phone}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm sm:text-base"
                  />
                </div>

                {!editingUser && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Password *
                    </label>
                    <input
                      type="password"
                      name="password"
                      required={!editingUser}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm sm:text-base"
                    />
                  </div>
                )}

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Role *
                  </label>
                  <select
                    name="role"
                    required
                    defaultValue={editingUser?.role}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm sm:text-base"
                  >
                    {ROLES.map(role => (
                      <option key={role.value} value={role.value}>
                        {role.label} - {role.description}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="flex gap-3 pt-4">
                  <button
                    type="button"
                    onClick={handleCloseModal}
                    className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors text-sm sm:text-base"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={createMutation.isPending || updateMutation.isPending}
                    className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors text-sm sm:text-base flex items-center justify-center gap-2"
                  >
                    {(createMutation.isPending || updateMutation.isPending) && <ButtonLoader size="sm" />}
                    <span>{editingUser ? 'Update User' : 'Add User'}</span>
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
