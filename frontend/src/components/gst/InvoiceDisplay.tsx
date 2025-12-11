'use client';

import React from 'react';
import { formatIndianCurrency, convertCurrencyToWords, getCurrentFinancialYear } from '@/lib/utils/indianFormatter';
import { GSTBreakdown } from './GSTBreakdown';

interface InvoiceItem {
  id: string;
  productName: string;
  hsnCode?: string;
  quantity: number;
  unitPrice: number;
  discountAmount: number;
  cgstAmount: number;
  sgstAmount: number;
  igstAmount: number;
  cessAmount: number;
  lineTotal: number;
}

interface Invoice {
  invoiceNumber: string;
  invoiceDate: string;
  supplierGstin: string;
  customerGstin?: string;
  customerName: string;
  customerAddress: string;
  placeOfSupply: string;
  items: InvoiceItem[];
  subtotal: number;
  totalCgst: number;
  totalSgst: number;
  totalIgst: number;
  totalCess: number;
  totalAmount: number;
  isInterstate: boolean;
}

interface InvoiceDisplayProps {
  invoice: Invoice;
}

/**
 * Invoice Display Component with GST
 * Shows a formatted invoice with Indian GST details
 */
export const InvoiceDisplay: React.FC<InvoiceDisplayProps> = ({ invoice }) => {
  const totalTax = invoice.totalCgst + invoice.totalSgst + invoice.totalIgst + invoice.totalCess;

  return (
    <div className="max-w-4xl mx-auto bg-white shadow-lg">
      {/* Header */}
      <div className="bg-blue-600 text-white p-6">
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold mb-2">TAX INVOICE</h1>
            <p className="text-blue-100">As per GST compliance</p>
          </div>
          <div className="text-right">
            <div className="text-2xl font-bold">{invoice.invoiceNumber}</div>
            <div className="text-sm text-blue-100">Date: {invoice.invoiceDate}</div>
          </div>
        </div>
      </div>

      {/* Supplier & Customer Details */}
      <div className="grid grid-cols-2 gap-6 p-6 border-b">
        <div>
          <h3 className="text-sm font-semibold text-gray-500 uppercase mb-2">Supplier Details</h3>
          <div className="text-sm">
            <p className="font-medium text-gray-900">Your Company Name</p>
            <p className="text-gray-600 mt-1">123, Business Street</p>
            <p className="text-gray-600">City, State - 400001</p>
            <p className="text-gray-600 mt-2">
              <span className="font-medium">GSTIN:</span> {invoice.supplierGstin}
            </p>
          </div>
        </div>
        <div>
          <h3 className="text-sm font-semibold text-gray-500 uppercase mb-2">Bill To</h3>
          <div className="text-sm">
            <p className="font-medium text-gray-900">{invoice.customerName}</p>
            <p className="text-gray-600 mt-1">{invoice.customerAddress}</p>
            {invoice.customerGstin && (
              <p className="text-gray-600 mt-2">
                <span className="font-medium">GSTIN:</span> {invoice.customerGstin}
              </p>
            )}
            <p className="text-gray-600 mt-1">
              <span className="font-medium">Place of Supply:</span> {invoice.placeOfSupply}
            </p>
          </div>
        </div>
      </div>

      {/* Items Table */}
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead className="bg-gray-50 border-b">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">#</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Item</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">HSN</th>
              <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Qty</th>
              <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Price</th>
              <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Disc.</th>
              <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Tax</th>
              <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Total</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {invoice.items.map((item, index) => {
              const itemTax = item.cgstAmount + item.sgstAmount + item.igstAmount + item.cessAmount;
              return (
                <tr key={item.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 text-sm text-gray-900">{index + 1}</td>
                  <td className="px-4 py-3 text-sm text-gray-900">{item.productName}</td>
                  <td className="px-4 py-3 text-sm text-gray-600">{item.hsnCode || '-'}</td>
                  <td className="px-4 py-3 text-sm text-gray-900 text-right">{item.quantity}</td>
                  <td className="px-4 py-3 text-sm text-gray-900 text-right">
                    {formatIndianCurrency(item.unitPrice)}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-900 text-right">
                    {formatIndianCurrency(item.discountAmount)}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-900 text-right">
                    {formatIndianCurrency(itemTax)}
                  </td>
                  <td className="px-4 py-3 text-sm font-medium text-gray-900 text-right">
                    {formatIndianCurrency(item.lineTotal)}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Totals Section */}
      <div className="grid grid-cols-2 gap-6 p-6 border-t">
        {/* GST Breakdown */}
        <div className="col-span-1">
          <GSTBreakdown
            taxableAmount={invoice.subtotal}
            cgst={invoice.totalCgst}
            sgst={invoice.totalSgst}
            igst={invoice.totalIgst}
            cess={invoice.totalCess}
            isInterstate={invoice.isInterstate}
            showRates={false}
          />
        </div>

        {/* Summary */}
        <div className="col-span-1">
          <div className="bg-gray-50 rounded-lg p-4 border border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Summary</h3>
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Subtotal:</span>
                <span className="font-medium">{formatIndianCurrency(invoice.subtotal)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Total Tax:</span>
                <span className="font-medium">{formatIndianCurrency(totalTax)}</span>
              </div>
              <div className="border-t border-gray-300 my-2"></div>
              <div className="flex justify-between text-lg font-bold">
                <span>Total Amount:</span>
                <span className="text-blue-700">{formatIndianCurrency(invoice.totalAmount)}</span>
              </div>
            </div>
          </div>

          {/* Amount in Words */}
          <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded">
            <p className="text-xs text-gray-600 font-medium mb-1">Amount in Words:</p>
            <p className="text-sm text-gray-900 font-medium">
              {convertCurrencyToWords(invoice.totalAmount)} Only
            </p>
          </div>
        </div>
      </div>

      {/* Footer */}
      <div className="p-6 bg-gray-50 border-t">
        <div className="grid grid-cols-2 gap-6">
          <div>
            <h4 className="text-sm font-semibold text-gray-700 mb-2">Terms & Conditions:</h4>
            <ul className="text-xs text-gray-600 space-y-1">
              <li>• Payment due within 30 days</li>
              <li>• Interest @18% p.a. on delayed payments</li>
              <li>• Goods once sold will not be taken back</li>
            </ul>
          </div>
          <div className="text-right">
            <p className="text-sm font-semibold text-gray-700 mb-4">For Your Company Name</p>
            <div className="h-16 border-b border-gray-400 mb-2"></div>
            <p className="text-xs text-gray-600">Authorized Signatory</p>
          </div>
        </div>
      </div>

      {/* Print Friendly Note */}
      <div className="p-4 bg-yellow-50 border-t border-yellow-200 text-center text-xs text-gray-600">
        This is a computer-generated invoice and does not require a signature.
      </div>
    </div>
  );
};

// Demo data for testing
export const demoInvoice: Invoice = {
  invoiceNumber: `INV/${getCurrentFinancialYear()}/0001`,
  invoiceDate: new Date().toLocaleDateString('en-IN'),
  supplierGstin: '27AABCU9603R1ZX',
  customerGstin: '29AABCU9603R1ZY',
  customerName: 'ABC Corporation Pvt Ltd',
  customerAddress: '456, Corporate Tower, Bangalore, Karnataka - 560001',
  placeOfSupply: 'Karnataka',
  items: [
    {
      id: '1',
      productName: 'Product A',
      hsnCode: '1234',
      quantity: 10,
      unitPrice: 1000,
      discountAmount: 500,
      cgstAmount: 0,
      sgstAmount: 0,
      igstAmount: 1710, // 18% of (10000 - 500)
      cessAmount: 0,
      lineTotal: 11210,
    },
    {
      id: '2',
      productName: 'Product B',
      hsnCode: '5678',
      quantity: 5,
      unitPrice: 2000,
      discountAmount: 0,
      cgstAmount: 0,
      sgstAmount: 0,
      igstAmount: 1800, // 18% of 10000
      cessAmount: 0,
      lineTotal: 11800,
    },
  ],
  subtotal: 19500,
  totalCgst: 0,
  totalSgst: 0,
  totalIgst: 3510,
  totalCess: 0,
  totalAmount: 23010,
  isInterstate: true,
};

export default InvoiceDisplay;
