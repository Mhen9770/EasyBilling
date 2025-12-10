'use client';

import { useState, useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { inventoryApi } from '@/lib/api/inventory/inventoryApi';
import type { ProductRequest } from '@/lib/api/types';
import { useToastStore } from '@/components/ui/toast';
import { PageLoader, TableLoader, ButtonLoader, InlineLoader } from '@/components/ui/Loader';
import Link from 'next/link';

type ViewMode = 'grid' | 'table';
type FilterType = 'all' | 'active' | 'inactive' | 'low_stock' | 'out_of_stock';

export default function EnhancedProductsPage() {
  const [searchTerm, setSearchTerm] = useState('');
  const [viewMode, setViewMode] = useState<ViewMode>('grid');
  const [filter, setFilter] = useState<FilterType>('all');
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [selectedBrand, setSelectedBrand] = useState<string>('all');
  const [sortBy, setSortBy] = useState<string>('name');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [selectedProducts, setSelectedProducts] = useState<Set<number>>(new Set());
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<any>(null);
  const [isBulkModalOpen, setIsBulkModalOpen] = useState(false);
  
  const queryClient = useQueryClient();
  const { addToast } = useToastStore();

  // Fetch data
  const { data: productsData, isLoading } = useQuery({
    queryKey: ['products', searchTerm, filter],
    queryFn: () => inventoryApi.listProducts(0, 1000),
  });

  const { data: categories } = useQuery({
    queryKey: ['categories'],
    queryFn: () => inventoryApi.getCategories(),
  });

  const { data: brands } = useQuery({
    queryKey: ['brands'],
    queryFn: () => inventoryApi.getBrands(),
  });

  // Filter and sort products
  const filteredAndSortedProducts = useMemo(() => {
    if (!productsData?.data?.content) return [];

    let products = [...productsData.data.content];

    // Apply search
    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      products = products.filter((p: any) =>
        p.name?.toLowerCase().includes(term) ||
        p.sku?.toLowerCase().includes(term) ||
        p.barcode?.toLowerCase().includes(term)
      );
    }

    // Apply filters
    if (filter === 'active') {
      products = products.filter((p: any) => p.isActive);
    } else if (filter === 'inactive') {
      products = products.filter((p: any) => !p.isActive);
    } else if (filter === 'low_stock') {
      products = products.filter((p: any) => p.trackStock && p.lowStockThreshold > 0);
    }

    if (selectedCategory !== 'all') {
      products = products.filter((p: any) => p.category?.id === parseInt(selectedCategory));
    }

    if (selectedBrand !== 'all') {
      products = products.filter((p: any) => p.brand?.id === parseInt(selectedBrand));
    }

    // Apply sorting
    products.sort((a: any, b: any) => {
      let aVal: any, bVal: any;
      
      switch (sortBy) {
        case 'name':
          aVal = a.name?.toLowerCase() || '';
          bVal = b.name?.toLowerCase() || '';
          break;
        case 'price':
          aVal = parseFloat(a.sellingPrice) || 0;
          bVal = parseFloat(b.sellingPrice) || 0;
          break;
        case 'created':
          aVal = new Date(a.createdAt).getTime();
          bVal = new Date(b.createdAt).getTime();
          break;
        default:
          aVal = a.name?.toLowerCase() || '';
          bVal = b.name?.toLowerCase() || '';
      }

      if (sortOrder === 'asc') {
        return aVal > bVal ? 1 : -1;
      } else {
        return aVal < bVal ? 1 : -1;
      }
    });

    return products;
  }, [productsData, searchTerm, filter, selectedCategory, selectedBrand, sortBy, sortOrder]);

  // Mutations
  const createMutation = useMutation({
    mutationFn: (data: ProductRequest) => inventoryApi.createProduct(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
      setIsCreateModalOpen(false);
      addToast('Product created successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to create product', 'error');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: ProductRequest }) =>
      inventoryApi.updateProduct(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
      setEditingProduct(null);
      setIsCreateModalOpen(false);
      addToast('Product updated successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to update product', 'error');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => inventoryApi.deleteProduct(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
      setSelectedProducts(new Set());
      addToast('Product(s) deleted successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to delete product', 'error');
    },
  });

  const bulkDeleteMutation = useMutation({
    mutationFn: async (ids: number[]) => {
      await Promise.all(ids.map(id => inventoryApi.deleteProduct(id.toString())));
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
      setSelectedProducts(new Set());
      setIsBulkModalOpen(false);
      addToast('Products deleted successfully', 'success');
    },
    onError: () => {
      addToast('Failed to delete some products', 'error');
    },
  });

  const handleBulkDelete = () => {
    if (selectedProducts.size === 0) return;
    if (confirm(`Are you sure you want to delete ${selectedProducts.size} product(s)?`)) {
      bulkDeleteMutation.mutate(Array.from(selectedProducts));
    }
  };

  const handleSelectAll = () => {
    if (selectedProducts.size === filteredAndSortedProducts.length) {
      setSelectedProducts(new Set());
    } else {
      setSelectedProducts(new Set(filteredAndSortedProducts.map((p: any) => p.id)));
    }
  };

  const handleSelectProduct = (id: number) => {
    const newSelected = new Set(selectedProducts);
    if (newSelected.has(id)) {
      newSelected.delete(id);
    } else {
      newSelected.add(id);
    }
    setSelectedProducts(newSelected);
  };

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    
    const sellingPrice = parseFloat(formData.get('price') as string);
    const costPrice = parseFloat(formData.get('cost') as string);
    const mrpValue = formData.get('mrp') ? parseFloat(formData.get('mrp') as string) : sellingPrice;
    
    const productData: ProductRequest = {
      name: formData.get('name') as string,
      sku: formData.get('sku') as string,
      barcode: formData.get('barcode') as string,
      description: formData.get('description') as string,
      categoryId: formData.get('categoryId') as string || undefined,
      brandId: formData.get('brandId') as string || undefined,
      sellingPrice: sellingPrice,
      costPrice: costPrice,
      mrp: mrpValue,
      taxRate: parseFloat(formData.get('taxRate') as string) || 0,
      unit: formData.get('unit') as string,
      trackStock: formData.get('trackStock') === 'true',
      lowStockThreshold: parseInt(formData.get('lowStockThreshold') as string) || 10,
    };

    if (editingProduct) {
      updateMutation.mutate({ id: editingProduct.id.toString(), data: productData });
    } else {
      createMutation.mutate(productData);
    }
  };

  if (isLoading) {
    return <PageLoader text="Loading products..." />;
  }

  return (
    <div className="w-full space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Product Catalog</h1>
          <p className="text-gray-600 mt-1">Manage your complete product inventory</p>
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => setViewMode(viewMode === 'grid' ? 'table' : 'grid')}
            className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors text-sm font-medium"
          >
            {viewMode === 'grid' ? '📋 Table' : '🎴 Grid'}
          </button>
          <button
            onClick={() => setIsCreateModalOpen(true)}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium flex items-center gap-2"
          >
            <span>+</span>
            <span>Add Product</span>
          </button>
        </div>
      </div>

      {/* Toolbar */}
      <div className="bg-white rounded-xl shadow-md p-4 space-y-4">
        {/* Search and Filters Row */}
        <div className="flex flex-col lg:flex-row gap-4">
          {/* Search */}
          <div className="flex-1">
            <div className="relative">
              <input
                type="text"
                placeholder="Search by name, SKU, or barcode..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
              <span className="absolute left-3 top-2.5 text-gray-400">🔍</span>
            </div>
          </div>

          {/* Filters */}
          <div className="flex gap-2 flex-wrap">
            <select
              value={filter}
              onChange={(e) => setFilter(e.target.value as FilterType)}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm"
            >
              <option value="all">All Products</option>
              <option value="active">Active</option>
              <option value="inactive">Inactive</option>
              <option value="low_stock">Low Stock</option>
              <option value="out_of_stock">Out of Stock</option>
            </select>

            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm"
            >
              <option value="all">All Categories</option>
              {categories?.data?.map((cat: any) => (
                <option key={cat.id} value={cat.id}>{cat.name}</option>
              ))}
            </select>

            <select
              value={selectedBrand}
              onChange={(e) => setSelectedBrand(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm"
            >
              <option value="all">All Brands</option>
              {brands?.data?.map((brand: any) => (
                <option key={brand.id} value={brand.id}>{brand.name}</option>
              ))}
            </select>

            <select
              value={`${sortBy}-${sortOrder}`}
              onChange={(e) => {
                const [field, order] = e.target.value.split('-');
                setSortBy(field);
                setSortOrder(order as 'asc' | 'desc');
              }}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm"
            >
              <option value="name-asc">Name (A-Z)</option>
              <option value="name-desc">Name (Z-A)</option>
              <option value="price-asc">Price (Low-High)</option>
              <option value="price-desc">Price (High-Low)</option>
              <option value="created-desc">Newest First</option>
              <option value="created-asc">Oldest First</option>
            </select>
          </div>
        </div>

        {/* Bulk Actions */}
        {selectedProducts.size > 0 && (
          <div className="flex items-center justify-between p-3 bg-blue-50 rounded-lg border border-blue-200">
            <span className="text-sm font-medium text-blue-900">
              {selectedProducts.size} product(s) selected
            </span>
            <div className="flex gap-2">
              <button
                onClick={handleBulkDelete}
                disabled={bulkDeleteMutation.isPending}
                className="px-3 py-1.5 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50 text-sm font-medium flex items-center gap-2"
              >
                {bulkDeleteMutation.isPending && <ButtonLoader size="sm" />}
                <span>Delete Selected</span>
              </button>
              <button
                onClick={() => setSelectedProducts(new Set())}
                className="px-3 py-1.5 border border-gray-300 rounded-lg hover:bg-gray-50 text-sm font-medium"
              >
                Clear
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Products Display */}
      {viewMode === 'grid' ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 sm:gap-6">
          {filteredAndSortedProducts.map((product: any) => (
            <ProductCard
              key={product.id}
              product={product}
              isSelected={selectedProducts.has(product.id)}
              onSelect={() => handleSelectProduct(product.id)}
              onEdit={() => {
                setEditingProduct(product);
                setIsCreateModalOpen(true);
              }}
              onDelete={() => {
                if (confirm('Are you sure you want to delete this product?')) {
                  deleteMutation.mutate(product.id.toString());
                }
              }}
            />
          ))}
        </div>
      ) : (
        <ProductTable
          products={filteredAndSortedProducts}
          selectedProducts={selectedProducts}
          onSelectAll={handleSelectAll}
          onSelectProduct={handleSelectProduct}
          onEdit={(product) => {
            setEditingProduct(product);
            setIsCreateModalOpen(true);
          }}
          onDelete={(id) => {
            if (confirm('Are you sure you want to delete this product?')) {
              deleteMutation.mutate(id.toString());
            }
          }}
        />
      )}

      {filteredAndSortedProducts.length === 0 && (
        <div className="text-center py-12 bg-white rounded-xl shadow-md">
          <div className="text-6xl mb-4">📦</div>
          <h3 className="text-lg font-semibold text-gray-900 mb-2">No products found</h3>
          <p className="text-gray-600 mb-4">Try adjusting your search or filters</p>
          <button
            onClick={() => setIsCreateModalOpen(true)}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            Add Your First Product
          </button>
        </div>
      )}

      {/* Create/Edit Modal */}
      {(isCreateModalOpen || editingProduct) && (
        <ProductModal
          product={editingProduct}
          categories={categories?.data || []}
          brands={brands?.data || []}
          onSubmit={handleSubmit}
          onClose={() => {
            setIsCreateModalOpen(false);
            setEditingProduct(null);
          }}
          isSubmitting={createMutation.isPending || updateMutation.isPending}
        />
      )}
    </div>
  );
}

