/**
 * Form Renderer
 * 
 * Dynamically renders forms based on metadata from the backend.
 */

import React from 'react';
import { useForm, FormProvider } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { FormMetadata, FieldMetadata } from './metadataClient';
import componentRegistry from './registry';

/**
 * Build Zod schema from field metadata
 */
export function buildZodSchemaFromMeta(fields: FieldMetadata[]): z.ZodObject<any> {
  const shape: Record<string, z.ZodTypeAny> = {};
  
  fields.forEach((field) => {
    let fieldSchema: z.ZodTypeAny = z.any();
    
    // Determine base type
    if (field.component === 'Text' || field.component === 'TextArea') {
      fieldSchema = z.string();
      
      if (field.validation?.minLength) {
        fieldSchema = (fieldSchema as z.ZodString).min(field.validation.minLength);
      }
      if (field.validation?.maxLength) {
        fieldSchema = (fieldSchema as z.ZodString).max(field.validation.maxLength);
      }
      if (field.validation?.pattern) {
        fieldSchema = (fieldSchema as z.ZodString).regex(new RegExp(field.validation.pattern));
      }
    } else if (field.component === 'Number') {
      fieldSchema = z.number();
      
      if (field.validation?.min !== undefined) {
        fieldSchema = (fieldSchema as z.ZodNumber).min(field.validation.min);
      }
      if (field.validation?.max !== undefined) {
        fieldSchema = (fieldSchema as z.ZodNumber).max(field.validation.max);
      }
    } else if (field.component === 'Checkbox') {
      fieldSchema = z.boolean();
    } else if (field.component === 'Date') {
      fieldSchema = z.date().or(z.string());
    } else if (field.component === 'Select') {
      fieldSchema = z.string();
    }
    
    // Make optional if not required
    if (!field.required) {
      fieldSchema = fieldSchema.optional();
    }
    
    shape[field.name] = fieldSchema;
  });
  
  return z.object(shape);
}

/**
 * Field Renderer Component
 */
interface FieldRendererProps {
  field: FieldMetadata;
}

export function FieldRenderer({ field }: FieldRendererProps) {
  const Component = componentRegistry.resolve(field.component);
  
  if (!Component) {
    return (
      <div className="text-red-500 p-2 border border-red-300 rounded">
        Component "{field.component}" not found in registry
      </div>
    );
  }
  
  return <Component field={field} />;
}

/**
 * Form Renderer Component
 */
interface FormRendererProps {
  formMeta: FormMetadata;
  onSuccess?: (data: any) => void;
  onError?: (errors: any) => void;
  defaultValues?: Record<string, any>;
}

export function FormRenderer({ formMeta, onSuccess, onError, defaultValues }: FormRendererProps) {
  const zodSchema = buildZodSchemaFromMeta(formMeta.fields);
  
  const methods = useForm({
    resolver: zodResolver(zodSchema),
    defaultValues: defaultValues || {},
  });
  
  const handleSubmit = async (data: any) => {
    try {
      console.log('Form submitted:', data);
      if (onSuccess) {
        onSuccess(data);
      }
    } catch (error) {
      console.error('Form submission error:', error);
      if (onError) {
        onError(error);
      }
    }
  };
  
  return (
    <FormProvider {...methods}>
      <form onSubmit={methods.handleSubmit(handleSubmit)} className="space-y-4">
        <h2 className="text-2xl font-bold mb-4">{formMeta.title}</h2>
        
        <div className={`grid ${formMeta.layout.type === 'two-column' ? 'grid-cols-2' : 'grid-cols-1'} gap-4`}>
          {formMeta.fields.map((field) => (
            <div key={field.name} className="space-y-1">
              <FieldRenderer field={field} />
            </div>
          ))}
        </div>
        
        <div className="flex gap-2 pt-4">
          {formMeta.actions.map((action) => (
            <button
              key={action.id}
              type={action.type === 'submit' ? 'submit' : 'button'}
              className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:bg-gray-400"
              disabled={action.type === 'submit' && methods.formState.isSubmitting}
            >
              {action.label}
            </button>
          ))}
        </div>
        
        {methods.formState.errors && Object.keys(methods.formState.errors).length > 0 && (
          <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded">
            <p className="text-red-800 font-semibold">Validation Errors:</p>
            <ul className="list-disc list-inside text-red-600">
              {Object.entries(methods.formState.errors).map(([field, error]: [string, any]) => (
                <li key={field}>{field}: {error.message}</li>
              ))}
            </ul>
          </div>
        )}
      </form>
    </FormProvider>
  );
}
