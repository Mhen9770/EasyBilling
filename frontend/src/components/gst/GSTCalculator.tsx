'use client';

import React, { useState } from 'react';
import { gstApi, GstCalculation } from '@/lib/api';
import { formatIndianCurrency, formatIndianNumber } from '@/lib/utils/indianFormatter';
import { GSTBreakdown } from './GSTBreakdown';

/**
 * GST Calculator Component
 * Allows users to calculate GST based on amount, HSN/SAC code, and states
 */
export const GSTCalculator: React.FC = () => {
  const [hsnOrSacCode, setHsnOrSacCode] = useState('');
  const [amount, setAmount] = useState('');
  const [supplierState, setSupplierState] = useState('');
  const [customerState, setCustomerState] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState<GstCalculation | null>(null);

  const handleCalculate = async () => {
    if (!hsnOrSacCode || !amount || !supplierState || !customerState) {
      setError('Please fill all fields');
      return;
    }

    setLoading(true);
    setError('');
    setResult(null);

    try {
      const response = await gstApi.calculateGst({
        hsnOrSacCode,
        amount: parseFloat(amount),
        supplierState,
        customerState,
      });

      if (response.success && response.data) {
        setResult(response.data);
      } else {
        setError(response.message || 'Failed to calculate GST');
      }
    } catch (err: any) {
      setError(err.message || 'An error occurred while calculating GST');
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setHsnOrSacCode('');
    setAmount('');
    setSupplierState('');
    setCustomerState('');
    setResult(null);
    setError('');
  };

  return (
    <div className="max-w-4xl mx-auto p-6">
      <div className="bg-white rounded-lg shadow-lg p-6">
        <h2 className="text-2xl font-bold text-gray-900 mb-6">GST Calculator</h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
          {/* HSN/SAC Code */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              HSN/SAC Code <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={hsnOrSacCode}
              onChange={(e) => setHsnOrSacCode(e.target.value)}
              placeholder="e.g., 1234"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <p className="mt-1 text-xs text-gray-500">HSN for goods, SAC for services</p>
          </div>

          {/* Amount */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Amount <span className="text-red-500">*</span>
            </label>
            <input
              type="number"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder="e.g., 10000"
              min="0"
              step="0.01"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            {amount && (
              <p className="mt-1 text-xs text-gray-600">
                {formatIndianCurrency(parseFloat(amount) || 0)}
              </p>
            )}
          </div>

          {/* Supplier State */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Supplier State Code <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={supplierState}
              onChange={(e) => setSupplierState(e.target.value.toUpperCase())}
              placeholder="e.g., 27 (Maharashtra)"
              maxLength={2}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <p className="mt-1 text-xs text-gray-500">2-digit state code</p>
          </div>

          {/* Customer State */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Customer State Code <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={customerState}
              onChange={(e) => setCustomerState(e.target.value.toUpperCase())}
              placeholder="e.g., 29 (Karnataka)"
              maxLength={2}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <p className="mt-1 text-xs text-gray-500">2-digit state code</p>
          </div>
        </div>

        {/* Error Display */}
        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-md text-red-700 text-sm">
            {error}
          </div>
        )}

        {/* Buttons */}
        <div className="flex gap-3 mb-6">
          <button
            onClick={handleCalculate}
            disabled={loading}
            className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed font-medium"
          >
            {loading ? 'Calculating...' : 'Calculate GST'}
          </button>
          <button
            onClick={handleReset}
            className="px-6 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 font-medium"
          >
            Reset
          </button>
        </div>

        {/* Result Display */}
        {result && (
          <div className="mt-6">
            <GSTBreakdown
              taxableAmount={result.taxableAmount}
              cgst={result.cgst}
              sgst={result.sgst}
              igst={result.igst}
              cess={result.cess}
              isInterstate={result.isInterstate}
              cgstRate={result.cgstRate}
              sgstRate={result.sgstRate}
              igstRate={result.igstRate}
              cessRate={result.cessRate}
              showRates={true}
            />
          </div>
        )}
      </div>
    </div>
  );
};

export default GSTCalculator;
