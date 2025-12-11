/**
 * Layout Builder
 * 
 * Visual drag-and-drop layout builder for creating page layouts.
 */

import React, { useState } from 'react';
import { PageMetadata } from '../engine/metadataClient';

interface LayoutBuilderProps {
  onSave?: (layout: PageMetadata) => void;
}

export function LayoutBuilder({ onSave }: LayoutBuilderProps) {
  const [pageMeta, setPageMeta] = useState<PageMetadata>({
    id: '',
    title: '',
    layout: {
      regions: ['left', 'center', 'right'],
      items: [],
    },
    permissions: [],
  });

  const [selectedRegion, setSelectedRegion] = useState<string>('left');
  const [widgetName, setWidgetName] = useState('');

  const availableWidgets = [
    'KPICard',
    'ChartWidget',
    'QuickActionsWidget',
    'ActivityFeedWidget',
    'DataCardWidget',
    'FormRenderer',
    'ListRenderer',
  ];

  const addWidget = () => {
    if (!widgetName || !selectedRegion) return;

    const newItem = {
      region: selectedRegion,
      widget: widgetName,
      props: {},
    };

    setPageMeta({
      ...pageMeta,
      layout: {
        ...pageMeta.layout,
        items: [...pageMeta.layout.items, newItem],
      },
    });

    setWidgetName('');
  };

  const removeWidget = (index: number) => {
    const newItems = [...pageMeta.layout.items];
    newItems.splice(index, 1);
    setPageMeta({
      ...pageMeta,
      layout: {
        ...pageMeta.layout,
        items: newItems,
      },
    });
  };

  const addRegion = () => {
    const newRegionName = `region_${pageMeta.layout.regions.length + 1}`;
    setPageMeta({
      ...pageMeta,
      layout: {
        ...pageMeta.layout,
        regions: [...pageMeta.layout.regions, newRegionName],
      },
    });
  };

  const removeRegion = (regionName: string) => {
    // Remove region and all items in that region
    const newRegions = pageMeta.layout.regions.filter((r) => r !== regionName);
    const newItems = pageMeta.layout.items.filter((item) => item.region !== regionName);

    setPageMeta({
      ...pageMeta,
      layout: {
        regions: newRegions,
        items: newItems,
      },
    });
  };

  const exportLayout = () => {
    const json = JSON.stringify(pageMeta, null, 2);
    const blob = new Blob([json], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `page-${pageMeta.id || 'layout'}.json`;
    a.click();
  };

  const handleSave = () => {
    if (onSave) {
      onSave(pageMeta);
    }
    alert('Layout saved!');
  };

  return (
    <div className="max-w-7xl mx-auto p-6">
      <h1 className="text-3xl font-bold mb-6">Page Layout Builder</h1>

      <div className="grid grid-cols-3 gap-6">
        {/* Left Panel - Page Properties */}
        <div className="col-span-1 bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold mb-4">Page Properties</h2>

          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Page ID
              </label>
              <input
                type="text"
                value={pageMeta.id}
                onChange={(e) => setPageMeta({ ...pageMeta, id: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
                placeholder="dashboard.main"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Page Title
              </label>
              <input
                type="text"
                value={pageMeta.title}
                onChange={(e) => setPageMeta({ ...pageMeta, title: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
                placeholder="Dashboard"
              />
            </div>

            <div className="pt-4 border-t">
              <h3 className="text-lg font-semibold mb-3">Regions</h3>
              <div className="space-y-2 mb-3">
                {pageMeta.layout.regions.map((region) => (
                  <div key={region} className="flex items-center justify-between p-2 bg-gray-50 rounded">
                    <span className="font-medium">{region}</span>
                    <button
                      onClick={() => removeRegion(region)}
                      className="text-red-600 hover:text-red-800 text-sm"
                    >
                      Remove
                    </button>
                  </div>
                ))}
              </div>
              <button
                onClick={addRegion}
                className="w-full px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
              >
                Add Region
              </button>
            </div>
          </div>
        </div>

        {/* Middle Panel - Widget Library */}
        <div className="col-span-1 bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold mb-4">Add Widget</h2>

          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Select Region
              </label>
              <select
                value={selectedRegion}
                onChange={(e) => setSelectedRegion(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
              >
                {pageMeta.layout.regions.map((region) => (
                  <option key={region} value={region}>
                    {region}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Select Widget
              </label>
              <select
                value={widgetName}
                onChange={(e) => setWidgetName(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
              >
                <option value="">Choose a widget...</option>
                {availableWidgets.map((widget) => (
                  <option key={widget} value={widget}>
                    {widget}
                  </option>
                ))}
              </select>
            </div>

            <button
              onClick={addWidget}
              disabled={!widgetName || !selectedRegion}
              className="w-full px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 disabled:bg-gray-400"
            >
              Add Widget to {selectedRegion}
            </button>
          </div>

          <div className="mt-6 pt-4 border-t">
            <h3 className="text-lg font-semibold mb-3">Available Widgets</h3>
            <div className="space-y-2 max-h-96 overflow-y-auto">
              {availableWidgets.map((widget) => (
                <div key={widget} className="p-2 bg-gray-50 rounded text-sm">
                  {widget}
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right Panel - Layout Preview */}
        <div className="col-span-1 bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold mb-4">Layout Items</h2>

          <div className="space-y-3 max-h-[600px] overflow-y-auto">
            {pageMeta.layout.items.length === 0 ? (
              <p className="text-gray-500 text-center py-8">No widgets added yet</p>
            ) : (
              pageMeta.layout.items.map((item, index) => (
                <div
                  key={index}
                  className="p-3 bg-blue-50 border border-blue-200 rounded"
                >
                  <div className="flex items-center justify-between mb-2">
                    <span className="font-medium text-blue-900">{item.widget}</span>
                    <button
                      onClick={() => removeWidget(index)}
                      className="text-red-600 hover:text-red-800 text-sm"
                    >
                      Remove
                    </button>
                  </div>
                  <div className="text-sm text-blue-700">
                    Region: <span className="font-medium">{item.region}</span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="mt-6 flex gap-4">
        <button
          onClick={handleSave}
          className="flex-1 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          Save Layout
        </button>
        <button
          onClick={exportLayout}
          className="flex-1 px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700"
        >
          Export JSON
        </button>
      </div>

      {/* JSON Preview */}
      <div className="mt-6 bg-gray-50 p-6 rounded-lg">
        <h2 className="text-xl font-semibold mb-4">JSON Preview</h2>
        <pre className="bg-white p-4 rounded border overflow-x-auto text-xs">
          {JSON.stringify(pageMeta, null, 2)}
        </pre>
      </div>
    </div>
  );
}
