'use client';

import { useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { usePOSStore } from '@/lib/store/posStore';
import { inventoryApi } from '@/lib/api/inventory/inventoryApi';
import { billingApi, InvoiceRequest } from '@/lib/api/billing/billingApi';
import { useToastStore } from '@/components/ui/toast';

export default function POSPage() {
  const [searchInput, setSearchInput] = useState('');
  const [selectedPaymentMode, setSelectedPaymentMode] = useState<'CASH' | 'CARD' | 'UPI' | 'WALLET'>('CASH');
  const [paymentAmount, setPaymentAmount] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);
  const { addToast } = useToastStore();

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
    clearPayments,
    getSubtotal,
    getTotalTax,
    getTotalDiscount,
    getTotal,
    getPaidAmount,
    getBalanceAmount,
  } = usePOSStore();

  // Search product mutation
  const searchProduct = useMutation({
    mutationFn: (barcode: string) => inventoryApi.findByBarcode(barcode),
    onSuccess: (response) => {
      if (response.data) {
        const product = response.data;
        addToCart({
          productId: product.id,
          productName: product.name,
          quantity: 1,
          unitPrice: product.sellingPrice,
          taxRate: product.taxRate,
        });
        setSearchInput('');
        addToast(`Added ${product.name} to cart`, 'success');
      }
    },
    onError: () => {
      addToast('Product not found', 'error');
      setSearchInput('');
    },
  });

  // Create invoice mutation
  const createInvoice = useMutation({
    mutationFn: (data: InvoiceRequest) => billingApi.createInvoice(data),
  });

  // Complete invoice mutation
  const completeInvoice = useMutation({
    mutationFn: ({ id, payments }: { id: string; payments: any[] }) =>
      billingApi.completeInvoice(id, payments),
    onSuccess: () => {
      addToast('Sale completed successfully!', 'success');
      clearCart();
      clearPayments();
      setPaymentAmount('');
      setIsProcessing(false);
    },
    onError: () => {
      addToast('Failed to complete sale', 'error');
      setIsProcessing(false);
    },
  });

  // Hold invoice mutation
  const holdInvoice = useMutation({
    mutationFn: (data: InvoiceRequest) => billingApi.holdInvoice(data),
    onSuccess: () => {
      addToast('Bill held successfully!', 'success');
      clearCart();
    },
    onError: () => {
      addToast('Failed to hold bill', 'error');
    },
  });

  const handleSearch = () => {
    if (searchInput.trim()) {
      searchProduct.mutate(searchInput.trim());
    }
  };

  const handleAddPayment = () => {
    const amount = parseFloat(paymentAmount);
    if (amount > 0) {
      addPayment({
        mode: selectedPaymentMode,
        amount,
      });
      setPaymentAmount('');
    }
  };

  const handleCompleteSale = async () => {
    if (cart.length === 0) {
      addToast('Cart is empty', 'warning');
      return;
    }

    if (getPaidAmount() < getTotal()) {
      addToast('Payment amount is less than total', 'warning');
      return;
    }

    setIsProcessing(true);

    try {
      // First create the invoice
      const invoiceData: InvoiceRequest = {
        customerName: customer.name,
        customerPhone: customer.phone,
        customerEmail: customer.email,
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

      const invoiceResponse = await createInvoice.mutateAsync(invoiceData);
      
      if (invoiceResponse.data) {
        // Then complete it with payments
        const paymentsList = payments.map((p) => ({
          mode: p.mode,
          amount: p.amount,
          reference: p.reference,
        }));

        await completeInvoice.mutateAsync({
          id: invoiceResponse.data.id,
          payments: paymentsList,
        });
      }
    } catch (error) {
      setIsProcessing(false);
      addToast('Failed to complete sale', 'error');
    }
  };

  const handleHoldBill = () => {
    if (cart.length === 0) {
      addToast('Cart is empty', 'warning');
      return;
    }

    const invoiceData: InvoiceRequest = {
      customerName: customer.name,
      customerPhone: customer.phone,
      customerEmail: customer.email,
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

    holdInvoice.mutate(invoiceData);
  };

  return (
    <div className="max-w-7xl mx-auto">
      <h1 className="text-3xl font-bold mb-6">Point of Sale</h1>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left: Product Search and Cart */}
          <div className="lg:col-span-2 space-y-4">
            {/* Search */}
            <div className="bg-white p-4 rounded-lg shadow">
              <h2 className="text-lg font-semibold mb-3">Product Search</h2>
              <div className="flex gap-2">
                <input
                  type="text"
                  placeholder="Scan barcode or search product..."
                  value={searchInput}
                  onChange={(e) => setSearchInput(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                  className="flex-1 px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  autoFocus
                />
                <button
                  onClick={handleSearch}
                  disabled={searchProduct.isPending}
                  className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
                >
                  {searchProduct.isPending ? 'Searching...' : 'Search'}
                </button>
              </div>
            </div>

            {/* Cart */}
            <div className="bg-white p-4 rounded-lg shadow">
              <div className="flex justify-between items-center mb-3">
                <h2 className="text-lg font-semibold">Shopping Cart</h2>
                <button
                  onClick={clearCart}
                  className="text-red-600 hover:text-red-700 text-sm"
                >
                  Clear Cart
                </button>
              </div>

              {cart.length === 0 ? (
                <p className="text-gray-500 text-center py-8">Cart is empty</p>
              ) : (
                <div className="space-y-2">
                  {cart.map((item) => (
                    <div
                      key={item.productId}
                      className="flex items-center justify-between p-3 bg-gray-50 rounded"
                    >
                      <div className="flex-1">
                        <p className="font-medium">{item.productName}</p>
                        <p className="text-sm text-gray-600">
                          ${item.unitPrice.toFixed(2)} × {item.quantity}
                        </p>
                      </div>
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => updateCartItem(item.productId, item.quantity - 1)}
                          className="px-2 py-1 bg-gray-200 rounded hover:bg-gray-300"
                        >
                          −
                        </button>
                        <span className="w-8 text-center">{item.quantity}</span>
                        <button
                          onClick={() => updateCartItem(item.productId, item.quantity + 1)}
                          className="px-2 py-1 bg-gray-200 rounded hover:bg-gray-300"
                        >
                          +
                        </button>
                        <button
                          onClick={() => removeFromCart(item.productId)}
                          className="ml-2 px-2 py-1 bg-red-500 text-white rounded hover:bg-red-600"
                        >
                          Remove
                        </button>
                      </div>
                      <div className="ml-4 text-right">
                        <p className="font-semibold">
                          ${(item.quantity * item.unitPrice).toFixed(2)}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Customer Info */}
            <div className="bg-white p-4 rounded-lg shadow">
              <h2 className="text-lg font-semibold mb-3">Customer Information (Optional)</h2>
              <div className="grid grid-cols-3 gap-3">
                <input
                  type="text"
                  placeholder="Name"
                  value={customer.name || ''}
                  onChange={(e) => setCustomer({ ...customer, name: e.target.value })}
                  className="px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <input
                  type="tel"
                  placeholder="Phone"
                  value={customer.phone || ''}
                  onChange={(e) => setCustomer({ ...customer, phone: e.target.value })}
                  className="px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <input
                  type="email"
                  placeholder="Email"
                  value={customer.email || ''}
                  onChange={(e) => setCustomer({ ...customer, email: e.target.value })}
                  className="px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>
          </div>

          {/* Right: Payment and Total */}
          <div className="space-y-4">
            {/* Total Summary */}
            <div className="bg-white p-4 rounded-lg shadow">
              <h2 className="text-lg font-semibold mb-3">Order Summary</h2>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span>Subtotal:</span>
                  <span>${getSubtotal().toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-green-600">
                  <span>Discount:</span>
                  <span>-${getTotalDiscount().toFixed(2)}</span>
                </div>
                <div className="flex justify-between">
                  <span>Tax:</span>
                  <span>${getTotalTax().toFixed(2)}</span>
                </div>
                <div className="border-t pt-2 mt-2 flex justify-between text-lg font-bold">
                  <span>Total:</span>
                  <span>${getTotal().toFixed(2)}</span>
                </div>
              </div>
            </div>

            {/* Payment */}
            <div className="bg-white p-4 rounded-lg shadow">
              <h2 className="text-lg font-semibold mb-3">Payment</h2>
              
              <div className="space-y-3">
                <div>
                  <label className="block text-sm font-medium mb-1">Payment Mode</label>
                  <select
                    value={selectedPaymentMode}
                    onChange={(e) => setSelectedPaymentMode(e.target.value as any)}
                    className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="CASH">Cash</option>
                    <option value="CARD">Card</option>
                    <option value="UPI">UPI</option>
                    <option value="WALLET">Wallet</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium mb-1">Amount</label>
                  <div className="flex gap-2">
                    <input
                      type="number"
                      step="0.01"
                      placeholder="0.00"
                      value={paymentAmount}
                      onChange={(e) => setPaymentAmount(e.target.value)}
                      className="flex-1 px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <button
                      onClick={handleAddPayment}
                      className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
                    >
                      Add
                    </button>
                  </div>
                </div>

                {payments.length > 0 && (
                  <div className="space-y-1">
                    <p className="text-sm font-medium">Payments Added:</p>
                    {payments.map((payment, index) => (
                      <div key={index} className="flex justify-between text-sm bg-gray-50 p-2 rounded">
                        <span>{payment.mode}: ${payment.amount.toFixed(2)}</span>
                      </div>
                    ))}
                    <div className="flex justify-between font-semibold pt-2 border-t">
                      <span>Paid:</span>
                      <span>${getPaidAmount().toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Balance:</span>
                      <span>${getBalanceAmount().toFixed(2)}</span>
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* Actions */}
            <div className="space-y-2">
              <button
                onClick={handleCompleteSale}
                disabled={cart.length === 0 || isProcessing || getPaidAmount() < getTotal()}
                className="w-full px-6 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 disabled:bg-gray-400"
              >
                {isProcessing ? 'Processing...' : 'Complete Sale'}
              </button>
              <button
                onClick={handleHoldBill}
                disabled={cart.length === 0}
                className="w-full px-6 py-3 bg-yellow-600 text-white rounded-lg font-semibold hover:bg-yellow-700 disabled:bg-gray-400"
              >
                Hold Bill
              </button>
            </div>
          </div>
        </div>
    </div>
  );
}
