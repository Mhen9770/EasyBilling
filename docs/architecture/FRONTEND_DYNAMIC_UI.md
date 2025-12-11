# Frontend Dynamic UI Architecture

## Overview

The frontend implements a metadata-driven rendering engine that dynamically generates forms, tables, and UI components based on metadata definitions from the backend.

## Core Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    React Application (Next.js)              │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐   │
│  │   Metadata   │  │    Theme     │  │   Permission    │   │
│  │   Provider   │  │   Provider   │  │    Provider     │   │
│  └──────┬───────┘  └──────┬───────┘  └────────┬────────┘   │
│         │                  │                    │            │
│  ┌──────┴──────────────────┴────────────────────┴────────┐  │
│  │                  Context Providers                     │  │
│  └──────┬──────────────────┬────────────────────┬────────┘  │
│         │                  │                    │            │
│  ┌──────▼───────┐  ┌───────▼──────┐  ┌─────────▼────────┐  │
│  │   Dynamic    │  │   Dynamic    │  │    Dynamic       │  │
│  │     Form     │  │    Table     │  │     Theme        │  │
│  │   Renderer   │  │   Renderer   │  │    Engine        │  │
│  └──────┬───────┘  └───────┬──────┘  └─────────┬────────┘  │
│         │                  │                    │            │
│  ┌──────▼──────────────────▼────────────────────▼────────┐  │
│  │              Field Component Library                   │  │
│  │  ┌─────────┐  ┌─────────┐  ┌──────────┐  ┌─────────┐ │  │
│  │  │  Text   │  │ Number  │  │  Select  │  │  Date   │ │  │
│  │  └─────────┘  └─────────┘  └──────────┘  └─────────┘ │  │
│  │  ┌─────────┐  ┌─────────┐  ┌──────────┐  ┌─────────┐ │  │
│  │  │Checkbox │  │  Radio  │  │TextArea  │  │   ...   │ │  │
│  │  └─────────┘  └─────────┘  └──────────┘  └─────────┘ │  │
│  └────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## 1. Dynamic Form Renderer

### Implementation

```typescript
// src/components/dynamic/DynamicForm.tsx
import React, { useEffect, useState } from 'react';
import { useMetadata } from '@/hooks/useMetadata';
import { DynamicField } from './DynamicField';

interface DynamicFormProps {
  entityType: string;
  formName: string;
  initialValues?: Record<string, any>;
  onSubmit: (values: Record<string, any>) => Promise<void>;
  onCancel?: () => void;
}

export const DynamicForm: React.FC<DynamicFormProps> = ({
  entityType,
  formName,
  initialValues = {},
  onSubmit,
  onCancel,
}) => {
  const { formMetadata, fieldMetadata, loading } = useMetadata(entityType, formName);
  const [formValues, setFormValues] = useState(initialValues);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    // Set default values from field metadata
    if (fieldMetadata) {
      const defaults: Record<string, any> = {};
      fieldMetadata.forEach(field => {
        if (field.defaultValue && !formValues[field.fieldName]) {
          defaults[field.fieldName] = field.defaultValue;
        }
      });
      setFormValues(prev => ({ ...defaults, ...prev }));
    }
  }, [fieldMetadata]);

  const handleFieldChange = (fieldName: string, value: any) => {
    setFormValues(prev => ({
      ...prev,
      [fieldName]: value,
    }));
    
    // Clear error for this field
    if (errors[fieldName]) {
      setErrors(prev => {
        const newErrors = { ...prev };
        delete newErrors[fieldName];
        return newErrors;
      });
    }
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};
    
    fieldMetadata?.forEach(field => {
      const value = formValues[field.fieldName];
      
      // Required field validation
      if (field.isRequired && !value) {
        newErrors[field.fieldName] = `${field.fieldLabel} is required`;
        return;
      }
      
      // Additional validations from metadata
      if (value && field.validationRules) {
        const rules = field.validationRules;
        
        // Min/Max for numbers
        if (field.dataType === 'integer' || field.dataType === 'decimal') {
          const numValue = Number(value);
          if (rules.min !== undefined && numValue < rules.min) {
            newErrors[field.fieldName] = `Must be at least ${rules.min}`;
          }
          if (rules.max !== undefined && numValue > rules.max) {
            newErrors[field.fieldName] = `Must be at most ${rules.max}`;
          }
        }
        
        // Min/Max length for strings
        if (field.dataType === 'string') {
          const strValue = String(value);
          if (rules.minLength && strValue.length < rules.minLength) {
            newErrors[field.fieldName] = `Must be at least ${rules.minLength} characters`;
          }
          if (rules.maxLength && strValue.length > rules.maxLength) {
            newErrors[field.fieldName] = `Must be at most ${rules.maxLength} characters`;
          }
          
          // Pattern validation
          if (rules.pattern && !new RegExp(rules.pattern).test(strValue)) {
            newErrors[field.fieldName] = rules.patternMessage || 'Invalid format';
          }
        }
      }
    });
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }
    
    setIsSubmitting(true);
    try {
      await onSubmit(formValues);
    } catch (error) {
      console.error('Form submission error:', error);
      setErrors({ _form: 'An error occurred while submitting the form' });
    } finally {
      setIsSubmitting(false);
    }
  };

  const shouldShowField = (field: FieldMetadata): boolean => {
    if (!field.isVisible) return false;
    
    // Check conditional logic
    if (field.conditionalLogic) {
      const { showWhen } = field.conditionalLogic;
      if (showWhen) {
        const dependentValue = formValues[showWhen.field];
        const operator = showWhen.operator;
        const expectedValue = showWhen.value;
        
        switch (operator) {
          case 'equals':
            return dependentValue === expectedValue;
          case 'notEquals':
            return dependentValue !== expectedValue;
          case 'in':
            return Array.isArray(expectedValue) && expectedValue.includes(dependentValue);
          default:
            return true;
        }
      }
    }
    
    return true;
  };

  if (loading) {
    return <div className="flex justify-center p-8">Loading form...</div>;
  }

  if (!formMetadata || !fieldMetadata) {
    return <div className="text-red-500">Form metadata not found</div>;
  }

  const layoutConfig = formMetadata.layoutConfig || { type: 'grid', columns: 2 };
  const gridCols = layoutConfig.columns || 2;

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div className="border-b pb-4">
        <h2 className="text-2xl font-bold">{formMetadata.displayLabel}</h2>
        {formMetadata.description && (
          <p className="text-gray-600 mt-1">{formMetadata.description}</p>
        )}
      </div>

      {errors._form && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {errors._form}
        </div>
      )}

      <div 
        className="grid gap-6"
        style={{
          gridTemplateColumns: `repeat(${gridCols}, minmax(0, 1fr))`,
        }}
      >
        {fieldMetadata
          .filter(shouldShowField)
          .sort((a, b) => (a.displayOrder || 0) - (b.displayOrder || 0))
          .map(field => (
            <DynamicField
              key={field.id}
              field={field}
              value={formValues[field.fieldName]}
              onChange={(value) => handleFieldChange(field.fieldName, value)}
              error={errors[field.fieldName]}
              readOnly={field.isReadonly}
            />
          ))}
      </div>

      <div className="flex gap-4 pt-4 border-t">
        <button
          type="submit"
          disabled={isSubmitting}
          className="px-6 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
        >
          {isSubmitting ? 'Submitting...' : 'Submit'}
        </button>
        {onCancel && (
          <button
            type="button"
            onClick={onCancel}
            className="px-6 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300"
          >
            Cancel
          </button>
        )}
      </div>
    </form>
  );
};
```

