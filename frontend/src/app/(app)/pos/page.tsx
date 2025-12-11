'use client';

import { useState, useEffect, useRef, useMemo } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { usePOSStore } from '@/lib/store/posStore';
import { inventoryApi } from '@/lib/api/inventory/inventoryApi';
import { billingApi, InvoiceRequest, PaymentRequest, HeldInvoiceResponse } from '@/lib/api/billing/billingApi';
import { customerApi } from '@/lib/api/customer/customerApi';
import { useToastStore } from '@/components/ui/toast';
import { PageLoader, ButtonLoader, InlineLoader } from '@/components/ui/Loader';
import { GSTINInput } from '@/components/gst/GSTINInput';
import { GSTBreakdown } from '@/components/gst/GSTBreakdown';
import { formatIndianCurrency, convertCurrencyToWords, getStateCodeFromGSTIN } from '@/lib/utils/indianFormatter';

export default function POSPage() {
  const [searchInput, setSearchInput] = useState('');
  const [customerSearch, setCustomerSearch] = useState('');
  const [showCustomerSearch, setShowCustomerSearch] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState<any>(null);
  const [selectedPaymentMode, setSelectedPaymentMode] = useState<'CASH' | 'CARD' | 'UPI' | 'WALLET' | 'CREDIT'>('CASH');
  const [paymentAmount, setPaymentAmount] = useState('');
  const [paymentReference, setPaymentReference] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [showReceipt, setShowReceipt] = useState(false);
  const [completedInvoice, setCompletedInvoice] = useState<any>(null);
  const [showHoldList, setShowHoldList] = useState(false);
  const [discountModal, setDiscountModal] = useState<{ open: boolean; itemId?: string }>({ open: false });
  const [discountType, setDiscountType] = useState<'PERCENTAGE' | 'FLAT'>('PERCENTAGE');
  const [discountValue, setDiscountValue] = useState('');
  const [customerGstin, setCustomerGstin] = useState('');
  const [supplierGstin, setSupplierGstin] = useState('27AABCU9603R1ZX'); // Default supplier GSTIN
  const [showGstSection, setShowGstSection] = useState(false);
  
  const searchInputRef = useRef<HTMLInputElement>(null);
  const { addToast } = useToastStore();
  const queryClient = useQueryClient();

  const {
    cart,
    customer,
    payments,
    addToCart,
    updateCartItem,
    removeFromCart,
    clearCart,
    setCustomer,
    addPayment,
    removePayment,
    clearPayments,
    getSubtotal,
    getTotalTax,
    getTotalDiscount,
    getTotal,
    getPaidAmount,
    getBalanceAmount,
  } = usePOSStore();

  // Fetch products for search
  const { data: productsData } = useQuery({
    queryKey: ['products', 'pos-search'],
    queryFn: () => inventoryApi.listProducts(0, 1000),
    enabled: false, // Only fetch when needed
  });

  // Search customers
  const { data: customersData, isLoading: searchingCustomers } = useQuery({
    queryKey: ['customers', 'search', customerSearch],
    queryFn: () => customerApi.listCustomers(0, 20, customerSearch),
    enabled: customerSearch.length > 0 && showCustomerSearch,
  });

  // Filter products based on search
  const filteredProducts = useMemo(() => {
    if (!searchInput || !productsData?.data?.content) return [];
    const term = searchInput.toLowerCase();
    return productsData.data.content.filter((p: any) =>
      p.name?.toLowerCase().includes(term) ||
      p.sku?.toLowerCase().includes(term) ||
      p.barcode?.toLowerCase().includes(term)
    ).slice(0, 10);
  }, [searchInput, productsData]);

  // Search product mutation
  const searchProduct = useMutation({
    mutationFn: (barcode: string) => inventoryApi.findByBarcode(barcode),
    onSuccess: (response) => {
      if (response.data) {
        const product = response.data;
        addToCart({
          productId: (product.id || '').toString(),
          productName: product.name || '',
          quantity: 1,
          unitPrice: parseFloat((product.sellingPrice || product.basePrice || 0).toString()),
          taxRate: parseFloat((product.taxRate || 0).toString()),
        });
        setSearchInput('');
        addToast(`Added ${product.name} to cart`, 'success');
        searchInputRef.current?.focus();
      }
    },
    onError: () => {
      addToast('Product not found', 'error');
      setSearchInput('');
    },
  });

  // Create and complete invoice mutation
  const completeSaleMutation = useMutation({
    mutationFn: async ({ invoiceData, paymentsList }: { invoiceData: InvoiceRequest; paymentsList: PaymentRequest[] }) => {
      // Create invoice
      const invoiceResponse = await billingApi.createInvoice(invoiceData);
      if (invoiceResponse.data) {
        // Complete with payments
        const completeResponse = await billingApi.completeInvoice(
          invoiceResponse.data.id,
          paymentsList
        );
        return completeResponse.data;
      }
      throw new Error('Failed to create invoice');
    },
    onSuccess: (invoice) => {
      addToast('Sale completed successfully!', 'success');
      setCompletedInvoice(invoice);
      setShowReceipt(true);
      clearCart();
      clearPayments();
      setPaymentAmount('');
      setPaymentReference('');
      setSelectedCustomer(null);
      setCustomer({});
      setIsProcessing(false);
      queryClient.invalidateQueries({ queryKey: ['invoices'] });
      searchInputRef.current?.focus();
    },
    onError: (error: any) => {
      addToast(error?.response?.data?.message || 'Failed to complete sale', 'error');
      setIsProcessing(false);
    },
  });

  // Hold invoice mutation
  const holdInvoiceMutation = useMutation({
    mutationFn: (data: InvoiceRequest) => billingApi.holdInvoice(data),
    onSuccess: () => {
      addToast('Bill held successfully!', 'success');
      clearCart();
      clearPayments();
      setSelectedCustomer(null);
      setCustomer({});
      queryClient.invalidateQueries({ queryKey: ['held-invoices'] });
    },
    onError: () => {
      addToast('Failed to hold bill', 'error');
    },
  });

  // Fetch held invoices
  const { data: heldInvoicesData, refetch: refetchHeldInvoices } = useQuery({
    queryKey: ['held-invoices'],
    queryFn: () => billingApi.listHeldInvoices(),
    enabled: showHoldList,
  });

  // Resume held invoice mutation
  const resumeHeldInvoiceMutation = useMutation({
    mutationFn: (holdReference: string) => billingApi.resumeHeldInvoice(holdReference),
    onSuccess: (response) => {
      if (response.data) {
        const invoiceData = response.data;
        // Restore cart
        clearCart();
        invoiceData.items.forEach((item: any) => {
          addToCart({
            productId: item.productId,
            productName: item.productName,
            quantity: item.quantity,
            unitPrice: parseFloat(item.unitPrice.toString()),
            taxRate: parseFloat((item.taxRate || 0).toString()),
            discountType: item.discountType,
            discountValue: item.discountValue ? parseFloat(item.discountValue.toString()) : undefined,
          });
        });
        // Restore customer
        if (invoiceData.customerName || invoiceData.customerPhone) {
          setCustomer({
            name: invoiceData.customerName,
            phone: invoiceData.customerPhone,
            email: invoiceData.customerEmail,
          });
        }
        setShowHoldList(false);
        addToast('Held invoice resumed', 'success');
        searchInputRef.current?.focus();
      }
    },
    onError: () => {
      addToast('Failed to resume held invoice', 'error');
    },
  });

  // Delete held invoice mutation
  const deleteHeldInvoiceMutation = useMutation({
    mutationFn: (holdReference: string) => billingApi.deleteHeldInvoice(holdReference),
    onSuccess: () => {
      refetchHeldInvoices();
      addToast('Held invoice deleted', 'success');
    },
    onError: () => {
      addToast('Failed to delete held invoice', 'error');
    },
  });

  // Keyboard shortcuts
  useEffect(() => {
    const handleKeyPress = (e: KeyboardEvent) => {
      // Focus search on any key press (if not typing in input)
      if (e.target instanceof HTMLInputElement || e.target instanceof HTMLTextAreaElement) {
        return;
      }
      
      if (e.key.length === 1 && !e.ctrlKey && !e.metaKey) {
        searchInputRef.current?.focus();
      }
      
      // Quick actions
      if (e.key === 'Enter' && searchInput) {
        handleSearch();
      }
      
      if (e.key === 'Escape') {
        setShowCustomerSearch(false);
        setDiscountModal({ open: false });
      }
    };

    window.addEventListener('keydown', handleKeyPress);
    return () => window.removeEventListener('keydown', handleKeyPress);
  }, [searchInput]);

  // Auto-focus search on mount
  useEffect(() => {
    searchInputRef.current?.focus();
  }, []);

  const handleSearch = () => {
    if (searchInput.trim()) {
      // Try barcode search first
      searchProduct.mutate(searchInput.trim());
    }
  };

  const handleProductSelect = (product: any) => {
    addToCart({
      productId: (product.id || '').toString(),
      productName: product.name || '',
      quantity: 1,
      unitPrice: parseFloat((product.sellingPrice || product.basePrice || 0).toString()),
      taxRate: parseFloat((product.taxRate || 0).toString()),
    });
    setSearchInput('');
    addToast(`Added ${product.name} to cart`, 'success');
    searchInputRef.current?.focus();
  };

  const handleCustomerSelect = (customer: any) => {
    setSelectedCustomer(customer);
    setCustomer({
      name: customer.name,
      phone: customer.phone,
      email: customer.email,
    });
    setShowCustomerSearch(false);
    setCustomerSearch('');
  };

  const handleAddPayment = () => {
    const amount = parseFloat(paymentAmount);
    if (amount > 0 && amount <= getBalanceAmount()) {
      addPayment({
        mode: selectedPaymentMode,
        amount,
        reference: paymentReference || undefined,
      });
      setPaymentAmount('');
      setPaymentReference('');
    } else if (amount > getBalanceAmount()) {
      addToast('Payment amount exceeds balance', 'warning');
    }
  };

  const handleQuickPayment = (mode: 'CASH' | 'CARD' | 'UPI', amount?: number) => {
    const paymentAmt = amount || getBalanceAmount();
    if (paymentAmt > 0) {
      addPayment({
        mode,
        amount: paymentAmt,
      });
      setPaymentAmount('');
    }
  };

  const handleApplyDiscount = () => {
    if (!discountModal.itemId || !discountValue) return;
    
    const item = cart.find(i => i.productId === discountModal.itemId);
    if (item) {
      const updatedItem = {
        ...item,
        discountType,
        discountValue: discountType === 'PERCENTAGE' ? parseFloat(discountValue) : parseFloat(discountValue),
      };
      // @ts-ignore - updateCartItem accepts either number or CartItem
      updateCartItem(item.productId, updatedItem);
      
      setDiscountModal({ open: false });
      setDiscountValue('');
      addToast('Discount applied', 'success');
    }
  };

  const handleCompleteSale = async () => {
    if (cart.length === 0) {
      addToast('Cart is empty', 'warning');
      return;
    }

    const total = getTotal();
    const paid = getPaidAmount();
    
    if (paid < total) {
      // Auto-add remaining as cash payment
      if (total - paid > 0) {
        addPayment({
          mode: 'CASH',
          amount: total - paid,
        });
      }
    }

    setIsProcessing(true);

    // Determine if interstate based on GSTIN
    const supplierState = supplierGstin ? getStateCodeFromGSTIN(supplierGstin) : null;
    const customerState = customerGstin ? getStateCodeFromGSTIN(customerGstin) : null;
    const isInterstate = supplierState && customerState ? supplierState !== customerState : false;

    const invoiceData: InvoiceRequest = {
      customerName: selectedCustomer?.name || customer.name,
      customerPhone: selectedCustomer?.phone || customer.phone,
      customerEmail: selectedCustomer?.email || customer.email,
      storeId: 'main-store', // Default store
      counterId: '1', // Default counter
      items: cart.map((item) => ({
        productId: item.productId,
        productName: item.productName,
        quantity: item.quantity,
        unitPrice: item.unitPrice,
        discountType: item.discountType,
        discountValue: item.discountValue,
        taxRate: item.taxRate,
        // GST fields
        hsnCode: '1234', // Default HSN, should be from product
        cgstRate: isInterstate ? 0 : (item.taxRate || 0) / 2,
        sgstRate: isInterstate ? 0 : (item.taxRate || 0) / 2,
        igstRate: isInterstate ? (item.taxRate || 0) : 0,
      })),
      // GST fields
      customerGstin: customerGstin || undefined,
      supplierGstin: supplierGstin || undefined,
      placeOfSupply: customerState || undefined,
    };

    const paymentsList: PaymentRequest[] = payments.map((p) => ({
      mode: p.mode,
      amount: p.amount,
      reference: p.reference,
    }));

    completeSaleMutation.mutate({ invoiceData, paymentsList });
  };

  const handleHoldBill = () => {
    if (cart.length === 0) {
      addToast('Cart is empty', 'warning');
      return;
    }

    const invoiceData: InvoiceRequest = {
      customerName: selectedCustomer?.name || customer.name,
      customerPhone: selectedCustomer?.phone || customer.phone,
      customerEmail: selectedCustomer?.email || customer.email,
      storeId: 'main-store',
      counterId: '1',
      items: cart.map((item) => ({
        productId: item.productId,
        productName: item.productName,
        quantity: item.quantity,
        unitPrice: item.unitPrice,
        discountType: item.discountType,
        discountValue: item.discountValue,
        taxRate: item.taxRate,
      })),
    };

    holdInvoiceMutation.mutate(invoiceData);
  };

  const handlePrintReceipt = () => {
    window.print();
  };

  return (
    <div className="w-full h-screen flex flex-col bg-gray-50 overflow-hidden">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between shadow-sm">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Point of Sale</h1>
          <p className="text-sm text-gray-600">Quick and efficient billing</p>
        </div>
        <div className="flex items-center gap-4">
          <div className="text-right">
            <p className="text-xs text-gray-500">Store</p>
            <p className="text-sm font-medium">Main Store</p>
          </div>
          <div className="text-right">
            <p className="text-xs text-gray-500">Counter</p>
            <p className="text-sm font-medium">Counter 1</p>
          </div>
          <button
            onClick={() => {
              setShowHoldList(!showHoldList);
              if (!showHoldList) {
                refetchHeldInvoices();
              }
            }}
            className="px-4 py-2 bg-yellow-100 text-yellow-800 rounded-lg hover:bg-yellow-200 text-sm font-medium"
          >
            📋 Held Bills ({heldInvoicesData?.data?.length || 0})
          </button>
        </div>
      </div>

      <div className="flex-1 flex overflow-hidden">
        {/* Left Panel - Product Search & Cart */}
        <div className="flex-1 flex flex-col overflow-hidden">
          {/* Product Search */}
          <div className="bg-white border-b border-gray-200 p-4">
            <div className="relative">
              <input
                ref={searchInputRef}
                type="text"
                placeholder="🔍 Scan barcode or search product (name, SKU, barcode)..."
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    if (filteredProducts.length > 0) {
                      handleProductSelect(filteredProducts[0]);
                    } else {
                      handleSearch();
                    }
                  }
                }}
                className="w-full px-4 py-3 text-lg border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                autoFocus
              />
              
              {/* Product Suggestions */}
              {searchInput && filteredProducts.length > 0 && (
                <div className="absolute z-50 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-xl max-h-64 overflow-y-auto">
                  {filteredProducts.map((product: any) => (
                    <button
                      key={product.id}
                      onClick={() => handleProductSelect(product)}
                      className="w-full px-4 py-3 text-left hover:bg-blue-50 border-b border-gray-100 last:border-0 flex items-center justify-between"
                    >
                      <div>
                        <p className="font-medium text-gray-900">{product.name}</p>
                        <p className="text-sm text-gray-500">
                          SKU: {product.sku} | ₹{parseFloat((product.sellingPrice || product.basePrice || 0).toString()).toFixed(2)}
                        </p>
                      </div>
                      <span className="text-blue-600">+ Add</span>
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Cart */}
          <div className="flex-1 overflow-y-auto bg-white">
            <div className="p-4 border-b border-gray-200 flex items-center justify-between sticky top-0 bg-white z-10">
              <h2 className="text-lg font-semibold text-gray-900">
                Cart ({cart.length} {cart.length === 1 ? 'item' : 'items'})
              </h2>
              {cart.length > 0 && (
                <button
                  onClick={clearCart}
                  className="text-sm text-red-600 hover:text-red-700 font-medium"
                >
                  Clear All
                </button>
              )}
            </div>

            {cart.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-full text-gray-400">
                <div className="text-6xl mb-4">🛒</div>
                <p className="text-lg font-medium">Cart is empty</p>
                <p className="text-sm">Start scanning or searching products</p>
              </div>
            ) : (
              <div className="divide-y divide-gray-200">
                {cart.map((item) => {
                  const lineTotal = item.quantity * item.unitPrice;
                  const itemDiscount = item.discountType === 'PERCENTAGE' && item.discountValue
                    ? (lineTotal * item.discountValue) / 100
                    : (item.discountValue || 0) * item.quantity;
                  const taxableAmount = lineTotal - itemDiscount;
                  const itemTax = (taxableAmount * (item.taxRate || 0)) / 100;
                  const itemTotal = lineTotal - itemDiscount + itemTax;

                  return (
                    <div key={item.productId} className="p-4 hover:bg-gray-50 transition-colors">
                      <div className="flex items-start justify-between gap-4">
                        <div className="flex-1">
                          <div className="flex items-center justify-between mb-2">
                            <h3 className="font-semibold text-gray-900">{item.productName}</h3>
                            <button
                              onClick={() => removeFromCart(item.productId)}
                              className="text-red-600 hover:text-red-700 p-1"
                              title="Remove"
                            >
                              🗑️
                            </button>
                          </div>
                          
                          <div className="flex items-center gap-4 text-sm text-gray-600 mb-2">
                            <span>Price: ₹{item.unitPrice.toFixed(2)}</span>
                            <span>Tax: {item.taxRate || 0}%</span>
                            {item.discountValue && (
                              <span className="text-green-600">
                                Discount: {item.discountType === 'PERCENTAGE' ? `${item.discountValue}%` : `₹${item.discountValue}`}
                              </span>
                            )}
                          </div>

                          <div className="flex items-center gap-3">
                            <div className="flex items-center border border-gray-300 rounded-lg">
                              <button
                                onClick={() => updateCartItem(item.productId, Math.max(1, item.quantity - 1))}
                                className="px-3 py-1 hover:bg-gray-100 font-medium"
                              >
                                −
                              </button>
                              <input
                                type="number"
                                value={item.quantity}
                                onChange={(e) => {
                                  const qty = parseInt(e.target.value) || 1;
                                  updateCartItem(item.productId, Math.max(1, qty));
                                }}
                                className="w-16 text-center border-0 focus:ring-0"
                                min="1"
                              />
                              <button
                                onClick={() => updateCartItem(item.productId, item.quantity + 1)}
                                className="px-3 py-1 hover:bg-gray-100 font-medium"
                              >
                                +
                              </button>
                            </div>
                            
                            <button
                              onClick={() => {
                                setDiscountModal({ open: true, itemId: item.productId });
                                setDiscountType('PERCENTAGE');
                                setDiscountValue('');
                              }}
                              className="px-3 py-1 text-sm bg-yellow-100 text-yellow-800 rounded-lg hover:bg-yellow-200"
                            >
                              💰 Discount
                            </button>
                          </div>
                        </div>

                        <div className="text-right">
                          <p className="text-lg font-bold text-gray-900">₹{itemTotal.toFixed(2)}</p>
                          <p className="text-xs text-gray-500">
                            {item.quantity} × ₹{item.unitPrice.toFixed(2)}
                          </p>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>

        {/* Right Panel - Customer, Payment & Summary */}
        <div className="w-96 bg-white border-l border-gray-200 flex flex-col overflow-hidden">
          {/* Customer Section */}
          <div className="p-4 border-b border-gray-200">
            <div className="flex items-center justify-between mb-3">
              <h2 className="text-lg font-semibold text-gray-900">Customer</h2>
              <button
                onClick={() => {
                  setShowCustomerSearch(!showCustomerSearch);
                  if (!showCustomerSearch) {
                    setCustomerSearch('');
                  }
                }}
                className="text-sm text-blue-600 hover:text-blue-700"
              >
                {selectedCustomer ? 'Change' : 'Search'}
              </button>
            </div>

            {selectedCustomer ? (
              <div className="bg-blue-50 rounded-lg p-3">
                <p className="font-medium text-gray-900">{selectedCustomer.name}</p>
                <p className="text-sm text-gray-600">{selectedCustomer.phone}</p>
                {selectedCustomer.email && (
                  <p className="text-sm text-gray-600">{selectedCustomer.email}</p>
                )}
                <button
                  onClick={() => {
                    setSelectedCustomer(null);
                    setCustomer({});
                  }}
                  className="mt-2 text-xs text-red-600 hover:text-red-700"
                >
                  Remove
                </button>
              </div>
            ) : showCustomerSearch ? (
              <div className="space-y-2">
                <input
                  type="text"
                  placeholder="Search customer by name or phone..."
                  value={customerSearch}
                  onChange={(e) => setCustomerSearch(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm"
                  autoFocus
                />
                {searchingCustomers && <InlineLoader />}
                {customersData?.data?.content && customersData.data.content.length > 0 && (
                  <div className="max-h-48 overflow-y-auto border border-gray-200 rounded-lg">
                    {customersData.data.content.map((customer: any) => (
                      <button
                        key={customer.id}
                        onClick={() => handleCustomerSelect(customer)}
                        className="w-full px-3 py-2 text-left hover:bg-blue-50 border-b border-gray-100 last:border-0"
                      >
                        <p className="font-medium text-sm">{customer.name}</p>
                        <p className="text-xs text-gray-500">{customer.phone}</p>
                      </button>
                    ))}
                  </div>
                )}
              </div>
            ) : (
              <div className="text-sm text-gray-500">
                <p>Walk-in customer</p>
                <button
                  onClick={() => setShowCustomerSearch(true)}
                  className="text-blue-600 hover:text-blue-700 mt-1"
                >
                  + Add customer
                </button>
              </div>
            )}
          </div>

          {/* GST Section */}
          <div className="p-4 border-b border-gray-200">
            <div className="flex items-center justify-between mb-3">
              <h2 className="text-sm font-semibold text-gray-900">GST Details (India)</h2>
              <button
                onClick={() => setShowGstSection(!showGstSection)}
                className="text-xs text-blue-600 hover:text-blue-700"
              >
                {showGstSection ? 'Hide' : 'Show'}
              </button>
            </div>

            {showGstSection && (
              <div className="space-y-3">
                <div>
                  <GSTINInput
                    value={customerGstin}
                    onChange={setCustomerGstin}
                    label="Customer GSTIN"
                    placeholder="Optional - for B2B"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">
                    Your GSTIN
                  </label>
                  <input
                    type="text"
                    value={supplierGstin}
                    onChange={(e) => setSupplierGstin(e.target.value.toUpperCase())}
                    placeholder="Your business GSTIN"
                    maxLength={15}
                    className="w-full px-2 py-1.5 text-sm border border-gray-300 rounded focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>
            )}
          </div>

          {/* Order Summary */}
          <div className="flex-1 overflow-y-auto p-4 space-y-4">
            <div className="bg-gray-50 rounded-lg p-4">
              <h3 className="font-semibold text-gray-900 mb-3">Order Summary</h3>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-600">Subtotal:</span>
                  <span className="font-medium">₹{getSubtotal().toFixed(2)}</span>
                </div>
                {getTotalDiscount() > 0 && (
                  <div className="flex justify-between text-green-600">
                    <span>Discount:</span>
                    <span className="font-medium">-₹{getTotalDiscount().toFixed(2)}</span>
                  </div>
                )}
                <div className="flex justify-between">
                  <span className="text-gray-600">Tax:</span>
                  <span className="font-medium">₹{getTotalTax().toFixed(2)}</span>
                </div>
                <div className="border-t border-gray-300 pt-2 mt-2">
                  <div className="flex justify-between text-lg font-bold">
                    <span>Total:</span>
                    <span className="text-blue-600">₹{getTotal().toFixed(2)}</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Payment Section */}
            <div>
              <h3 className="font-semibold text-gray-900 mb-3">Payment</h3>
              
              {/* Quick Payment Buttons */}
              <div className="grid grid-cols-3 gap-2 mb-3">
                <button
                  onClick={() => handleQuickPayment('CASH')}
                  disabled={getBalanceAmount() <= 0}
                  className="px-3 py-2 bg-green-100 text-green-800 rounded-lg hover:bg-green-200 disabled:opacity-50 text-sm font-medium"
                >
                  💵 Cash
                </button>
                <button
                  onClick={() => handleQuickPayment('CARD')}
                  disabled={getBalanceAmount() <= 0}
                  className="px-3 py-2 bg-blue-100 text-blue-800 rounded-lg hover:bg-blue-200 disabled:opacity-50 text-sm font-medium"
                >
                  💳 Card
                </button>
                <button
                  onClick={() => handleQuickPayment('UPI')}
                  disabled={getBalanceAmount() <= 0}
                  className="px-3 py-2 bg-purple-100 text-purple-800 rounded-lg hover:bg-purple-200 disabled:opacity-50 text-sm font-medium"
                >
                  📱 UPI
                </button>
              </div>

              {/* Manual Payment */}
              <div className="space-y-2">
                <select
                  value={selectedPaymentMode}
                  onChange={(e) => setSelectedPaymentMode(e.target.value as any)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm"
                >
                  <option value="CASH">Cash</option>
                  <option value="CARD">Card</option>
                  <option value="UPI">UPI</option>
                  <option value="WALLET">Wallet</option>
                  <option value="CREDIT">Credit</option>
                </select>

                <div className="flex gap-2">
                  <input
                    type="number"
                    step="0.01"
                    placeholder="Amount"
                    value={paymentAmount}
                    onChange={(e) => setPaymentAmount(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleAddPayment()}
                    className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm"
                  />
                  <button
                    onClick={handleAddPayment}
                    disabled={!paymentAmount || parseFloat(paymentAmount) <= 0}
                    className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 text-sm font-medium"
                  >
                    Add
                  </button>
                </div>

                {selectedPaymentMode !== 'CASH' && (
                  <input
                    type="text"
                    placeholder="Reference/Transaction ID"
                    value={paymentReference}
                    onChange={(e) => setPaymentReference(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm"
                  />
                )}
              </div>

              {/* Payment List */}
              {payments.length > 0 && (
                <div className="mt-3 space-y-1">
                  <p className="text-xs font-medium text-gray-600 mb-2">Payments:</p>
                  {payments.map((payment, index) => (
                    <div key={index} className="flex items-center justify-between bg-gray-50 p-2 rounded text-sm">
                      <span>
                        {payment.mode}: ₹{payment.amount.toFixed(2)}
                      </span>
                      <button
                        onClick={() => removePayment(index)}
                        className="text-red-600 hover:text-red-700"
                      >
                        ×
                      </button>
                    </div>
                  ))}
                  <div className="flex justify-between font-semibold pt-2 border-t border-gray-300">
                    <span>Paid:</span>
                    <span className="text-green-600">₹{getPaidAmount().toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Balance:</span>
                    <span className={getBalanceAmount() > 0 ? 'text-red-600' : 'text-green-600'}>
                      ₹{Math.abs(getBalanceAmount()).toFixed(2)}
                    </span>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Action Buttons */}
          <div className="p-4 border-t border-gray-200 space-y-2 bg-gray-50">
            <button
              onClick={handleCompleteSale}
              disabled={cart.length === 0 || isProcessing || getBalanceAmount() > 0.01}
              className="w-full px-6 py-4 bg-gradient-to-r from-blue-600 to-blue-700 text-white rounded-lg font-bold text-lg hover:from-blue-700 hover:to-blue-800 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-lg flex items-center justify-center gap-2"
            >
              {isProcessing ? (
                <>
                  <ButtonLoader size="sm" />
                  <span>Processing...</span>
                </>
              ) : (
                <>
                  <span>💰</span>
                  <span>Complete Sale (₹{getTotal().toFixed(2)})</span>
                </>
              )}
            </button>
            
            <div className="grid grid-cols-2 gap-2">
              <button
                onClick={handleHoldBill}
                disabled={cart.length === 0 || isProcessing}
                className="px-4 py-3 bg-yellow-600 text-white rounded-lg font-semibold hover:bg-yellow-700 disabled:opacity-50 transition-colors"
              >
                📋 Hold Bill
              </button>
              <button
                onClick={() => {
                  clearCart();
                  clearPayments();
                  setSelectedCustomer(null);
                  setCustomer({});
                }}
                disabled={isProcessing}
                className="px-4 py-3 bg-gray-600 text-white rounded-lg font-semibold hover:bg-gray-700 disabled:opacity-50 transition-colors"
              >
                🔄 New Sale
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Discount Modal */}
      {discountModal.open && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 max-w-md w-full mx-4">
            <h2 className="text-xl font-bold mb-4">Apply Discount</h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Discount Type</label>
                <select
                  value={discountType}
                  onChange={(e) => setDiscountType(e.target.value as any)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                >
                  <option value="PERCENTAGE">Percentage (%)</option>
                  <option value="FLAT">Flat Amount (₹)</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Discount Value</label>
                <input
                  type="number"
                  step="0.01"
                  value={discountValue}
                  onChange={(e) => setDiscountValue(e.target.value)}
                  placeholder={discountType === 'PERCENTAGE' ? 'Enter percentage' : 'Enter amount'}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  autoFocus
                />
              </div>
              <div className="flex gap-3 pt-4">
                <button
                  onClick={() => setDiscountModal({ open: false })}
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  onClick={handleApplyDiscount}
                  className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                >
                  Apply
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Held Invoices Modal */}
      {showHoldList && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-2xl font-bold">Held Bills</h2>
                <button
                  onClick={() => setShowHoldList(false)}
                  className="text-gray-400 hover:text-gray-600 text-2xl"
                >
                  ×
                </button>
              </div>

              {heldInvoicesData?.data && heldInvoicesData.data.length > 0 ? (
                <div className="space-y-3">
                  {heldInvoicesData.data.map((held: HeldInvoiceResponse) => (
                    <div
                      key={held.holdReference}
                      className="border border-gray-200 rounded-lg p-4 hover:bg-gray-50 transition-colors"
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex-1">
                          <div className="flex items-center gap-4 mb-2">
                            <span className="font-semibold text-gray-900">
                              {held.holdReference}
                            </span>
                            <span className="text-sm text-gray-500">
                              {new Date(held.heldAt).toLocaleString()}
                            </span>
                          </div>
                          <div className="text-sm text-gray-600 space-y-1">
                            {held.customerName && (
                              <p>
                                <span className="font-medium">Customer:</span> {held.customerName}
                                {held.customerPhone && ` (${held.customerPhone})`}
                              </p>
                            )}
                            <p>
                              <span className="font-medium">Items:</span> {held.itemCount} |{' '}
                              <span className="font-medium">Total:</span> ₹
                              {parseFloat(held.totalAmount.toString()).toFixed(2)}
                            </p>
                            {held.notes && (
                              <p className="text-gray-500 italic">Note: {held.notes}</p>
                            )}
                          </div>
                        </div>
                        <div className="flex gap-2">
                          <button
                            onClick={() => resumeHeldInvoiceMutation.mutate(held.holdReference)}
                            disabled={resumeHeldInvoiceMutation.isPending}
                            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 text-sm font-medium"
                          >
                            {resumeHeldInvoiceMutation.isPending ? (
                              <InlineLoader />
                            ) : (
                              'Resume'
                            )}
                          </button>
                          <button
                            onClick={() => {
                              if (confirm('Are you sure you want to delete this held invoice?')) {
                                deleteHeldInvoiceMutation.mutate(held.holdReference);
                              }
                            }}
                            disabled={deleteHeldInvoiceMutation.isPending}
                            className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50 text-sm font-medium"
                          >
                            Delete
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-12 text-gray-500">
                  <div className="text-6xl mb-4">📋</div>
                  <p className="text-lg font-medium">No held bills</p>
                  <p className="text-sm">Bills you hold will appear here</p>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Receipt Modal */}
      {showReceipt && completedInvoice && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-2xl font-bold">Receipt</h2>
                <div className="flex gap-2">
                  <button
                    onClick={handlePrintReceipt}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                  >
                    🖨️ Print
                  </button>
                  <button
                    onClick={() => {
                      setShowReceipt(false);
                      setCompletedInvoice(null);
                    }}
                    className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
                  >
                    Close
                  </button>
                </div>
              </div>

              {/* Receipt Content */}
              <div className="space-y-4" id="receipt-content">
                <div className="text-center border-b pb-4">
                  <h3 className="text-xl font-bold">EasyBilling</h3>
                  <p className="text-sm text-gray-600">Main Store</p>
                  <p className="text-xs text-gray-500">Invoice #{completedInvoice.invoiceNumber}</p>
                </div>

                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Date:</span>
                    <span>{new Date(completedInvoice.createdAt).toLocaleString()}</span>
                  </div>
                  {completedInvoice.customerName && (
                    <div className="flex justify-between">
                      <span className="text-gray-600">Customer:</span>
                      <span>{completedInvoice.customerName}</span>
                    </div>
                  )}
                </div>

                <div className="border-t border-b py-4">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b">
                        <th className="text-left py-2">Item</th>
                        <th className="text-center py-2">Qty</th>
                        <th className="text-right py-2">Price</th>
                        <th className="text-right py-2">Total</th>
                      </tr>
                    </thead>
                    <tbody>
                      {completedInvoice.items.map((item: any, index: number) => (
                        <tr key={index} className="border-b">
                          <td className="py-2">{item.productName}</td>
                          <td className="text-center py-2">{item.quantity}</td>
                          <td className="text-right py-2">₹{item.unitPrice.toFixed(2)}</td>
                          <td className="text-right py-2">₹{item.lineTotal.toFixed(2)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span>Subtotal:</span>
                    <span>{formatIndianCurrency(completedInvoice.subtotal)}</span>
                  </div>
                  {completedInvoice.discountAmount > 0 && (
                    <div className="flex justify-between text-green-600">
                      <span>Discount:</span>
                      <span>-{formatIndianCurrency(completedInvoice.discountAmount)}</span>
                    </div>
                  )}
                  <div className="flex justify-between">
                    <span>Tax:</span>
                    <span>{formatIndianCurrency(completedInvoice.taxAmount)}</span>
                  </div>
                  <div className="flex justify-between font-bold text-lg border-t pt-2">
                    <span>Total:</span>
                    <span>{formatIndianCurrency(completedInvoice.totalAmount)}</span>
                  </div>
                  
                  {/* Amount in words */}
                  {completedInvoice.totalAmount > 0 && (
                    <div className="text-xs text-gray-600 border-t pt-2 italic">
                      Amount in words: {convertCurrencyToWords(completedInvoice.totalAmount)} Only
                    </div>
                  )}
                </div>

                {/* GST Breakdown if available */}
                {(completedInvoice.totalCgst > 0 || completedInvoice.totalSgst > 0 || completedInvoice.totalIgst > 0) && (
                  <div className="border-t pt-4">
                    <GSTBreakdown
                      taxableAmount={completedInvoice.subtotal - (completedInvoice.discountAmount || 0)}
                      cgst={completedInvoice.totalCgst || 0}
                      sgst={completedInvoice.totalSgst || 0}
                      igst={completedInvoice.totalIgst || 0}
                      cess={completedInvoice.totalCess || 0}
                      isInterstate={completedInvoice.isInterstate || false}
                      showRates={false}
                    />
                  </div>
                )}

                {completedInvoice.payments && completedInvoice.payments.length > 0 && (
                  <div className="border-t pt-4">
                    <p className="font-medium mb-2">Payments:</p>
                    {completedInvoice.payments.map((payment: any, index: number) => (
                      <div key={index} className="flex justify-between text-sm">
                        <span>{payment.mode}:</span>
                        <span>₹{payment.amount.toFixed(2)}</span>
                      </div>
                    ))}
                  </div>
                )}

                <div className="text-center text-xs text-gray-500 pt-4 border-t">
                  <p>Thank you for your business!</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
