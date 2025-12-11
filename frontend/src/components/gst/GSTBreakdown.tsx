'use client';

import React from 'react';
import { formatIndianCurrency, formatIndianNumber } from '@/lib/utils/indianFormatter';

interface GSTBreakdownProps {
  taxableAmount: number;
  cgst: number;
  sgst: number;
  igst: number;
  cess?: number;
  isInterstate: boolean;
  cgstRate?: number;
  sgstRate?: number;
  igstRate?: number;
  cessRate?: number;
  showRates?: boolean;
}

/**
 * Component to display GST tax breakdown
 * Shows CGST+SGST for intra-state, IGST for inter-state
 */
export const GSTBreakdown: React.FC<GSTBreakdownProps> = ({
  taxableAmount,
  cgst,
  sgst,
  igst,
  cess = 0,
  isInterstate,
  cgstRate,
  sgstRate,
  igstRate,
  cessRate,
  showRates = true,
}) => {
  const totalTax = cgst + sgst + igst + cess;
  const totalAmount = taxableAmount + totalTax;

  return (
    <div className="bg-gray-50 rounded-lg p-4 border border-gray-200">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">GST Breakdown</h3>
      
      <div className="space-y-2">
        {/* Taxable Amount */}
        <div className="flex justify-between text-sm">
          <span className="text-gray-600">Taxable Amount:</span>
          <span className="font-medium">{formatIndianCurrency(taxableAmount)}</span>
        </div>

        {/* Transaction Type */}
        <div className="flex justify-between text-sm">
          <span className="text-gray-600">Transaction Type:</span>
          <span className={`font-medium ${isInterstate ? 'text-blue-600' : 'text-green-600'}`}>
            {isInterstate ? 'Inter-State' : 'Intra-State'}
          </span>
        </div>

        <div className="border-t border-gray-300 my-2"></div>

        {/* Tax Components */}
        {!isInterstate ? (
          <>
            {/* CGST */}
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">
                CGST {showRates && cgstRate ? `(${formatIndianNumber(cgstRate, 2)}%)` : ''}:
              </span>
              <span className="font-medium text-green-700">{formatIndianCurrency(cgst)}</span>
            </div>
            
            {/* SGST */}
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">
                SGST {showRates && sgstRate ? `(${formatIndianNumber(sgstRate, 2)}%)` : ''}:
              </span>
              <span className="font-medium text-green-700">{formatIndianCurrency(sgst)}</span>
            </div>
          </>
        ) : (
          /* IGST */
          <div className="flex justify-between text-sm">
            <span className="text-gray-600">
              IGST {showRates && igstRate ? `(${formatIndianNumber(igstRate, 2)}%)` : ''}:
            </span>
            <span className="font-medium text-blue-700">{formatIndianCurrency(igst)}</span>
          </div>
        )}

        {/* CESS (if applicable) */}
        {cess > 0 && (
          <div className="flex justify-between text-sm">
            <span className="text-gray-600">
              CESS {showRates && cessRate ? `(${formatIndianNumber(cessRate, 2)}%)` : ''}:
            </span>
            <span className="font-medium text-orange-700">{formatIndianCurrency(cess)}</span>
          </div>
        )}

        <div className="border-t border-gray-300 my-2"></div>

        {/* Total Tax */}
        <div className="flex justify-between text-sm font-semibold">
          <span className="text-gray-700">Total Tax:</span>
          <span className="text-gray-900">{formatIndianCurrency(totalTax)}</span>
        </div>

        {/* Total Amount */}
        <div className="flex justify-between text-base font-bold bg-blue-50 p-2 rounded">
          <span className="text-gray-900">Total Amount:</span>
          <span className="text-blue-700">{formatIndianCurrency(totalAmount)}</span>
        </div>
      </div>

      {/* Info Note */}
      <div className="mt-4 text-xs text-gray-500 bg-white p-2 rounded border border-gray-200">
        <div className="flex items-start">
          <svg className="w-4 h-4 mr-2 mt-0.5 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
          </svg>
          <div>
            {isInterstate ? (
              <span>Inter-state transaction: IGST is charged when supplier and customer are in different states.</span>
            ) : (
              <span>Intra-state transaction: CGST + SGST is charged when both parties are in the same state.</span>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default GSTBreakdown;
