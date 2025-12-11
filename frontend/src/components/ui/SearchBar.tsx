'use client';

import React, { useState } from 'react';
import { Modal, Button } from './';

export interface SearchBarProps {
  placeholder?: string;
  onSearch: (query: string) => void;
  suggestions?: Array<{ id: string; label: string; subtitle?: string }>;
  onSelect?: (item: any) => void;
  loading?: boolean;
  icon?: React.ReactNode;
  shortcut?: string;
}

export const SearchBar: React.FC<SearchBarProps> = ({
  placeholder = 'Search...',
  onSearch,
  suggestions = [],
  onSelect,
  loading = false,
  icon,
  shortcut,
}) => {
  const [query, setQuery] = useState('');
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(0);

  const handleChange = (value: string) => {
    setQuery(value);
    onSearch(value);
    setShowSuggestions(value.length > 0);
    setSelectedIndex(0);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setSelectedIndex((prev) => Math.min(prev + 1, suggestions.length - 1));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setSelectedIndex((prev) => Math.max(prev - 1, 0));
    } else if (e.key === 'Enter' && suggestions[selectedIndex]) {
      e.preventDefault();
      handleSelect(suggestions[selectedIndex]);
    } else if (e.key === 'Escape') {
      setShowSuggestions(false);
    }
  };

  const handleSelect = (item: any) => {
    if (onSelect) {
      onSelect(item);
    }
    setQuery('');
    setShowSuggestions(false);
  };

  return (
    <div className="relative w-full">
      <div className="relative">
        {icon && (
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
            {icon}
          </div>
        )}
        <input
          type="text"
          value={query}
          onChange={(e) => handleChange(e.target.value)}
          onKeyDown={handleKeyDown}
          onFocus={() => query.length > 0 && setShowSuggestions(true)}
          placeholder={placeholder}
          className={`block w-full ${icon ? 'pl-10' : 'pl-4'} pr-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-base`}
        />
        {shortcut && (
          <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
            <kbd className="px-2 py-1 bg-gray-100 border border-gray-300 rounded text-xs font-mono">
              {shortcut}
            </kbd>
          </div>
        )}
      </div>

      {showSuggestions && suggestions.length > 0 && (
        <div className="absolute z-50 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-xl max-h-96 overflow-y-auto">
          {suggestions.map((item, index) => (
            <button
              key={item.id}
              onClick={() => handleSelect(item)}
              className={`w-full px-4 py-3 text-left hover:bg-blue-50 border-b border-gray-100 last:border-0 ${
                index === selectedIndex ? 'bg-blue-50' : ''
              }`}
            >
              <div className="font-medium text-gray-900">{item.label}</div>
              {item.subtitle && (
                <div className="text-sm text-gray-500">{item.subtitle}</div>
              )}
            </button>
          ))}
        </div>
      )}

      {loading && (
        <div className="absolute inset-y-0 right-0 pr-3 flex items-center">
          <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600"></div>
        </div>
      )}
    </div>
  );
};