### Field Component

```typescript
// src/components/dynamic/DynamicField.tsx
import React from 'react';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';

interface DynamicFieldProps {
  field: FieldMetadata;
  value: any;
  onChange: (value: any) => void;
  error?: string;
  readOnly?: boolean;
}

export const DynamicField: React.FC<DynamicFieldProps> = ({
  field,
  value,
  onChange,
  error,
  readOnly = false,
}) => {
  const renderField = () => {
    const commonProps = {
      id: field.id,
      name: field.fieldName,
      value: value || '',
      onChange: (e: React.ChangeEvent<HTMLInputElement>) => onChange(e.target.value),
      placeholder: field.placeholder,
      disabled: readOnly,
      required: field.isRequired,
    };

    switch (field.fieldType.toLowerCase()) {
      case 'text':
        return <Input {...commonProps} type="text" />;
        
      case 'number':
        return (
          <Input
            {...commonProps}
            type="number"
            min={field.uiAttributes?.min}
            max={field.uiAttributes?.max}
            step={field.uiAttributes?.step}
          />
        );
        
      case 'email':
        return <Input {...commonProps} type="email" />;
        
      case 'password':
        return <Input {...commonProps} type="password" />;
        
      case 'date':
        return <Input {...commonProps} type="date" />;
        
      case 'time':
        return <Input {...commonProps} type="time" />;
        
      case 'datetime':
        return <Input {...commonProps} type="datetime-local" />;
        
      case 'textarea':
        return (
          <textarea
            {...commonProps}
            rows={field.uiAttributes?.rows || 4}
            className="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        );
        
      case 'select':
        return (
          <Select
            {...commonProps}
            options={field.options?.items || []}
            onChange={(value) => onChange(value)}
          />
        );
        
      case 'checkbox':
        return (
          <input
            type="checkbox"
            checked={!!value}
            onChange={(e) => onChange(e.target.checked)}
            disabled={readOnly}
            className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
          />
        );
        
      default:
        return <Input {...commonProps} type="text" />;
    }
  };

  const gridColumn = field.gridColumn || 'span 1';

  return (
    <div style={{ gridColumn }} className="space-y-2">
      <label
        htmlFor={field.id}
        className="block text-sm font-medium text-gray-700"
      >
        {field.fieldLabel}
        {field.isRequired && <span className="text-red-500 ml-1">*</span>}
      </label>
      
      {renderField()}
      
      {field.helpText && (
        <p className="text-sm text-gray-500">{field.helpText}</p>
      )}
      
      {error && (
        <p className="text-sm text-red-600">{error}</p>
      )}
      
      {field.uiAttributes?.suffix && value && (
        <span className="text-sm text-gray-500 ml-2">
          {field.uiAttributes.suffix}
        </span>
      )}
    </div>
  );
};
```

