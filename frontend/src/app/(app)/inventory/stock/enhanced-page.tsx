'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { inventoryApi } from '@/lib/api/inventory/inventoryApi';
import { useToastStore } from '@/components/ui/toast';
import { Button, Card, Input, Select, Modal, Table, Badge, StatCard } from '@/components/ui';

type TabType = 'overview' | 'movements' | 'adjustments' | 'transfers' | 'alerts';

interface Location {
  id: string;
  name: string;
}

export default function EnhancedStockPage() {
  const [activeTab, setActiveTab] = useState<TabType>('overview');
  const [selectedProduct, setSelectedProduct] = useState<string>('');
  const [selectedLocation, setSelectedLocation] = useState<string>('LOC-001');
  const [isAdjustmentModalOpen, setIsAdjustmentModalOpen] = useState(false);
  const [isTransferModalOpen, setIsTransferModalOpen] = useState(false);
  const [isMovementModalOpen, setIsMovementModalOpen] = useState(false);
  
  const queryClient = useQueryClient();
  const { addToast } = useToastStore();

  // Mock locations - in real app, fetch from API
  const locations: Location[] = [
    { id: 'LOC-001', name: 'Main Store' },
    { id: 'LOC-002', name: 'Warehouse 1' },
    { id: 'LOC-003', name: 'Warehouse 2' },
  ];

  // Fetch products
  const { data: productsData } = useQuery({
    queryKey: ['products'],
    queryFn: () => inventoryApi.getProducts({ page: 0, size: 100 }),
  });

  // Fetch stock for selected product
  const { data: stockData, isLoading: stockLoading } = useQuery({
    queryKey: ['stock', selectedProduct],
    queryFn: () => inventoryApi.getStock(selectedProduct),
    enabled: !!selectedProduct,
  });

  // Fetch inventory dashboard
  const { data: dashboardData } = useQuery({
    queryKey: ['inventory-dashboard'],
    queryFn: () => inventoryApi.getStockSummary(),
  });

  // Fetch low stock alerts
  const { data: lowStockData } = useQuery({
    queryKey: ['low-stock-alerts', selectedLocation],
    queryFn: () => inventoryApi.getLowStockAlerts(selectedLocation),
  });

  // Stock adjustment mutation
  const adjustmentMutation = useMutation({
    mutationFn: (data: { productId: string; locationId: string; quantity: number; reason: string; notes?: string }) =>
      inventoryApi.adjustStock(data.productId, data.locationId, data.quantity, data.reason, data.notes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['stock'] });
      queryClient.invalidateQueries({ queryKey: ['inventory-dashboard'] });
      queryClient.invalidateQueries({ queryKey: ['low-stock-alerts'] });
      setIsAdjustmentModalOpen(false);
      addToast('Stock adjusted successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to adjust stock', 'error');
    },
  });

  // Stock transfer mutation
  const transferMutation = useMutation({
    mutationFn: (data: { productId: string; fromLocationId: string; toLocationId: string; quantity: number; notes?: string }) =>
      inventoryApi.transferStock(data.productId, data.fromLocationId, data.toLocationId, data.quantity, data.notes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['stock'] });
      queryClient.invalidateQueries({ queryKey: ['inventory-dashboard'] });
      setIsTransferModalOpen(false);
      addToast('Stock transferred successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to transfer stock', 'error');
    },
  });

  // Stock movement mutation
  const movementMutation = useMutation({
    mutationFn: (data: any) => inventoryApi.recordStockMovement(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['stock'] });
      queryClient.invalidateQueries({ queryKey: ['inventory-dashboard'] });
      setIsMovementModalOpen(false);
      addToast('Stock movement recorded successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to record movement', 'error');
    },
  });

  const handleAdjustmentSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    
    adjustmentMutation.mutate({
      productId: selectedProduct,
      locationId: formData.get('locationId') as string,
      quantity: parseFloat(formData.get('quantity') as string),
      reason: formData.get('reason') as string,
      notes: formData.get('notes') as string || undefined,
    });
  };

  const handleTransferSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    
    transferMutation.mutate({
      productId: selectedProduct,
      fromLocationId: formData.get('fromLocation') as string,
      toLocationId: formData.get('toLocation') as string,
      quantity: parseFloat(formData.get('quantity') as string),
      notes: formData.get('notes') as string || undefined,
    });
  };

  const handleMovementSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    
    movementMutation.mutate({
      productId: selectedProduct,
      locationId: formData.get('locationId') as string,
      movementType: formData.get('movementType') as 'IN' | 'OUT' | 'TRANSFER' | 'ADJUSTMENT',
      quantity: parseFloat(formData.get('quantity') as string),
      reference: formData.get('reference') as string,
      notes: formData.get('notes') as string,
    });
  };

  const products = productsData?.data?.content || [];
  const selectedProductData = products.find((p: any) => p.id === selectedProduct);

  return (
    <div className="max-w-7xl mx-auto p-6">
      {/* Header */}
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Advanced Stock Management</h1>
          <p className="text-gray-600 mt-1">Manage inventory, track movements, and monitor stock levels</p>
        </div>
        <div className="flex gap-3">
          <Button
            variant="secondary"
            onClick={() => setIsMovementModalOpen(true)}
            disabled={!selectedProduct}
          >
            📝 Record Movement
          </Button>
          <Button
            variant="primary"
            onClick={() => setIsAdjustmentModalOpen(true)}
            disabled={!selectedProduct}
          >
            ⚖️ Adjust Stock
          </Button>
          <Button
            variant="success"
            onClick={() => setIsTransferModalOpen(true)}
            disabled={!selectedProduct}
          >
            🔄 Transfer Stock
          </Button>
        </div>
      </div>

      {/* Dashboard Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <StatCard
          title="Total Products"
          value={dashboardData?.data?.summary?.totalProducts?.toString() || '0'}
          icon="📦"
          color="blue"
        />
        <StatCard
          title="Stock Value"
          value={`₹${dashboardData?.data?.summary?.totalStockValue?.toFixed(2) || '0.00'}`}
          icon="💰"
          color="green"
        />
        <StatCard
          title="Low Stock Items"
          value={dashboardData?.data?.summary?.lowStockItems?.toString() || '0'}
          icon="⚠️"
          color="yellow"
        />
        <StatCard
          title="Out of Stock"
          value={dashboardData?.data?.summary?.outOfStockItems?.toString() || '0'}
          icon="❌"
          color="red"
        />
      </div>

      {/* Product & Location Selection */}
      <Card className="mb-6">
        <Card.Header>
          <Card.Title>Select Product & Location</Card.Title>
        </Card.Header>
        <Card.Content>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Select
              label="Product"
              value={selectedProduct}
              onChange={(e) => setSelectedProduct(e.target.value)}
              options={[
                { value: '', label: '-- Select a product --' },
                ...products.map((p: any) => ({
                  value: p.id,
                  label: `${p.name} (${p.sku})`,
                })),
              ]}
            />
            <Select
              label="Location"
              value={selectedLocation}
              onChange={(e) => setSelectedLocation(e.target.value)}
              options={locations.map(loc => ({
                value: loc.id,
                label: loc.name,
              }))}
            />
          </div>
        </Card.Content>
      </Card>

      {/* Tabs */}
      <div className="mb-6">
        <div className="border-b border-gray-200">
          <nav className="-mb-px flex space-x-8">
            {[
              { id: 'overview', label: 'Overview', icon: '📊' },
              { id: 'movements', label: 'Stock Movements', icon: '📋' },
              { id: 'adjustments', label: 'Adjustments', icon: '⚖️' },
              { id: 'transfers', label: 'Transfers', icon: '🔄' },
              { id: 'alerts', label: 'Low Stock Alerts', icon: '⚠️' },
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as TabType)}
                className={`
                  py-4 px-1 border-b-2 font-medium text-sm whitespace-nowrap
                  ${activeTab === tab.id
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }
                `}
              >
                {tab.icon} {tab.label}
              </button>
            ))}
          </nav>
        </div>
      </div>

      {/* Tab Content */}
      {activeTab === 'overview' && (
        <Card>
          <Card.Header>
            <Card.Title>Stock Overview {selectedProductData && `- ${selectedProductData.name}`}</Card.Title>
          </Card.Header>
          <Card.Content>
            {!selectedProduct ? (
              <div className="text-center py-12 text-gray-500">
                <p className="text-lg">👆 Please select a product to view stock details</p>
              </div>
            ) : stockLoading ? (
              <div className="text-center py-12">
                <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
                <p className="mt-4 text-gray-600">Loading stock data...</p>
              </div>
            ) : stockData?.data && stockData.data.length > 0 ? (
              <div>
                {/* Stock Levels by Location */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
                  {stockData.data.map((stock: any) => (
                    <div key={stock.id} className="bg-gradient-to-br from-blue-50 to-indigo-50 rounded-lg p-6 border border-blue-200">
                      <div className="flex justify-between items-start mb-4">
                        <div>
                          <h3 className="font-semibold text-gray-900">{stock.locationName || stock.locationId}</h3>
                          <p className="text-sm text-gray-600">Location ID: {stock.locationId}</p>
                        </div>
                        {stock.availableQuantity <= stock.lowStockThreshold && (
                          <Badge variant="danger">Low Stock</Badge>
                        )}
                      </div>
                      <div className="space-y-3">
                        <div className="flex justify-between items-center">
                          <span className="text-sm text-gray-600">Total Quantity:</span>
                          <span className="text-2xl font-bold text-blue-900">{stock.totalQuantity || stock.quantity || 0}</span>
                        </div>
                        <div className="flex justify-between items-center">
                          <span className="text-sm text-gray-600">Reserved:</span>
                          <span className="text-lg font-semibold text-yellow-700">{stock.reservedQuantity || 0}</span>
                        </div>
                        <div className="flex justify-between items-center">
                          <span className="text-sm text-gray-600">Available:</span>
                          <span className="text-lg font-semibold text-green-700">{stock.availableQuantity || 0}</span>
                        </div>
                        {stock.lowStockThreshold && (
                          <div className="pt-2 border-t border-blue-200">
                            <div className="flex justify-between items-center text-xs">
                              <span className="text-gray-500">Threshold:</span>
                              <span className="text-gray-700">{stock.lowStockThreshold}</span>
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>

                {/* Product Details */}
                {selectedProductData && (
                  <div className="bg-gray-50 rounded-lg p-6">
                    <h3 className="font-semibold text-lg mb-4">Product Information</h3>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                      <div>
                        <p className="text-sm text-gray-600">SKU</p>
                        <p className="font-medium">{selectedProductData.sku}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Selling Price</p>
                        <p className="font-medium">₹{selectedProductData.sellingPrice}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Cost Price</p>
                        <p className="font-medium">₹{selectedProductData.costPrice}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">MRP</p>
                        <p className="font-medium">₹{selectedProductData.mrp}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Category</p>
                        <p className="font-medium">{selectedProductData.category?.name || 'N/A'}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Brand</p>
                        <p className="font-medium">{selectedProductData.brand?.name || 'N/A'}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Unit</p>
                        <p className="font-medium">{selectedProductData.unit}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Track Stock</p>
                        <Badge variant={selectedProductData.trackStock ? 'success' : 'default'}>
                          {selectedProductData.trackStock ? 'Yes' : 'No'}
                        </Badge>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <div className="text-center py-12 text-gray-500">
                <p className="text-lg">No stock records found for this product</p>
                <p className="mt-2">Click "Record Movement" to create initial stock</p>
              </div>
            )}
          </Card.Content>
        </Card>
      )}

      {activeTab === 'movements' && (
        <Card>
          <Card.Header>
            <Card.Title>Recent Stock Movements</Card.Title>
          </Card.Header>
          <Card.Content>
            <div className="text-center py-12 text-gray-500">
              <p className="text-lg">Stock movement history will be displayed here</p>
              <p className="mt-2">Includes IN, OUT, TRANSFER, and ADJUSTMENT records</p>
            </div>
          </Card.Content>
        </Card>
      )}

      {activeTab === 'adjustments' && (
        <Card>
          <Card.Header>
            <Card.Title>Stock Adjustments History</Card.Title>
          </Card.Header>
          <Card.Content>
            <div className="text-center py-12 text-gray-500">
              <p className="text-lg">Stock adjustment history will be displayed here</p>
              <p className="mt-2">Tracks manual stock corrections for damaged, theft, found items</p>
            </div>
          </Card.Content>
        </Card>
      )}

      {activeTab === 'transfers' && (
        <Card>
          <Card.Header>
            <Card.Title>Stock Transfer History</Card.Title>
          </Card.Header>
          <Card.Content>
            <div className="text-center py-12 text-gray-500">
              <p className="text-lg">Stock transfer history will be displayed here</p>
              <p className="mt-2">Tracks inter-location stock movements</p>
            </div>
          </Card.Content>
        </Card>
      )}

      {activeTab === 'alerts' && (
        <Card>
          <Card.Header>
            <Card.Title>Low Stock Alerts</Card.Title>
          </Card.Header>
          <Card.Content>
            {lowStockData?.data && lowStockData.data.length > 0 ? (
              <Table
                data={lowStockData.data}
                columns={[
                  {
                    header: 'Product',
                    accessor: 'productName',
                    cell: (value, row: any) => (
                      <div>
                        <div className="font-medium">{value}</div>
                        <div className="text-sm text-gray-500">SKU: {row.productSku || 'N/A'}</div>
                      </div>
                    ),
                  },
                  {
                    header: 'Location',
                    accessor: 'locationName',
                    cell: (value, row: any) => value || row.locationId,
                  },
                  {
                    header: 'Available',
                    accessor: 'availableQuantity',
                    cell: (value) => (
                      <span className="font-semibold text-red-600">{value || 0}</span>
                    ),
                  },
                  {
                    header: 'Threshold',
                    accessor: 'lowStockThreshold',
                    cell: (value) => value || 0,
                  },
                  {
                    header: 'Status',
                    accessor: 'availableQuantity',
                    cell: (value, row: any) => {
                      const threshold = row.lowStockThreshold || 0;
                      if (value === 0) return <Badge variant="danger">Out of Stock</Badge>;
                      if (value <= threshold) return <Badge variant="warning">Low Stock</Badge>;
                      return <Badge variant="success">In Stock</Badge>;
                    },
                  },
                ]}
              />
            ) : (
              <div className="text-center py-12 text-gray-500">
                <p className="text-lg">✅ No low stock alerts</p>
                <p className="mt-2">All products are adequately stocked</p>
              </div>
            )}
          </Card.Content>
        </Card>
      )}

      {/* Stock Adjustment Modal */}
      <Modal
        isOpen={isAdjustmentModalOpen}
        onClose={() => setIsAdjustmentModalOpen(false)}
        title="Adjust Stock"
      >
        <form onSubmit={handleAdjustmentSubmit} className="space-y-4">
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-4">
            <p className="text-sm text-blue-800">
              <strong>Product:</strong> {selectedProductData?.name} ({selectedProductData?.sku})
            </p>
          </div>

          <Select
            label="Location *"
            name="locationId"
            required
            options={locations.map(loc => ({
              value: loc.id,
              label: loc.name,
            }))}
          />

          <Input
            label="Quantity Adjustment *"
            name="quantity"
            type="number"
            step="0.01"
            required
            helperText="Use positive numbers to add stock, negative to reduce"
            placeholder="e.g., 10 or -5"
          />

          <Select
            label="Reason *"
            name="reason"
            required
            options={[
              { value: 'DAMAGED', label: 'Damaged Goods' },
              { value: 'THEFT', label: 'Theft/Loss' },
              { value: 'FOUND', label: 'Found/Discovered' },
              { value: 'EXPIRED', label: 'Expired' },
              { value: 'MANUAL_COUNT', label: 'Manual Stock Count' },
              { value: 'OTHER', label: 'Other' },
            ]}
          />

          <Input
            label="Notes"
            name="notes"
            placeholder="Additional details about this adjustment"
          />

          <div className="flex gap-3 pt-4">
            <Button
              type="submit"
              variant="primary"
              disabled={adjustmentMutation.isPending}
              className="flex-1"
            >
              {adjustmentMutation.isPending ? 'Adjusting...' : 'Adjust Stock'}
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={() => setIsAdjustmentModalOpen(false)}
              className="flex-1"
            >
              Cancel
            </Button>
          </div>
        </form>
      </Modal>

      {/* Stock Transfer Modal */}
      <Modal
        isOpen={isTransferModalOpen}
        onClose={() => setIsTransferModalOpen(false)}
        title="Transfer Stock"
      >
        <form onSubmit={handleTransferSubmit} className="space-y-4">
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-4">
            <p className="text-sm text-blue-800">
              <strong>Product:</strong> {selectedProductData?.name} ({selectedProductData?.sku})
            </p>
          </div>

          <Select
            label="From Location *"
            name="fromLocation"
            required
            options={locations.map(loc => ({
              value: loc.id,
              label: loc.name,
            }))}
          />

          <Select
            label="To Location *"
            name="toLocation"
            required
            options={locations.map(loc => ({
              value: loc.id,
              label: loc.name,
            }))}
          />

          <Input
            label="Quantity to Transfer *"
            name="quantity"
            type="number"
            step="0.01"
            min="0.01"
            required
            placeholder="e.g., 100"
          />

          <Input
            label="Notes"
            name="notes"
            placeholder="Reason for transfer or additional details"
          />

          <div className="flex gap-3 pt-4">
            <Button
              type="submit"
              variant="success"
              disabled={transferMutation.isPending}
              className="flex-1"
            >
              {transferMutation.isPending ? 'Transferring...' : 'Transfer Stock'}
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={() => setIsTransferModalOpen(false)}
              className="flex-1"
            >
              Cancel
            </Button>
          </div>
        </form>
      </Modal>

      {/* Stock Movement Modal */}
      <Modal
        isOpen={isMovementModalOpen}
        onClose={() => setIsMovementModalOpen(false)}
        title="Record Stock Movement"
      >
        <form onSubmit={handleMovementSubmit} className="space-y-4">
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-4">
            <p className="text-sm text-blue-800">
              <strong>Product:</strong> {selectedProductData?.name} ({selectedProductData?.sku})
            </p>
          </div>

          <Select
            label="Movement Type *"
            name="movementType"
            required
            options={[
              { value: 'IN', label: '📥 Stock In (Purchase/Receipt)' },
              { value: 'OUT', label: '📤 Stock Out (Sale/Issue)' },
              { value: 'TRANSFER', label: '🔄 Transfer' },
              { value: 'ADJUSTMENT', label: '⚖️ Adjustment' },
            ]}
          />

          <Select
            label="Location *"
            name="locationId"
            required
            options={locations.map(loc => ({
              value: loc.id,
              label: loc.name,
            }))}
          />

          <Input
            label="Quantity *"
            name="quantity"
            type="number"
            step="0.01"
            min="0.01"
            required
            placeholder="e.g., 100"
          />

          <Input
            label="Reference"
            name="reference"
            placeholder="e.g., PO-001, INV-001"
            helperText="Purchase order, invoice, or other reference number"
          />

          <Input
            label="Notes"
            name="notes"
            placeholder="Additional details about this movement"
          />

          <div className="flex gap-3 pt-4">
            <Button
              type="submit"
              variant="primary"
              disabled={movementMutation.isPending}
              className="flex-1"
            >
              {movementMutation.isPending ? 'Recording...' : 'Record Movement'}
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={() => setIsMovementModalOpen(false)}
              className="flex-1"
            >
              Cancel
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
