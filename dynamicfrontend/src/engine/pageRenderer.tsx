/**
 * Page Renderer
 * 
 * Renders complete pages from metadata with region-based layouts.
 */

import React from 'react';
import { PageMetadata } from './metadataClient';
import componentRegistry from './registry';

interface PageRendererProps {
  pageMeta: PageMetadata;
}

export function PageRenderer({ pageMeta }: PageRendererProps) {
  const { layout, title } = pageMeta;

  const renderRegion = (regionName: string) => {
    const items = layout.items.filter((item) => item.region === regionName);

    return (
      <div key={regionName} className="space-y-4">
        {items.map((item, index) => {
          const Widget = componentRegistry.resolve(item.widget) || 
                        componentRegistry.resolve(`Widget_${item.widget}`);

          if (!Widget) {
            return (
              <div key={index} className="p-4 bg-red-50 border border-red-200 rounded">
                <p className="text-red-600">Widget "{item.widget}" not found</p>
              </div>
            );
          }

          return (
            <div key={index}>
              <Widget {...(item.props || {})} />
            </div>
          );
        })}
      </div>
    );
  };

  // Determine layout class based on regions
  const getLayoutClass = () => {
    const regionCount = layout.regions.length;
    
    if (regionCount === 1) return 'grid-cols-1';
    if (regionCount === 2) return 'grid-cols-2';
    if (regionCount === 3) return 'grid-cols-3';
    if (regionCount === 4) return 'grid-cols-4';
    
    return 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3';
  };

  return (
    <div className="w-full">
      {title && (
        <h1 className="text-3xl font-bold mb-6">{title}</h1>
      )}
      
      <div className={`grid ${getLayoutClass()} gap-6`}>
        {layout.regions.map((region) => (
          <div key={region} className="space-y-4">
            {renderRegion(region)}
          </div>
        ))}
      </div>
    </div>
  );
}
