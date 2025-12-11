/**
 * Permission Utilities
 * 
 * Helper functions for checking permissions in the UI.
 */

import { create } from 'zustand';

interface PermissionStore {
  permissions: Set<string>;
  setPermissions: (permissions: string[]) => void;
  addPermission: (permission: string) => void;
  removePermission: (permission: string) => void;
  hasPermission: (permission: string) => boolean;
  hasAnyPermission: (permissions: string[]) => boolean;
  hasAllPermissions: (permissions: string[]) => boolean;
  clear: () => void;
}

export const usePermissionStore = create<PermissionStore>((set, get) => ({
  permissions: new Set(),

  setPermissions: (permissions: string[]) =>
    set({ permissions: new Set(permissions) }),

  addPermission: (permission: string) =>
    set((state) => {
      const newPermissions = new Set(state.permissions);
      newPermissions.add(permission);
      return { permissions: newPermissions };
    }),

  removePermission: (permission: string) =>
    set((state) => {
      const newPermissions = new Set(state.permissions);
      newPermissions.delete(permission);
      return { permissions: newPermissions };
    }),

  hasPermission: (permission: string) => {
    return get().permissions.has(permission);
  },

  hasAnyPermission: (permissions: string[]) => {
    const userPermissions = get().permissions;
    return permissions.some((p) => userPermissions.has(p));
  },

  hasAllPermissions: (permissions: string[]) => {
    const userPermissions = get().permissions;
    return permissions.every((p) => userPermissions.has(p));
  },

  clear: () => set({ permissions: new Set() }),
}));

/**
 * Hook for checking permissions
 */
export function usePermissions() {
  const store = usePermissionStore();

  return {
    hasPermission: store.hasPermission,
    hasAnyPermission: store.hasAnyPermission,
    hasAllPermissions: store.hasAllPermissions,
    permissions: Array.from(store.permissions),
  };
}

/**
 * Component wrapper for permission-based rendering
 */
interface PermissionGateProps {
  permission?: string;
  permissions?: string[];
  requireAll?: boolean;
  fallback?: React.ReactNode;
  children: React.ReactNode;
}

export function PermissionGate({
  permission,
  permissions,
  requireAll = false,
  fallback = null,
  children,
}: PermissionGateProps) {
  const { hasPermission, hasAnyPermission, hasAllPermissions } = usePermissions();

  let hasAccess = true;

  if (permission) {
    hasAccess = hasPermission(permission);
  } else if (permissions) {
    hasAccess = requireAll
      ? hasAllPermissions(permissions)
      : hasAnyPermission(permissions);
  }

  return hasAccess ? <>{children}</> : <>{fallback}</>;
}

/**
 * Parse permission string into parts
 */
export function parsePermission(permission: string): {
  entity?: string;
  action?: string;
} {
  const parts = permission.split(':');
  return {
    entity: parts[0],
    action: parts[1],
  };
}

/**
 * Build permission string
 */
export function buildPermission(entity: string, action: string): string {
  return `${entity}:${action}`;
}

/**
 * Check if user has entity-level permission
 */
export function hasEntityPermission(
  entity: string,
  action: string,
  permissions: string[]
): boolean {
  const permissionString = buildPermission(entity, action);
  return permissions.includes(permissionString);
}
