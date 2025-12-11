/**
 * Custom React Hooks
 * 
 * Reusable hooks for common patterns in the application.
 */

import { useState, useEffect, useCallback } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { metadataClient } from '../engine/metadataClient';

/**
 * Hook for fetching and caching form metadata
 */
export function useFormMetadata(formId: string) {
  return useQuery({
    queryKey: ['form-metadata', formId],
    queryFn: () => metadataClient.fetchForm(formId),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
}

/**
 * Hook for fetching and caching list metadata
 */
export function useListMetadata(listId: string) {
  return useQuery({
    queryKey: ['list-metadata', listId],
    queryFn: () => metadataClient.fetchList(listId),
    staleTime: 5 * 60 * 1000,
  });
}

/**
 * Hook for fetching and caching page metadata
 */
export function usePageMetadata(pageId: string) {
  return useQuery({
    queryKey: ['page-metadata', pageId],
    queryFn: () => metadataClient.fetchPage(pageId),
    staleTime: 5 * 60 * 1000,
  });
}

/**
 * Hook for creating an entity
 */
export function useCreateEntity(entity: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: any) => apiClient.createEntity(entity, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['entities', entity] });
    },
  });
}

/**
 * Hook for updating an entity
 */
export function useUpdateEntity(entity: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: any) => apiClient.updateEntity(entity, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['entities', entity] });
    },
  });
}

/**
 * Hook for deleting an entity
 */
export function useDeleteEntity(entity: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => apiClient.deleteEntity(entity, id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['entities', entity] });
    },
  });
}

/**
 * Hook for fetching entities with pagination
 */
export function useEntities(entity: string, params?: Record<string, any>) {
  return useQuery({
    queryKey: ['entities', entity, params],
    queryFn: () => apiClient.findEntities(entity, params),
  });
}

/**
 * Hook for fetching a single entity by ID
 */
export function useEntity(entity: string, id: string | null) {
  return useQuery({
    queryKey: ['entity', entity, id],
    queryFn: () => apiClient.getEntityById(entity, id!),
    enabled: !!id,
  });
}

/**
 * Hook for local storage state
 */
export function useLocalStorage<T>(key: string, initialValue: T) {
  const [storedValue, setStoredValue] = useState<T>(() => {
    try {
      const item = window.localStorage.getItem(key);
      return item ? JSON.parse(item) : initialValue;
    } catch (error) {
      console.error('Error reading from localStorage:', error);
      return initialValue;
    }
  });

  const setValue = (value: T | ((val: T) => T)) => {
    try {
      const valueToStore = value instanceof Function ? value(storedValue) : value;
      setStoredValue(valueToStore);
      window.localStorage.setItem(key, JSON.stringify(valueToStore));
    } catch (error) {
      console.error('Error writing to localStorage:', error);
    }
  };

  return [storedValue, setValue] as const;
}

/**
 * Hook for debounced value
 */
export function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}

/**
 * Hook for online/offline status
 */
export function useOnlineStatus() {
  const [isOnline, setIsOnline] = useState(navigator.onLine);

  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  return isOnline;
}

/**
 * Hook for window size
 */
export function useWindowSize() {
  const [windowSize, setWindowSize] = useState({
    width: window.innerWidth,
    height: window.innerHeight,
  });

  useEffect(() => {
    const handleResize = () => {
      setWindowSize({
        width: window.innerWidth,
        height: window.innerHeight,
      });
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  return windowSize;
}

/**
 * Hook for dark mode
 */
export function useDarkMode() {
  const [isDark, setIsDark] = useLocalStorage('dark-mode', false);

  useEffect(() => {
    if (isDark) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [isDark]);

  return [isDark, setIsDark] as const;
}

/**
 * Hook for click outside
 */
export function useClickOutside(
  ref: React.RefObject<HTMLElement>,
  handler: () => void
) {
  useEffect(() => {
    const listener = (event: MouseEvent | TouchEvent) => {
      if (!ref.current || ref.current.contains(event.target as Node)) {
        return;
      }
      handler();
    };

    document.addEventListener('mousedown', listener);
    document.addEventListener('touchstart', listener);

    return () => {
      document.removeEventListener('mousedown', listener);
      document.removeEventListener('touchstart', listener);
    };
  }, [ref, handler]);
}