// Product Card Component
function ProductCard({ product, isSelected, onSelect, onEdit, onDelete }: any) {
  const isLowStock = product.trackStock && product.lowStockThreshold > 0;

  return (
    <div className={`bg-white rounded-xl shadow-md hover:shadow-lg transition-all p-6 border-2 ${
      isSelected ? 'border-blue-500' : 'border-transparent'
    }`}>
      {/* Checkbox */}
      <div className="flex justify-end mb-2">
        <input
          type="checkbox"
          checked={isSelected}
          onChange={onSelect}
          className="w-4 h-4 text-blue-600 rounded focus:ring-blue-500"
        />
      </div>

      {/* Product Image */}
      <div className="w-full h-48 bg-gradient-to-br from-gray-100 to-gray-200 rounded-lg mb-4 flex items-center justify-center overflow-hidden">
        {product.imageUrl ? (
          <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover" />
        ) : (
          <div className="text-6xl text-gray-400">📦</div>
        )}
      </div>

      {/* Product Info */}
      <div className="space-y-2">
        <div className="flex items-start justify-between">
          <h3 className="text-lg font-semibold text-gray-900 line-clamp-2">{product.name}</h3>
          <span className={`px-2 py-1 rounded text-xs font-medium ${
            product.isActive ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
          }`}>
            {product.isActive ? 'Active' : 'Inactive'}
          </span>
        </div>

        <div className="text-sm text-gray-600 space-y-1">
          <p><span className="font-medium">SKU:</span> {product.sku}</p>
          <p><span className="font-medium">Barcode:</span> {product.barcode}</p>
          {product.category && (
            <p><span className="font-medium">Category:</span> {product.category.name}</p>
          )}
        </div>

        <div className="flex items-center justify-between pt-2 border-t">
          <div>
            <p className="text-2xl font-bold text-gray-900">₹{parseFloat(product.sellingPrice || 0).toFixed(2)}</p>
            <p className="text-xs text-gray-500">Cost: ₹{parseFloat(product.costPrice || 0).toFixed(2)}</p>
          </div>
          {isLowStock && (
            <span className="px-2 py-1 bg-yellow-100 text-yellow-800 rounded text-xs font-medium">
              ⚠️ Low Stock
            </span>
          )}
        </div>

        {/* Actions */}
        <div className="flex gap-2 pt-2">
          <button
            onClick={onEdit}
            className="flex-1 px-3 py-2 bg-blue-50 text-blue-600 rounded-lg hover:bg-blue-100 text-sm font-medium transition-colors"
          >
            Edit
          </button>
          <button
            onClick={onDelete}
            className="flex-1 px-3 py-2 bg-red-50 text-red-600 rounded-lg hover:bg-red-100 text-sm font-medium transition-colors"
          >
            Delete
          </button>
        </div>
      </div>
    </div>
  );
}

