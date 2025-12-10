'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { inventoryApi } from '@/lib/api/inventory/inventoryApi';
import type { StockMovementRequest } from '@/lib/api/types';
import { useToastStore } from '@/components/ui/toast';

export default function StockPage() {
  const [selectedProduct, setSelectedProduct] = useState<string>('');
  const [isMovementModalOpen, setIsMovementModalOpen] = useState(false);
  const queryClient = useQueryClient();
  const { addToast } = useToastStore();

  const { data: products } = useQuery({
    queryKey: ['products'],
    queryFn: () => inventoryApi.getProducts({ page: 0, size: 100 }),
  });

  const { data: stock, isLoading } = useQuery({
    queryKey: ['stock', selectedProduct],
    queryFn: () => inventoryApi.getStock(selectedProduct),
    enabled: !!selectedProduct,
  });

  const movementMutation = useMutation({
    mutationFn: (data: StockMovementRequest) => inventoryApi.recordStockMovement(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['stock'] });
      setIsMovementModalOpen(false);
      addToast('Stock movement recorded successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to record stock movement', 'error');
    },
  });

  const handleMovementSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    
    const movementData: StockMovementRequest = {
      productId: selectedProduct,
      movementType: formData.get('movementType') as 'IN' | 'OUT' | 'TRANSFER' | 'ADJUSTMENT',
      quantity: parseInt(formData.get('quantity') as string),
      locationId: formData.get('locationId') as string,
      reference: formData.get('reference') as string,
      notes: formData.get('notes') as string,
    };

    movementMutation.mutate(movementData);
  };

  return (
    <div className="max-w-7xl mx-auto">
      <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold text-gray-900">Stock Management</h1>
          <button
            onClick={() => setIsMovementModalOpen(true)}
            disabled={!selectedProduct}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            + Record Movement
          </button>
        </div>

        {/* Product Selection */}
        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">Select Product</label>
          <select
            value={selectedProduct}
            onChange={(e) => setSelectedProduct(e.target.value)}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
          >
            <option value="">-- Select a product --</option>
            {products?.data?.content?.map((product: any) => (
              <option key={product.id} value={product.id}>
                {product.name} ({product.sku})
              </option>
            ))}
          </select>
        </div>

        {/* Stock Information */}
        {selectedProduct && (
          <div className="bg-white rounded-lg shadow-md p-6 mb-6">
            {isLoading ? (
              <div className="text-center py-8">
                <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              </div>
            ) : stock?.data ? (
              <div>
                <h2 className="text-xl font-semibold mb-4">Current Stock Levels</h2>
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                  <div className="bg-blue-50 rounded-lg p-4">
                    <div className="text-sm text-blue-600 font-medium">Total Quantity</div>
                    <div className="text-3xl font-bold text-blue-900 mt-2">
                      {stock.data.totalQuantity || 0}
                    </div>
                  </div>
                  <div className="bg-yellow-50 rounded-lg p-4">
                    <div className="text-sm text-yellow-600 font-medium">Reserved</div>
                    <div className="text-3xl font-bold text-yellow-900 mt-2">
                      {stock.data.reservedQuantity || 0}
                    </div>
                  </div>
                  <div className="bg-green-50 rounded-lg p-4">
                    <div className="text-sm text-green-600 font-medium">Available</div>
                    <div className="text-3xl font-bold text-green-900 mt-2">
                      {stock.data.availableQuantity || 0}
                    </div>
                  </div>
                  <div className="bg-purple-50 rounded-lg p-4">
                    <div className="text-sm text-purple-600 font-medium">Location</div>
                    <div className="text-lg font-semibold text-purple-900 mt-2">
                      {stock.data.locationName || 'Default'}
                    </div>
                  </div>
                </div>

                {/* Low Stock Warning */}
                {stock.data.availableQuantity <= stock.data.lowStockThreshold && (
                  <div className="mt-4 bg-red-50 border border-red-200 rounded-lg p-4">
                    <div className="flex items-center gap-2">
                      <svg className="w-5 h-5 text-red-600" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                      </svg>
                      <span className="font-medium text-red-800">Low Stock Alert!</span>
                      <span className="text-red-600">
                        Stock is below threshold ({stock.data.lowStockThreshold} units)
                      </span>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <div className="text-center py-8 text-gray-500">
                No stock information available for this product
              </div>
            )}
          </div>
        )}

        {/* Stock Movement Modal */}
        {isMovementModalOpen && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-lg max-w-lg w-full p-6">
              <h2 className="text-2xl font-bold mb-6">Record Stock Movement</h2>
              
              <form onSubmit={handleMovementSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Movement Type *</label>
                  <select
                    name="movementType"
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="IN">Stock In (Purchase/Receipt)</option>
                    <option value="OUT">Stock Out (Sale/Issue)</option>
                    <option value="TRANSFER">Transfer</option>
                    <option value="ADJUSTMENT">Adjustment</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Quantity *</label>
                  <input
                    type="number"
                    name="quantity"
                    required
                    min="1"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Location ID *</label>
                  <input
                    type="text"
                    name="locationId"
                    required
                    defaultValue="LOC-001"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Reference</label>
                  <input
                    type="text"
                    name="reference"
                    placeholder="e.g., PO-001, INV-001"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Notes</label>
                  <textarea
                    name="notes"
                    rows={3}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div className="flex gap-3 pt-4">
                  <button
                    type="submit"
                    disabled={movementMutation.isPending}
                    className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
                  >
                    {movementMutation.isPending ? 'Recording...' : 'Record Movement'}
                  </button>
                  <button
                    type="button"
                    onClick={() => setIsMovementModalOpen(false)}
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
