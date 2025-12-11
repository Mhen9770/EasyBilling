'use client';

import React, { useState } from 'react';
import { validateGSTIN, getStateCodeFromGSTIN, getStateName } from '@/lib/utils/indianFormatter';

interface GSTINInputProps {
  value: string;
  onChange: (value: string) => void;
  label?: string;
  placeholder?: string;
  required?: boolean;
  error?: string;
  disabled?: boolean;
}

/**
 * Input component for GSTIN with real-time validation
 * Validates format and extracts state information
 */
export const GSTINInput: React.FC<GSTINInputProps> = ({
  value,
  onChange,
  label = 'GSTIN',
  placeholder = 'Enter 15-character GSTIN',
  required = false,
  error,
  disabled = false,
}) => {
  const [touched, setTouched] = useState(false);
  
  const isValid = value.length === 0 || validateGSTIN(value);
  const stateCode = value.length >= 2 ? getStateCodeFromGSTIN(value) : null;
  const stateName = stateCode ? getStateName(stateCode) : null;
  
  const showError = touched && !isValid && value.length > 0;
  const displayError = error || (showError ? 'Invalid GSTIN format' : '');

  return (
    <div className="w-full">
      <label className="block text-sm font-medium text-gray-700 mb-1">
        {label}
        {required && <span className="text-red-500 ml-1">*</span>}
      </label>
      
      <input
        type="text"
        value={value}
        onChange={(e) => {
          const upperValue = e.target.value.toUpperCase();
          onChange(upperValue);
        }}
        onBlur={() => setTouched(true)}
        placeholder={placeholder}
        maxLength={15}
        disabled={disabled}
        className={`
          w-full px-3 py-2 border rounded-md shadow-sm
          focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent
          ${displayError ? 'border-red-500' : 'border-gray-300'}
          ${disabled ? 'bg-gray-100 cursor-not-allowed' : 'bg-white'}
        `}
      />
      
      {/* Character count */}
      <div className="mt-1 text-xs text-gray-500">
        {value.length}/15 characters
      </div>
      
      {/* State information */}
      {stateName && isValid && (
        <div className="mt-1 text-sm text-green-600 flex items-center">
          <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
          </svg>
          State: {stateName} ({stateCode})
        </div>
      )}
      
      {/* Error message */}
      {displayError && (
        <div className="mt-1 text-sm text-red-600 flex items-center">
          <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
          </svg>
          {displayError}
        </div>
      )}
      
      {/* Format help */}
      {value.length === 0 && !disabled && (
        <div className="mt-2 text-xs text-gray-500">
          Format: 22AAAAA0000A1Z5 (State code + PAN + Entity + Z + Checksum)
        </div>
      )}
    </div>
  );
};

export default GSTINInput;
