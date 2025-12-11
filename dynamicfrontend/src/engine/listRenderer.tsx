/**
 * List Renderer
 * 
 * Dynamic list/table rendering with pagination, sorting, and filtering.
 */

import React, { useState } from 'react';
import { ListMetadata } from './metadataClient';
import axios from 'axios';

interface ListRendererProps {
  listMeta: ListMetadata;
  onRowClick?: (row: any) => void;
  onRowAction?: (action: string, row: any) => void;
}

export function ListRenderer({ listMeta, onRowClick, onRowAction }: ListRendererProps) {
  const [data, setData] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [sortField, setSortField] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  const [filters, setFilters] = useState<Record<string, any>>({});

  React.useEffect(() => {
    fetchData();
  }, [currentPage, sortField, sortDirection, filters]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({
        page: currentPage.toString(),
        size: listMeta.pageSize.toString(),
      });

      if (sortField) {
        params.append('sort', `${sortField},${sortDirection}`);
      }

      Object.entries(filters).forEach(([key, value]) => {
        if (value) {
          params.append(key, value);
        }
      });

      const response = await axios.get(
        `http://localhost:8081/api/${listMeta.entity}/find?${params}`,
        {
          headers: {
            'X-Tenant-Id': 'default',
            'X-User-Id': 'admin',
          },
        }
      );

      // Handle different response structures
      if (response.data.content) {
        setData(response.data.content);
        setTotalPages(response.data.totalPages || 1);
      } else if (Array.isArray(response.data)) {
        setData(response.data);
        setTotalPages(1);
      } else {
        setData([response.data]);
        setTotalPages(1);
      }
    } catch (error) {
      console.error('Error fetching list data:', error);
      setData([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSort = (field: string) => {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('asc');
    }
  };

  const handleFilterChange = (field: string, value: any) => {
    setFilters({ ...filters, [field]: value });
    setCurrentPage(0);
  };

  const formatValue = (value: any, format?: string) => {
    if (value === null || value === undefined) return '-';
    
    if (format === 'currency') {
      return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
      }).format(value);
    }
    
    if (format === 'date') {
      return new Date(value).toLocaleDateString();
    }
    
    if (format === 'datetime') {
      return new Date(value).toLocaleString();
    }
    
    return value.toString();
  };

  return (
    <div className="w-full">
      <div className="mb-4">
        <h2 className="text-2xl font-bold mb-4">{listMeta.entity} List</h2>
        
        {/* Filters */}
        {listMeta.filters && listMeta.filters.length > 0 && (
          <div className="flex gap-4 mb-4 p-4 bg-gray-50 rounded-lg">
            {listMeta.filters.map((filter) => (
              <div key={filter.name} className="flex-1">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  {filter.label}
                </label>
                <input
                  type="text"
                  value={filters[filter.name] || ''}
                  onChange={(e) => handleFilterChange(filter.name, e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md"
                  placeholder={`Filter by ${filter.label}`}
                />
              </div>
            ))}
            <div className="flex items-end">
              <button
                onClick={() => {
                  setFilters({});
                  setCurrentPage(0);
                }}
                className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700"
              >
                Clear
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Table */}
      <div className="overflow-x-auto shadow-md rounded-lg">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              {listMeta.columns.map((column) => (
                <th
                  key={column.field}
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                  onClick={() => column.sortable && handleSort(column.field)}
                >
                  <div className="flex items-center gap-2">
                    {column.label}
                    {column.sortable && (
                      <span className="text-gray-400">
                        {sortField === column.field && (
                          sortDirection === 'asc' ? '↑' : '↓'
                        )}
                      </span>
                    )}
                  </div>
                </th>
              ))}
              {listMeta.rowActions && listMeta.rowActions.length > 0 && (
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              )}
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {loading ? (
              <tr>
                <td
                  colSpan={listMeta.columns.length + (listMeta.rowActions ? 1 : 0)}
                  className="px-6 py-4 text-center text-gray-500"
                >
                  Loading...
                </td>
              </tr>
            ) : data.length === 0 ? (
              <tr>
                <td
                  colSpan={listMeta.columns.length + (listMeta.rowActions ? 1 : 0)}
                  className="px-6 py-4 text-center text-gray-500"
                >
                  No data found
                </td>
              </tr>
            ) : (
              data.map((row, index) => (
                <tr
                  key={index}
                  className="hover:bg-gray-50 cursor-pointer"
                  onClick={() => onRowClick && onRowClick(row)}
                >
                  {listMeta.columns.map((column) => (
                    <td key={column.field} className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {formatValue(row[column.field] || row.attributes?.[column.field], column.format)}
                    </td>
                  ))}
                  {listMeta.rowActions && listMeta.rowActions.length > 0 && (
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      <div className="flex gap-2">
                        {listMeta.rowActions.map((action) => (
                          <button
                            key={action.id}
                            onClick={(e) => {
                              e.stopPropagation();
                              onRowAction && onRowAction(action.id, row);
                            }}
                            className="px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 text-xs"
                          >
                            {action.label}
                          </button>
                        ))}
                      </div>
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      <div className="mt-4 flex items-center justify-between">
        <div className="text-sm text-gray-700">
          Page {currentPage + 1} of {totalPages || 1}
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
            disabled={currentPage === 0}
            className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700 disabled:bg-gray-400"
          >
            Previous
          </button>
          <button
            onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
            disabled={currentPage >= totalPages - 1}
            className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700 disabled:bg-gray-400"
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
}
