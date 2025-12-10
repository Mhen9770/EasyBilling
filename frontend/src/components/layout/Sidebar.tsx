'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useState } from 'react';

interface NavItem {
  label: string;
  href: string;
  icon: string;
  children?: NavItem[];
}

const navigationItems: NavItem[] = [
  {
    label: 'Dashboard',
    href: '/dashboard',
    icon: '📊',
  },
  {
    label: 'POS',
    href: '/pos',
    icon: '🛒',
  },
  {
    label: 'Inventory',
    href: '/inventory/products',
    icon: '📦',
    children: [
      { label: 'Products', href: '/inventory/products', icon: '📦' },
      { label: 'Categories', href: '/inventory/categories', icon: '🏷️' },
      { label: 'Brands', href: '/inventory/brands', icon: '⭐' },
      { label: 'Stock', href: '/inventory/stock', icon: '📊' },
    ],
  },
  {
    label: 'Customers',
    href: '/customers',
    icon: '👥',
  },
  {
    label: 'Suppliers',
    href: '/suppliers',
    icon: '🏢',
  },
  {
    label: 'Reports',
    href: '/reports',
    icon: '📈',
  },
  {
    label: 'Users',
    href: '/users',
    icon: '👤',
  },
  {
    label: 'Settings',
    href: '/settings',
    icon: '⚙️',
  },
];

interface SidebarProps {
  collapsed: boolean;
  onToggle: () => void;
}

export function Sidebar({ collapsed, onToggle }: SidebarProps) {
  const pathname = usePathname();
  const [expandedItems, setExpandedItems] = useState<string[]>(['/inventory']);

  const toggleExpanded = (href: string) => {
    setExpandedItems((prev) =>
      prev.includes(href) ? prev.filter((item) => item !== href) : [...prev, href]
    );
  };

  const isActive = (href: string) => {
    return pathname === href || pathname.startsWith(href + '/');
  };

  return (
    <aside
      className={`${
        collapsed ? 'w-20' : 'w-64'
      } bg-gradient-to-b from-blue-900 to-blue-800 text-white transition-all duration-300 flex flex-col h-full fixed left-0 top-0 z-40`}
    >
      {/* Header */}
      <div className="p-4 border-b border-blue-700">
        <div className="flex items-center justify-between">
          {!collapsed && (
            <div className="flex items-center space-x-2">
              <div className="h-8 w-8 bg-gradient-to-br from-blue-400 to-purple-400 rounded-lg" />
              <span className="text-xl font-bold">EasyBilling</span>
            </div>
          )}
          {collapsed && (
            <div className="h-8 w-8 bg-gradient-to-br from-blue-400 to-purple-400 rounded-lg mx-auto" />
          )}
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 overflow-y-auto py-4 px-2">
        <ul className="space-y-1">
          {navigationItems.map((item) => (
            <li key={item.href}>
              {item.children ? (
                <>
                  <button
                    onClick={() => toggleExpanded(item.href)}
                    className={`w-full flex items-center justify-between px-3 py-2 rounded-lg hover:bg-blue-700 transition-colors ${
                      isActive(item.href) ? 'bg-blue-700' : ''
                    }`}
                    title={collapsed ? item.label : undefined}
                  >
                    <div className="flex items-center space-x-3">
                      <span className="text-xl">{item.icon}</span>
                      {!collapsed && (
                        <span className="font-medium">{item.label}</span>
                      )}
                    </div>
                    {!collapsed && (
                      <span className="text-sm">
                        {expandedItems.includes(item.href) ? '▼' : '▶'}
                      </span>
                    )}
                  </button>
                  {!collapsed && expandedItems.includes(item.href) && (
                    <ul className="ml-8 mt-1 space-y-1">
                      {item.children.map((child) => (
                        <li key={child.href}>
                          <Link
                            href={child.href}
                            className={`flex items-center space-x-2 px-3 py-2 rounded-lg hover:bg-blue-700 transition-colors ${
                              isActive(child.href) ? 'bg-blue-600' : ''
                            }`}
                          >
                            <span className="text-lg">{child.icon}</span>
                            <span className="text-sm">{child.label}</span>
                          </Link>
                        </li>
                      ))}
                    </ul>
                  )}
                </>
              ) : (
                <Link
                  href={item.href}
                  className={`flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-blue-700 transition-colors ${
                    isActive(item.href) ? 'bg-blue-700' : ''
                  }`}
                  title={collapsed ? item.label : undefined}
                >
                  <span className="text-xl">{item.icon}</span>
                  {!collapsed && <span className="font-medium">{item.label}</span>}
                </Link>
              )}
            </li>
          ))}
        </ul>
      </nav>

      {/* Toggle Button */}
      <div className="p-4 border-t border-blue-700">
        <button
          onClick={onToggle}
          className="w-full px-3 py-2 bg-blue-700 hover:bg-blue-600 rounded-lg transition-colors flex items-center justify-center space-x-2"
          title={collapsed ? 'Expand Sidebar' : 'Collapse Sidebar'}
        >
          <span className="text-lg">{collapsed ? '▶' : '◀'}</span>
          {!collapsed && <span className="text-sm">Collapse</span>}
        </button>
      </div>
    </aside>
  );
}
