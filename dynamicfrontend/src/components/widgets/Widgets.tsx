/**
 * Reusable Widgets
 * 
 * Pre-built widgets for common UI patterns.
 */

import React from 'react';

/**
 * KPI Card Widget
 */
interface KPICardProps {
  title: string;
  value: string | number;
  change?: number;
  icon?: string;
  color?: string;
}

export function KPICard({ title, value, change, icon, color = 'blue' }: KPICardProps) {
  const colorClasses = {
    blue: 'bg-blue-500',
    green: 'bg-green-500',
    yellow: 'bg-yellow-500',
    red: 'bg-red-500',
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <div className="flex items-center justify-between mb-2">
        <h3 className="text-sm font-medium text-gray-600">{title}</h3>
        {icon && <span className="text-2xl">{icon}</span>}
      </div>
      <div className="text-3xl font-bold text-gray-900 mb-2">{value}</div>
      {change !== undefined && (
        <div className={`text-sm ${change >= 0 ? 'text-green-600' : 'text-red-600'}`}>
          {change >= 0 ? '↑' : '↓'} {Math.abs(change)}%
        </div>
      )}
    </div>
  );
}

/**
 * Chart Widget Placeholder
 */
interface ChartWidgetProps {
  title: string;
  data: any[];
  type?: 'line' | 'bar' | 'pie';
}

export function ChartWidget({ title, data, type = 'line' }: ChartWidgetProps) {
  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h3 className="text-lg font-semibold mb-4">{title}</h3>
      <div className="h-64 flex items-center justify-center bg-gray-50 rounded">
        <p className="text-gray-500">
          {type.charAt(0).toUpperCase() + type.slice(1)} Chart - {data.length} data points
        </p>
        <p className="text-xs text-gray-400 ml-2">(Chart library not yet integrated)</p>
      </div>
    </div>
  );
}

/**
 * Quick Actions Widget
 */
interface QuickAction {
  label: string;
  icon?: string;
  onClick: () => void;
  color?: string;
}

interface QuickActionsProps {
  actions: QuickAction[];
}

export function QuickActionsWidget({ actions }: QuickActionsProps) {
  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h3 className="text-lg font-semibold mb-4">Quick Actions</h3>
      <div className="grid grid-cols-2 gap-3">
        {actions.map((action, index) => (
          <button
            key={index}
            onClick={action.onClick}
            className={`p-4 rounded-lg border-2 hover:shadow-md transition-shadow ${
              action.color === 'primary'
                ? 'border-blue-500 hover:bg-blue-50'
                : 'border-gray-200 hover:bg-gray-50'
            }`}
          >
            {action.icon && <div className="text-2xl mb-2">{action.icon}</div>}
            <div className="text-sm font-medium">{action.label}</div>
          </button>
        ))}
      </div>
    </div>
  );
}

/**
 * Activity Feed Widget
 */
interface Activity {
  id: string;
  message: string;
  timestamp: Date;
  type?: 'info' | 'success' | 'warning' | 'error';
}

interface ActivityFeedProps {
  activities: Activity[];
}

export function ActivityFeedWidget({ activities }: ActivityFeedProps) {
  const typeColors = {
    info: 'bg-blue-100 text-blue-800',
    success: 'bg-green-100 text-green-800',
    warning: 'bg-yellow-100 text-yellow-800',
    error: 'bg-red-100 text-red-800',
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h3 className="text-lg font-semibold mb-4">Recent Activity</h3>
      <div className="space-y-3 max-h-96 overflow-y-auto">
        {activities.map((activity) => (
          <div key={activity.id} className="flex items-start gap-3 p-3 bg-gray-50 rounded">
            <div
              className={`px-2 py-1 rounded text-xs ${
                typeColors[activity.type || 'info']
              }`}
            >
              {activity.type || 'info'}
            </div>
            <div className="flex-1">
              <p className="text-sm text-gray-800">{activity.message}</p>
              <p className="text-xs text-gray-500 mt-1">
                {activity.timestamp.toLocaleString()}
              </p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

/**
 * Data Card Widget
 */
interface DataCardProps {
  title: string;
  data: Record<string, any>;
}

export function DataCardWidget({ title, data }: DataCardProps) {
  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h3 className="text-lg font-semibold mb-4">{title}</h3>
      <div className="space-y-2">
        {Object.entries(data).map(([key, value]) => (
          <div key={key} className="flex justify-between py-2 border-b border-gray-100">
            <span className="text-sm text-gray-600">{key}:</span>
            <span className="text-sm font-medium text-gray-900">
              {typeof value === 'object' ? JSON.stringify(value) : String(value)}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
