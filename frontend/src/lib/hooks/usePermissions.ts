import { useAuthStore } from '../store/authStore';
import { Permission } from '../api/security/securityApi';

/**
 * Custom hook for permission-based access control in UI
 */
export const usePermissions = () => {
  const { user } = useAuthStore();

  /**
   * Check if user has ADMIN role
   */
  const isAdmin = (): boolean => {
    if (!user?.roles) return false;
    return user.roles.includes('ADMIN');
  };

  /**
   * Check if user has STAFF role
   */
  const isStaff = (): boolean => {
    if (!user?.roles) return false;
    return user.roles.includes('STAFF');
  };

  /**
   * Check if user has a specific permission
   * ADMIN users always return true
   * STAFF users need to have the permission in their security groups
   */
  const hasPermission = (permission: Permission): boolean => {
    // Admin has all permissions
    if (isAdmin()) return true;
    
    // For STAFF, check permissions from user's security groups
    // Note: This requires the backend to include permissions in the user profile
    // For now, we'll return false for staff (implement after backend integration)
    return false;
  };

  /**
   * Check if user has any of the specified permissions
   */
  const hasAnyPermission = (permissions: Permission[]): boolean => {
    if (isAdmin()) return true;
    
    return permissions.some(permission => hasPermission(permission));
  };

  /**
   * Check if user has all of the specified permissions
   */
  const hasAllPermissions = (permissions: Permission[]): boolean => {
    if (isAdmin()) return true;
    
    return permissions.every(permission => hasPermission(permission));
  };

  /**
   * Check if user can access a route/feature
   * Uses permission if provided, otherwise checks role
   */
  const canAccess = (options: {
    permission?: Permission;
    permissions?: Permission[];
    requireAll?: boolean;
    adminOnly?: boolean;
  }): boolean => {
    const { permission, permissions, requireAll = false, adminOnly = false } = options;

    if (adminOnly) {
      return isAdmin();
    }

    if (permission) {
      return hasPermission(permission);
    }

    if (permissions) {
      return requireAll 
        ? hasAllPermissions(permissions)
        : hasAnyPermission(permissions);
    }

    // If no permission specified, just check if user is authenticated
    return !!user;
  };

  return {
    isAdmin,
    isStaff,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    canAccess,
  };
};
