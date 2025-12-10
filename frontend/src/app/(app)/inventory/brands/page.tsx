'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { inventoryApi, type BrandResponse } from '@/lib/api/inventory/inventoryApi';
import type { BrandRequest } from '@/lib/api/types';
import { useToastStore } from '@/components/ui/toast';

export default function BrandsPage() {
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [editingBrand, setEditingBrand] = useState<BrandResponse | null>(null);
  const queryClient = useQueryClient();
  const { addToast } = useToastStore();

  const { data: brandsResponse, isLoading } = useQuery({
    queryKey: ['brands'],
    queryFn: () => inventoryApi.getBrands(),
  });

  const brands = brandsResponse?.data || [];

  const createMutation = useMutation({
    mutationFn: (data: BrandRequest) => inventoryApi.createBrand(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['brands'] });
      setIsCreateModalOpen(false);
      addToast('Brand created successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to create brand', 'error');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: BrandRequest }) =>
      inventoryApi.updateBrand(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['brands'] });
      setEditingBrand(null);
      addToast('Brand updated successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to update brand', 'error');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => inventoryApi.deleteBrand(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['brands'] });
      addToast('Brand deleted successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to delete brand', 'error');
    },
  });

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    
    const brandData: BrandRequest = {
      name: formData.get('name') as string,
      description: formData.get('description') as string || undefined,
      website: formData.get('website') as string || undefined,
      logoUrl: formData.get('logoUrl') as string || undefined,
    };

    if (editingBrand) {
      updateMutation.mutate({ id: editingBrand.id, data: brandData });
    } else {
      createMutation.mutate(brandData);
    }
  };

  const handleEdit = (brand: BrandResponse) => {
    setEditingBrand(brand);
    setIsCreateModalOpen(true);
  };

  const handleDelete = (id: string) => {
    if (confirm('Are you sure you want to delete this brand?')) {
      deleteMutation.mutate(id);
    }
  };

  const handleCloseModal = () => {
    setIsCreateModalOpen(false);
    setEditingBrand(null);
  };

  return (
    <div className="max-w-6xl mx-auto">
      <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold text-gray-900">Brands</h1>
          <button
            onClick={() => setIsCreateModalOpen(true)}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            + Add Brand
          </button>
        </div>

        {isLoading ? (
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            <p className="mt-4 text-gray-600">Loading brands...</p>
          </div>
        ) : brands.length === 0 ? (
          <div className="bg-white rounded-lg shadow-md p-12 text-center">
            <div className="text-6xl mb-4">⭐</div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">No Brands Yet</h3>
            <p className="text-gray-600 mb-6">
              Add brands to organize products by manufacturer or brand name
            </p>
            <button
              onClick={() => setIsCreateModalOpen(true)}
              className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              Add Your First Brand
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {brands.map((brand: any) => (
              <div key={brand.id} className="bg-white rounded-lg shadow-md p-6">
                <div className="flex items-start gap-4 mb-4">
                  {brand.logoUrl ? (
                    <img
                      src={brand.logoUrl}
                      alt={brand.name}
                      className="w-16 h-16 object-contain rounded"
                    />
                  ) : (
                    <div className="w-16 h-16 bg-gray-200 rounded flex items-center justify-center">
                      <span className="text-2xl font-bold text-gray-400">
                        {brand.name.charAt(0)}
                      </span>
                    </div>
                  )}
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-gray-900">{brand.name}</h3>
                    {brand.website && (
                      <a
                        href={brand.website}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-sm text-blue-600 hover:underline"
                      >
                        {brand.website}
                      </a>
                    )}
                  </div>
                </div>

                <p className="text-sm text-gray-600 mb-4">{brand.description}</p>

                <div className="flex items-center justify-between">
                  <span className={`px-2 py-1 rounded text-xs font-medium ${
                    brand.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                  }`}>
                    {brand.active ? 'Active' : 'Inactive'}
                  </span>
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleEdit(brand)}
                      className="text-blue-600 hover:text-blue-900 text-sm"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => handleDelete(brand.id)}
                      className="text-red-600 hover:text-red-900 text-sm"
                    >
                      Delete
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Create/Edit Modal */}
        {isCreateModalOpen && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-lg max-w-lg w-full p-6">
              <h2 className="text-2xl font-bold mb-6">
                {editingBrand ? 'Edit Brand' : 'Create New Brand'}
              </h2>
              
              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Brand Name *</label>
                  <input
                    type="text"
                    name="name"
                    required
                    defaultValue={editingBrand?.name}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                  <textarea
                    name="description"
                    rows={3}
                    defaultValue={editingBrand?.description}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Website</label>
                  <input
                    type="url"
                    name="website"
                    placeholder="https://example.com"
                    defaultValue={editingBrand?.logo}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Logo URL</label>
                  <input
                    type="url"
                    name="logoUrl"
                    placeholder="https://example.com/logo.png"
                    defaultValue={editingBrand?.logo}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div className="flex gap-3 pt-4">
                  <button
                    type="submit"
                    disabled={createMutation.isPending || updateMutation.isPending}
                    className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
                  >
                    {createMutation.isPending || updateMutation.isPending 
                      ? 'Saving...' 
                      : editingBrand ? 'Update Brand' : 'Create Brand'}
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