## 2. Metadata Hook

```typescript
// src/hooks/useMetadata.ts
import { useEffect, useState } from 'react';
import { metadataApi } from '@/lib/api/metadata';

export const useMetadata = (entityType: string, formName: string) => {
  const [formMetadata, setFormMetadata] = useState<FormMetadata | null>(null);
  const [fieldMetadata, setFieldMetadata] = useState<FieldMetadata[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    const loadMetadata = async () => {
      try {
        setLoading(true);
        
        const [form, fields] = await Promise.all([
          metadataApi.getFormMetadata(entityType, formName),
          metadataApi.getFieldMetadata(entityType, formName),
        ]);
        
        setFormMetadata(form);
        setFieldMetadata(fields);
        setError(null);
      } catch (err) {
        setError(err as Error);
        console.error('Error loading metadata:', err);
      } finally {
        setLoading(false);
      }
    };

    loadMetadata();
  }, [entityType, formName]);

  return { formMetadata, fieldMetadata, loading, error };
};
```

## 3. Dynamic Table Renderer

```typescript
// src/components/dynamic/DynamicTable.tsx
import React, { useState, useEffect } from 'react';
import { useMetadata } from '@/hooks/useMetadata';

interface DynamicTableProps {
  entityType: string;
  data: any[];
  onRowClick?: (row: any) => void;
  onEdit?: (row: any) => void;
  onDelete?: (row: any) => void;
}

export const DynamicTable: React.FC<DynamicTableProps> = ({
  entityType,
  data,
  onRowClick,
  onEdit,
  onDelete,
}) => {
  const { fieldMetadata, loading } = useMetadata(entityType, 'list');
  const [sortField, setSortField] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');

  // Get visible and sortable columns
  const columns = fieldMetadata?.filter(f => f.isVisible) || [];

  const handleSort = (fieldName: string) => {
    if (sortField === fieldName) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(fieldName);
      setSortDirection('asc');
    }
  };

  const sortedData = React.useMemo(() => {
    if (!sortField) return data;
    
    return [...data].sort((a, b) => {
      const aVal = a[sortField];
      const bVal = b[sortField];
      
      if (aVal === bVal) return 0;
      
      const result = aVal > bVal ? 1 : -1;
      return sortDirection === 'asc' ? result : -result;
    });
  }, [data, sortField, sortDirection]);

  if (loading) {
    return <div>Loading table...</div>;
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            {columns.map(col => (
              <th
                key={col.id}
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                onClick={() => col.isSortable && handleSort(col.fieldName)}
              >
                <div className="flex items-center space-x-1">
                  <span>{col.fieldLabel}</span>
                  {col.isSortable && sortField === col.fieldName && (
                    <span>{sortDirection === 'asc' ? '↑' : '↓'}</span>
                  )}
                </div>
              </th>
            ))}
            {(onEdit || onDelete) && (
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                Actions
              </th>
            )}
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {sortedData.map((row, idx) => (
            <tr
              key={idx}
              className={onRowClick ? 'cursor-pointer hover:bg-gray-50' : ''}
              onClick={() => onRowClick?.(row)}
            >
              {columns.map(col => (
                <td key={col.id} className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {formatValue(row[col.fieldName], col.dataType)}
                </td>
              ))}
              {(onEdit || onDelete) && (
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  {onEdit && (
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        onEdit(row);
                      }}
                      className="text-blue-600 hover:text-blue-900 mr-4"
                    >
                      Edit
                    </button>
                  )}
                  {onDelete && (
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        onDelete(row);
                      }}
                      className="text-red-600 hover:text-red-900"
                    >
                      Delete
                    </button>
                  )}
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

function formatValue(value: any, dataType: string): string {
  if (value === null || value === undefined) return '-';
  
  switch (dataType) {
    case 'boolean':
      return value ? 'Yes' : 'No';
    case 'decimal':
      return Number(value).toFixed(2);
    case 'date':
      return new Date(value).toLocaleDateString();
    case 'datetime':
      return new Date(value).toLocaleString();
    default:
      return String(value);
  }
}
```

## Folder Structure

```
frontend/
├── src/
│   ├── components/
│   │   ├── dynamic/
│   │   │   ├── DynamicForm.tsx
│   │   │   ├── DynamicTable.tsx
│   │   │   ├── DynamicField.tsx
│   │   │   └── index.ts
│   │   └── ui/
│   │       ├── Input.tsx
│   │       ├── Select.tsx
│   │       ├── Button.tsx
│   │       └── ...
│   ├── hooks/
│   │   ├── useMetadata.ts
│   │   ├── useTheme.ts
│   │   └── usePermissions.ts
│   ├── lib/
│   │   └── api/
│   │       ├── metadata/
│   │       │   └── metadataApi.ts
│   │       └── index.ts
│   ├── types/
│   │   ├── metadata.ts
│   │   ├── forms.ts
│   │   └── index.ts
│   └── app/
│       └── ...
```

