/**
 * Primitive Form Controls
 * 
 * Basic form components that can be used in dynamic forms.
 */

import React from 'react';
import { useFormContext } from 'react-hook-form';
import { FieldMetadata } from '../engine/metadataClient';

interface FieldProps {
  field: FieldMetadata;
}

/**
 * Text Input Component
 */
export function TextInput({ field }: FieldProps) {
  const { register, formState: { errors } } = useFormContext();
  const error = errors[field.name];
  
  return (
    <div className="w-full">
      <label htmlFor={field.name} className="block text-sm font-medium text-gray-700 mb-1">
        {field.label}
        {field.required && <span className="text-red-500 ml-1">*</span>}
      </label>
      <input
        id={field.name}
        type="text"
        {...register(field.name)}
        className={`w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
          error ? 'border-red-500' : 'border-gray-300'
        }`}
        placeholder={field.label}
      />
      {error && (
        <p className="mt-1 text-sm text-red-600">{error.message as string}</p>
      )}
    </div>
  );
}

/**
 * Text Area Component
 */
export function TextArea({ field }: FieldProps) {
  const { register, formState: { errors } } = useFormContext();
  const error = errors[field.name];
  
  return (
    <div className="w-full">
      <label htmlFor={field.name} className="block text-sm font-medium text-gray-700 mb-1">
        {field.label}
        {field.required && <span className="text-red-500 ml-1">*</span>}
      </label>
      <textarea
        id={field.name}
        {...register(field.name)}
        rows={4}
        className={`w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
          error ? 'border-red-500' : 'border-gray-300'
        }`}
        placeholder={field.label}
      />
      {error && (
        <p className="mt-1 text-sm text-red-600">{error.message as string}</p>
      )}
    </div>
  );
}

/**
 * Number Input Component
 */
export function NumberInput({ field }: FieldProps) {
  const { register, formState: { errors } } = useFormContext();
  const error = errors[field.name];
  
  return (
    <div className="w-full">
      <label htmlFor={field.name} className="block text-sm font-medium text-gray-700 mb-1">
        {field.label}
        {field.required && <span className="text-red-500 ml-1">*</span>}
      </label>
      <input
        id={field.name}
        type="number"
        {...register(field.name, { valueAsNumber: true })}
        className={`w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
          error ? 'border-red-500' : 'border-gray-300'
        }`}
        placeholder={field.label}
        min={field.validation?.min}
        max={field.validation?.max}
      />
      {error && (
        <p className="mt-1 text-sm text-red-600">{error.message as string}</p>
      )}
    </div>
  );
}

/**
 * Select Component
 */
export function SelectInput({ field }: FieldProps) {
  const { register, formState: { errors } } = useFormContext();
  const error = errors[field.name];
  
  const options = field.optionsSource?.data || [];
  
  return (
    <div className="w-full">
      <label htmlFor={field.name} className="block text-sm font-medium text-gray-700 mb-1">
        {field.label}
        {field.required && <span className="text-red-500 ml-1">*</span>}
      </label>
      <select
        id={field.name}
        {...register(field.name)}
        className={`w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
          error ? 'border-red-500' : 'border-gray-300'
        }`}
      >
        <option value="">Select {field.label}</option>
        {options.map((option: any) => (
          <option key={option.value || option} value={option.value || option}>
            {option.label || option}
          </option>
        ))}
      </select>
      {error && (
        <p className="mt-1 text-sm text-red-600">{error.message as string}</p>
      )}
    </div>
  );
}

/**
 * Checkbox Component
 */
export function CheckboxInput({ field }: FieldProps) {
  const { register, formState: { errors } } = useFormContext();
  const error = errors[field.name];
  
  return (
    <div className="w-full flex items-center">
      <input
        id={field.name}
        type="checkbox"
        {...register(field.name)}
        className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
      />
      <label htmlFor={field.name} className="ml-2 block text-sm text-gray-900">
        {field.label}
        {field.required && <span className="text-red-500 ml-1">*</span>}
      </label>
      {error && (
        <p className="mt-1 text-sm text-red-600 ml-6">{error.message as string}</p>
      )}
    </div>
  );
}

/**
 * Date Input Component
 */
export function DateInput({ field }: FieldProps) {
  const { register, formState: { errors } } = useFormContext();
  const error = errors[field.name];
  
  return (
    <div className="w-full">
      <label htmlFor={field.name} className="block text-sm font-medium text-gray-700 mb-1">
        {field.label}
        {field.required && <span className="text-red-500 ml-1">*</span>}
      </label>
      <input
        id={field.name}
        type="date"
        {...register(field.name)}
        className={`w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
          error ? 'border-red-500' : 'border-gray-300'
        }`}
      />
      {error && (
        <p className="mt-1 text-sm text-red-600">{error.message as string}</p>
      )}
    </div>
  );
}
