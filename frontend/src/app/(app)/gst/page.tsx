'use client';

import React, { useState } from 'react';
import GSTCalculator from '@/components/gst/GSTCalculator';
import GSTRatesList from '@/components/gst/GSTRatesList';

/**
 * GST Management Page
 * Includes calculator and rates list
 */
export default function GSTPage() {
  const [activeTab, setActiveTab] = useState<'calculator' | 'rates'>('calculator');

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">GST Management</h1>
        <p className="text-gray-600">Calculate GST and manage tax rates for India</p>
      </div>

      {/* Tab Navigation */}
      <div className="border-b border-gray-200 mb-6">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab('calculator')}
            className={`
              py-4 px-1 border-b-2 font-medium text-sm
              ${activeTab === 'calculator'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }
            `}
          >
            GST Calculator
          </button>
          <button
            onClick={() => setActiveTab('rates')}
            className={`
              py-4 px-1 border-b-2 font-medium text-sm
              ${activeTab === 'rates'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }
            `}
          >
            GST Rates
          </button>
        </nav>
      </div>

      {/* Tab Content */}
      <div>
        {activeTab === 'calculator' && <GSTCalculator />}
        {activeTab === 'rates' && <GSTRatesList />}
      </div>
    </div>
  );
}
