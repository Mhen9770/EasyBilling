import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface CartItem {
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  taxRate: number;
  discountType?: 'PERCENTAGE' | 'FLAT';
  discountValue?: number;
}

export interface CustomerInfo {
  name?: string;
  phone?: string;
  email?: string;
}

export interface PaymentInfo {
  mode: 'CASH' | 'CARD' | 'UPI' | 'WALLET' | 'CREDIT' | 'BANK_TRANSFER';
  amount: number;
  reference?: string;
}

interface POSState {
  cart: CartItem[];
  customer: CustomerInfo;
  payments: PaymentInfo[];
  
  // Cart actions
  addToCart: (item: CartItem) => void;
  updateCartItem: (productId: string, quantity: number) => void;
  removeFromCart: (productId: string) => void;
  clearCart: () => void;
  
  // Customer actions
  setCustomer: (customer: CustomerInfo) => void;
  clearCustomer: () => void;
  
  // Payment actions
  addPayment: (payment: PaymentInfo) => void;
  removePayment: (index: number) => void;
  clearPayments: () => void;
  
  // Calculations
  getSubtotal: () => number;
  getTotalTax: () => number;
  getTotalDiscount: () => number;
  getTotal: () => number;
  getPaidAmount: () => number;
  getBalanceAmount: () => number;
}

export const usePOSStore = create<POSState>()(
  persist(
    (set, get) => ({
      cart: [],
      customer: {},
      payments: [],

      addToCart: (item) => {
        const cart = get().cart;
        const existingIndex = cart.findIndex((i) => i.productId === item.productId);
        
        if (existingIndex >= 0) {
          // Update existing item
          const newCart = [...cart];
          newCart[existingIndex].quantity += item.quantity;
          set({ cart: newCart });
        } else {
          // Add new item
          set({ cart: [...cart, item] });
        }
      },

      updateCartItem: (productId, itemOrQuantity) => {
        const cart = get().cart;
        if (typeof itemOrQuantity === 'number') {
          // Legacy: quantity only
          const quantity = itemOrQuantity;
          if (quantity <= 0) {
            set({ cart: cart.filter((item) => item.productId !== productId) });
          } else {
            set({
              cart: cart.map((item) =>
                item.productId === productId ? { ...item, quantity } : item
              ),
            });
          }
        } else {
          // New: full item update
          set({
            cart: cart.map((item) =>
              item.productId === productId ? itemOrQuantity : item
            ),
          });
        }
      },

      removeFromCart: (productId) => {
        set({ cart: get().cart.filter((item) => item.productId !== productId) });
      },

      clearCart: () => {
        set({ cart: [], customer: {}, payments: [] });
      },

      setCustomer: (customer) => {
        set({ customer });
      },

      clearCustomer: () => {
        set({ customer: {} });
      },

      addPayment: (payment) => {
        set({ payments: [...get().payments, payment] });
      },

      removePayment: (index) => {
        const payments = get().payments;
        set({ payments: payments.filter((_, i) => i !== index) });
      },

      clearPayments: () => {
        set({ payments: [] });
      },

      getSubtotal: () => {
        const cart = get().cart;
        return cart.reduce((total, item) => {
          return total + item.quantity * item.unitPrice;
        }, 0);
      },

      getTotalDiscount: () => {
        const cart = get().cart;
        return cart.reduce((total, item) => {
          let discount = 0;
          if (item.discountType === 'PERCENTAGE' && item.discountValue) {
            discount = (item.quantity * item.unitPrice * item.discountValue) / 100;
          } else if (item.discountType === 'FLAT' && item.discountValue) {
            discount = item.discountValue * item.quantity;
          }
          return total + discount;
        }, 0);
      },

      getTotalTax: () => {
        const cart = get().cart;
        const discount = get().getTotalDiscount();
        return cart.reduce((total, item) => {
          const lineTotal = item.quantity * item.unitPrice;
          const itemDiscount = item.discountType === 'PERCENTAGE' && item.discountValue
            ? (lineTotal * item.discountValue) / 100
            : (item.discountValue || 0) * item.quantity;
          const taxableAmount = lineTotal - itemDiscount;
          return total + (taxableAmount * item.taxRate) / 100;
        }, 0);
      },

      getTotal: () => {
        const subtotal = get().getSubtotal();
        const discount = get().getTotalDiscount();
        const tax = get().getTotalTax();
        return subtotal - discount + tax;
      },

      getPaidAmount: () => {
        const payments = get().payments;
        return payments.reduce((total, payment) => total + payment.amount, 0);
      },

      getBalanceAmount: () => {
        return get().getTotal() - get().getPaidAmount();
      },
    }),
    {
      name: 'pos-storage',
    }
  )
);
