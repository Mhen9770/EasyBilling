'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { customerApi, type CustomerRequest, type CustomerResponse } from '@/lib/api/customer/customerApi';
import { useToastStore } from '@/components/ui/toast';

export default function CustomersPage() {
  const [searchTerm, setSearchTerm] = useState('');
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<CustomerResponse | null>(null);
  const queryClient = useQueryClient();
  const { addToast } = useToastStore();

  // Fetch customers
  const { data: customersResponse, isLoading } = useQuery({
    queryKey: ['customers', searchTerm],
    queryFn: () => customerApi.listCustomers(0, 50, searchTerm || undefined),
  });

  const customers = customersResponse?.data?.content || [];

  // Create mutation
  const createMutation = useMutation({
    mutationFn: (data: CustomerRequest) => customerApi.createCustomer(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['customers'] });
      setIsCreateModalOpen(false);
      addToast('Customer created successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to create customer', 'error');
    },
  });

  // Update mutation
  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: CustomerRequest }) =>
      customerApi.updateCustomer(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['customers'] });
      setEditingCustomer(null);
      addToast('Customer updated successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to update customer', 'error');
    },
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: (id: string) => customerApi.deleteCustomer(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['customers'] });
      addToast('Customer deleted successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to delete customer', 'error');
    },
  });

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    
    const customerData: CustomerRequest = {
      name: formData.get('name') as string,
      phone: formData.get('phone') as string,
      email: formData.get('email') as string || undefined,
      address: formData.get('address') as string || undefined,
      city: formData.get('city') as string || undefined,
      state: formData.get('state') as string || undefined,
      pincode: formData.get('pincode') as string || undefined,
      gstin: formData.get('gstin') as string || undefined,
    };

    if (editingCustomer) {
      updateMutation.mutate({ id: editingCustomer.id, data: customerData });
    } else {
      createMutation.mutate(customerData);
    }
  };

  const handleEdit = (customer: CustomerResponse) => {
    setEditingCustomer(customer);
    setIsCreateModalOpen(true);
  };

  const handleDelete = (id: string) => {
    if (confirm('Are you sure you want to delete this customer?')) {
      deleteMutation.mutate(id);
    }
  };

  const handleCloseModal = () => {
    setIsCreateModalOpen(false);
    setEditingCustomer(null);
  };

  return (
    <div className="max-w-7xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Customers</h1>
        <button
          onClick={() => setIsCreateModalOpen(true)}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          + Add Customer
        </button>
      </div>

      {/* Search Bar */}
      <div className="mb-6">
        <input
          type="text"
          placeholder="Search customers by name, phone, or email..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        />
      </div>

      {/* Customers List */}
      {customers.length === 0 ? (
        <div className="bg-white rounded-lg shadow-md p-12 text-center">
          <div className="text-6xl mb-4">👥</div>
          <h3 className="text-xl font-semibold text-gray-900 mb-2">No Customers Yet</h3>
          <p className="text-gray-600 mb-6">
            Start by adding your first customer to keep track of their purchases and information.
          </p>
          <button
            onClick={() => setIsCreateModalOpen(true)}
            className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            Add Your First Customer
          </button>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow-md overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Customer
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Contact
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  GSTIN
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Total Purchases
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {customers.map((customer) => (
                <tr key={customer.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-gray-900">{customer.name}</div>
                    <div className="text-sm text-gray-500">{customer.address}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">{customer.phone}</div>
                    <div className="text-sm text-gray-500">{customer.email}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {customer.gstin || '-'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    ₹{customer.totalPurchases.toFixed(2)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    <button 
                      onClick={() => handleEdit(customer)}
                      className="text-blue-600 hover:text-blue-900 mr-3"
                    >
                      Edit
                    </button>
                    <button 
                      onClick={() => handleDelete(customer.id)}
                      className="text-red-600 hover:text-red-900"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Create/Edit Modal */}
      {isCreateModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-2xl w-full p-6">
            <h2 className="text-2xl font-bold mb-6">
              {editingCustomer ? 'Edit Customer' : 'Add New Customer'}
            </h2>
            
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Customer Name *</label>
                <input
                  type="text"
                  name="name"
                  required
                  defaultValue={editingCustomer?.name}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Phone *</label>
                  <input
                    type="tel"
                    name="phone"
                    required
                    defaultValue={editingCustomer?.phone}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                  <input
                    type="email"
                    name="email"
                    defaultValue={editingCustomer?.email}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Address</label>
                <textarea
                  name="address"
                  rows={3}
                  defaultValue={editingCustomer?.address}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div className="grid grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">City</label>
                  <input
                    type="text"
                    name="city"
                    defaultValue={editingCustomer?.city}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">State</label>
                  <input
                    type="text"
                    name="state"
                    defaultValue={editingCustomer?.state}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Pincode</label>
                  <input
                    type="text"
                    name="pincode"
                    defaultValue={editingCustomer?.pincode}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">GSTIN</label>
                <input
                  type="text"
                  name="gstin"
                  placeholder="e.g., 22AAAAA0000A1Z5"
                  defaultValue={editingCustomer?.gstin}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  type="submit"
                  disabled={createMutation.isPending || updateMutation.isPending}
                  className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
                >
                  {createMutation.isPending || updateMutation.isPending 
                    ? 'Saving...' 
                    : editingCustomer ? 'Update Customer' : 'Add Customer'}
                </button>
                <button
                  type="button"
                  onClick={handleCloseModal}
                  className="flex-1 px-4 py-2 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
