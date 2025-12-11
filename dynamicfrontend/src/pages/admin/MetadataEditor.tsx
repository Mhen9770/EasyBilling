/**
 * Admin Metadata Editor
 * 
 * UI for creating and editing entity metadata.
 */

import React, { useState } from 'react';
import { FormMetadata, FieldMetadata, ActionMetadata } from '../../engine/metadataClient';

export function MetadataEditor() {
  const [formMeta, setFormMeta] = useState<Partial<FormMetadata>>({
    id: '',
    entity: '',
    title: '',
    layout: { type: 'single-column' },
    fields: [],
    actions: [],
  });

  const [currentField, setCurrentField] = useState<Partial<FieldMetadata>>({
    name: '',
    component: 'Text',
    label: '',
    required: false,
  });

  const addField = () => {
    if (!currentField.name || !currentField.label) {
      alert('Field name and label are required');
      return;
    }

    setFormMeta({
      ...formMeta,
      fields: [...(formMeta.fields || []), currentField as FieldMetadata],
    });

    setCurrentField({
      name: '',
      component: 'Text',
      label: '',
      required: false,
    });
  };

  const removeField = (index: number) => {
    const newFields = [...(formMeta.fields || [])];
    newFields.splice(index, 1);
    setFormMeta({ ...formMeta, fields: newFields });
  };

  const addAction = () => {
    const newAction: ActionMetadata = {
      id: 'action_' + Date.now(),
      label: 'New Action',
      type: 'submit',
    };

    setFormMeta({
      ...formMeta,
      actions: [...(formMeta.actions || []), newAction],
    });
  };

  const exportMetadata = () => {
    const json = JSON.stringify(formMeta, null, 2);
    const blob = new Blob([json], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${formMeta.entity || 'metadata'}.json`;
    a.click();
  };

  const importMetadata = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const imported = JSON.parse(e.target?.result as string);
        setFormMeta(imported);
        alert('Metadata imported successfully');
      } catch (err) {
        alert('Failed to import metadata: Invalid JSON');
      }
    };
    reader.readAsText(file);
  };

  return (
    <div className="max-w-6xl mx-auto p-6">
      <h1 className="text-3xl font-bold mb-6">Metadata Editor</h1>

      <div className="grid grid-cols-2 gap-6">
        {/* Left Panel - Form Properties */}
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold mb-4">Form Properties</h2>
          
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Form ID
              </label>
              <input
                type="text"
                value={formMeta.id}
                onChange={(e) => setFormMeta({ ...formMeta, id: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
                placeholder="customer.form.basic"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Entity Name
              </label>
              <input
                type="text"
                value={formMeta.entity}
                onChange={(e) => setFormMeta({ ...formMeta, entity: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
                placeholder="Customer"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Form Title
              </label>
              <input
                type="text"
                value={formMeta.title}
                onChange={(e) => setFormMeta({ ...formMeta, title: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
                placeholder="Create Customer"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Layout Type
              </label>
              <select
                value={formMeta.layout?.type}
                onChange={(e) =>
                  setFormMeta({
                    ...formMeta,
                    layout: { ...formMeta.layout!, type: e.target.value },
                  })
                }
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
              >
                <option value="single-column">Single Column</option>
                <option value="two-column">Two Column</option>
                <option value="three-column">Three Column</option>
              </select>
            </div>
          </div>

          <div className="mt-6 pt-4 border-t">
            <h3 className="text-lg font-semibold mb-3">Actions</h3>
            <button
              onClick={addAction}
              className="w-full px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 mb-3"
            >
              Add Action
            </button>
            <div className="text-sm text-gray-600">
              {formMeta.actions?.length || 0} action(s) configured
            </div>
          </div>
        </div>

        {/* Right Panel - Field Builder */}
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold mb-4">Add Field</h2>

          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Field Name
              </label>
              <input
                type="text"
                value={currentField.name}
                onChange={(e) =>
                  setCurrentField({ ...currentField, name: e.target.value })
                }
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
                placeholder="customerName"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Label
              </label>
              <input
                type="text"
                value={currentField.label}
                onChange={(e) =>
                  setCurrentField({ ...currentField, label: e.target.value })
                }
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
                placeholder="Customer Name"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Component Type
              </label>
              <select
                value={currentField.component}
                onChange={(e) =>
                  setCurrentField({ ...currentField, component: e.target.value })
                }
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
              >
                <option value="Text">Text</option>
                <option value="TextArea">TextArea</option>
                <option value="Number">Number</option>
                <option value="Select">Select</option>
                <option value="Checkbox">Checkbox</option>
                <option value="Date">Date</option>
              </select>
            </div>

            <div className="flex items-center">
              <input
                type="checkbox"
                checked={currentField.required}
                onChange={(e) =>
                  setCurrentField({ ...currentField, required: e.target.checked })
                }
                className="h-4 w-4 text-blue-600 rounded"
              />
              <label className="ml-2 text-sm text-gray-700">Required Field</label>
            </div>

            <button
              onClick={addField}
              className="w-full px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
            >
              Add Field
            </button>
          </div>
        </div>
      </div>

      {/* Fields List */}
      <div className="mt-6 bg-white p-6 rounded-lg shadow-md">
        <h2 className="text-xl font-semibold mb-4">
          Fields ({formMeta.fields?.length || 0})
        </h2>
        <div className="space-y-2">
          {formMeta.fields?.map((field, index) => (
            <div
              key={index}
              className="flex items-center justify-between p-3 bg-gray-50 rounded"
            >
              <div>
                <span className="font-medium">{field.name}</span>
                <span className="text-gray-600 ml-2">({field.component})</span>
                {field.required && (
                  <span className="ml-2 text-xs bg-red-100 text-red-800 px-2 py-1 rounded">
                    Required
                  </span>
                )}
              </div>
              <button
                onClick={() => removeField(index)}
                className="px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700 text-sm"
              >
                Remove
              </button>
            </div>
          ))}
        </div>
      </div>

      {/* Export/Import */}
      <div className="mt-6 flex gap-4">
        <button
          onClick={exportMetadata}
          className="flex-1 px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700"
        >
          Export Metadata (JSON)
        </button>
        <label className="flex-1 px-6 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 text-center cursor-pointer">
          Import Metadata (JSON)
          <input
            type="file"
            accept=".json"
            onChange={importMetadata}
            className="hidden"
          />
        </label>
      </div>

      {/* Preview */}
      <div className="mt-6 bg-gray-50 p-6 rounded-lg">
        <h2 className="text-xl font-semibold mb-4">JSON Preview</h2>
        <pre className="bg-white p-4 rounded border overflow-x-auto text-xs">
          {JSON.stringify(formMeta, null, 2)}
        </pre>
      </div>
    </div>
  );
}