// Product Table Component
function ProductTable({ products, selectedProducts, onSelectAll, onSelectProduct, onEdit, onDelete }: any) {
  const allSelected = products.length > 0 && selectedProducts.size === products.length;

  return (
    <div className="bg-white rounded-xl shadow-md overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-4 py-3 text-left">
                <input
                  type="checkbox"
                  checked={allSelected}
                  onChange={onSelectAll}
                  className="w-4 h-4 text-blue-600 rounded focus:ring-blue-500"
                />
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Product</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">SKU</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Category</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Price</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Stock</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
              <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {products.map((product: any) => (
              <tr key={product.id} className="hover:bg-gray-50">
                <td className="px-4 py-3">
                  <input
                    type="checkbox"
                    checked={selectedProducts.has(product.id)}
                    onChange={() => onSelectProduct(product.id)}
                    className="w-4 h-4 text-blue-600 rounded focus:ring-blue-500"
                  />
                </td>
                <td className="px-4 py-3">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-gradient-to-br from-gray-100 to-gray-200 rounded flex items-center justify-center">
                      {product.imageUrl ? (
                        <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover rounded" />
                      ) : (
                        <span className="text-xl">📦</span>
                      )}
                    </div>
                    <div>
                      <div className="font-medium text-gray-900">{product.name}</div>
                      <div className="text-sm text-gray-500">{product.barcode}</div>
                    </div>
                  </div>
                </td>
                <td className="px-4 py-3 text-sm text-gray-900">{product.sku}</td>
                <td className="px-4 py-3 text-sm text-gray-600">
                  {product.category?.name || '-'}
                </td>
                <td className="px-4 py-3">
                  <div className="text-sm font-medium text-gray-900">₹{parseFloat(product.sellingPrice || 0).toFixed(2)}</div>
                  <div className="text-xs text-gray-500">Cost: ₹{parseFloat(product.costPrice || 0).toFixed(2)}</div>
                </td>
                <td className="px-4 py-3">
                  {product.trackStock ? (
                    <span className="text-sm text-gray-600">Tracked</span>
                  ) : (
                    <span className="text-sm text-gray-400">Not Tracked</span>
                  )}
                </td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-1 rounded text-xs font-medium ${
                    product.isActive ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                  }`}>
                    {product.isActive ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td className="px-4 py-3 text-right">
                  <div className="flex justify-end gap-2">
                    <button
                      onClick={() => onEdit(product)}
                      className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                      title="Edit"
                    >
                      ✏️
                    </button>
                    <button
                      onClick={() => onDelete(product.id)}
                      className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                      title="Delete"
                    >
                      🗑️
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

// Product Modal Component
function ProductModal({ product, categories, brands, onSubmit, onClose, isSubmitting }: any) {
  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-xl max-w-3xl w-full max-h-[90vh] overflow-y-auto">
        <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center">
          <h2 className="text-2xl font-bold text-gray-900">
            {product ? 'Edit Product' : 'Create New Product'}
          </h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 text-2xl"
          >
            ×
          </button>
        </div>

        <form onSubmit={onSubmit} className="p-6 space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-2">Product Name *</label>
              <input
                type="text"
                name="name"
                required
                defaultValue={product?.name}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">SKU *</label>
              <input
                type="text"
                name="sku"
                required
                defaultValue={product?.sku}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Barcode *</label>
              <input
                type="text"
                name="barcode"
                required
                defaultValue={product?.barcode}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-2">Description</label>
              <textarea
                name="description"
                rows={3}
                defaultValue={product?.description}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Category</label>
              <select
                name="categoryId"
                defaultValue={product?.category?.id}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              >
                <option value="">Select Category</option>
                {categories.map((cat: any) => (
                  <option key={cat.id} value={cat.id}>{cat.name}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Brand</label>
              <select
                name="brandId"
                defaultValue={product?.brand?.id}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              >
                <option value="">Select Brand</option>
                {brands.map((brand: any) => (
                  <option key={brand.id} value={brand.id}>{brand.name}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Cost Price *</label>
              <input
                type="number"
                step="0.01"
                name="cost"
                required
                defaultValue={product?.costPrice}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Selling Price *</label>
              <input
                type="number"
                step="0.01"
                name="price"
                required
                defaultValue={product?.sellingPrice}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">MRP *</label>
              <input
                type="number"
                step="0.01"
                name="mrp"
                required
                defaultValue={product?.mrp || product?.sellingPrice}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Tax Rate (%)</label>
              <input
                type="number"
                step="0.01"
                name="taxRate"
                defaultValue={product?.taxRate || 0}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div className="md:col-span-2">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Unit *</label>
                  <select
                    name="unit"
                    required
                    defaultValue={product?.unit || 'PCS'}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="PCS">PCS</option>
                    <option value="KG">KG</option>
                    <option value="LITER">LITER</option>
                    <option value="METER">METER</option>
                    <option value="BOX">BOX</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Track Stock</label>
                  <select
                    name="trackStock"
                    defaultValue={product?.trackStock ? 'true' : 'false'}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="true">Yes</option>
                    <option value="false">No</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Low Stock Threshold</label>
                  <input
                    type="number"
                    name="lowStockThreshold"
                    defaultValue={product?.lowStockThreshold || 10}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>
            </div>
          </div>

          <div className="flex gap-3 pt-4 border-t">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors flex items-center justify-center gap-2"
            >
              {isSubmitting && <ButtonLoader size="sm" />}
              <span>{product ? 'Update Product' : 'Create Product'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

