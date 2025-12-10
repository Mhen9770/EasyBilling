'use client';

import React from 'react';

interface LoaderProps {
  size?: 'sm' | 'md' | 'lg' | 'xl';
  variant?: 'spinner' | 'dots' | 'pulse' | 'bars' | 'ring';
  className?: string;
  text?: string;
}

export function Loader({ 
  size = 'md', 
  variant = 'spinner', 
  className = '',
  text 
}: LoaderProps) {
  const sizeClasses = {
    sm: 'h-4 w-4',
    md: 'h-8 w-8',
    lg: 'h-12 w-12',
    xl: 'h-16 w-16',
  };

  const textSizeClasses = {
    sm: 'text-xs',
    md: 'text-sm',
    lg: 'text-base',
    xl: 'text-lg',
  };

  const renderLoader = () => {
    switch (variant) {
      case 'spinner':
        return (
          <div className={`${sizeClasses[size]} ${className}`}>
            <div className="animate-spin rounded-full border-4 border-gray-200 border-t-blue-600"></div>
          </div>
        );
      
      case 'dots':
        return (
          <div className={`flex gap-1 ${className}`}>
            <div className={`${sizeClasses[size]} rounded-full bg-blue-600 animate-bounce`} style={{ animationDelay: '0ms' }}></div>
            <div className={`${sizeClasses[size]} rounded-full bg-blue-600 animate-bounce`} style={{ animationDelay: '150ms' }}></div>
            <div className={`${sizeClasses[size]} rounded-full bg-blue-600 animate-bounce`} style={{ animationDelay: '300ms' }}></div>
          </div>
        );
      
      case 'pulse':
        return (
          <div className={`${sizeClasses[size]} ${className}`}>
            <div className="rounded-full bg-blue-600 animate-ping h-full w-full"></div>
          </div>
        );
      
      case 'bars':
        return (
          <div className={`flex gap-1 items-end ${className}`}>
            <div className="w-1 bg-blue-600 animate-pulse" style={{ height: '40%', animationDelay: '0ms' }}></div>
            <div className="w-1 bg-blue-600 animate-pulse" style={{ height: '60%', animationDelay: '150ms' }}></div>
            <div className="w-1 bg-blue-600 animate-pulse" style={{ height: '80%', animationDelay: '300ms' }}></div>
            <div className="w-1 bg-blue-600 animate-pulse" style={{ height: '100%', animationDelay: '450ms' }}></div>
            <div className="w-1 bg-blue-600 animate-pulse" style={{ height: '80%', animationDelay: '600ms' }}></div>
            <div className="w-1 bg-blue-600 animate-pulse" style={{ height: '60%', animationDelay: '750ms' }}></div>
            <div className="w-1 bg-blue-600 animate-pulse" style={{ height: '40%', animationDelay: '900ms' }}></div>
          </div>
        );
      
      case 'ring':
        return (
          <div className={`${sizeClasses[size]} ${className} relative`}>
            <div className="absolute inset-0 rounded-full border-4 border-transparent border-t-blue-600 animate-spin"></div>
            <div className="absolute inset-2 rounded-full border-4 border-transparent border-r-purple-600 animate-spin" style={{ animationDirection: 'reverse', animationDuration: '0.8s' }}></div>
          </div>
        );
      
      default:
        return (
          <div className={`${sizeClasses[size]} ${className}`}>
            <div className="animate-spin rounded-full border-4 border-gray-200 border-t-blue-600"></div>
          </div>
        );
    }
  };

  if (text) {
    return (
      <div className="flex flex-col items-center justify-center gap-3">
        {renderLoader()}
        <p className={`${textSizeClasses[size]} text-gray-600 animate-pulse`}>{text}</p>
      </div>
    );
  }

  return renderLoader();
}

export function PageLoader({ text = 'Loading...' }: { text?: string }) {
  return (
    <div className="flex items-center justify-center min-h-[60vh]">
      <Loader size="xl" variant="ring" text={text} />
    </div>
  );
}

export function ButtonLoader({ size = 'sm' }: { size?: 'sm' | 'md' | 'lg' }) {
  return (
    <Loader size={size} variant="spinner" className="text-white" />
  );
}

export function InlineLoader({ text }: { text?: string }) {
  return (
    <div className="flex items-center gap-2 py-2">
      <Loader size="sm" variant="dots" />
      {text && <span className="text-sm text-gray-600">{text}</span>}
    </div>
  );
}

export function TableLoader() {
  return (
    <div className="flex items-center justify-center py-12">
      <Loader size="lg" variant="bars" text="Loading data..." />
    </div>
  );
}

export function CardLoader() {
  return (
    <div className="bg-white rounded-xl shadow-md p-6 animate-pulse">
      <div className="h-4 bg-gray-200 rounded w-3/4 mb-4"></div>
      <div className="h-4 bg-gray-200 rounded w-1/2 mb-2"></div>
      <div className="h-4 bg-gray-200 rounded w-2/3"></div>
    </div>
  );
}

export function SkeletonLoader({ lines = 3, className = '' }: { lines?: number; className?: string }) {
  return (
    <div className={`space-y-3 ${className}`}>
      {Array.from({ length: lines }).map((_, i) => (
        <div key={i} className="h-4 bg-gray-200 rounded animate-pulse" style={{ width: `${100 - i * 10}%` }}></div>
      ))}
    </div>
  );
}

