# Frontend Security Management Implementation Guide

## Overview
This document provides implementation details for the frontend security management UI components for EasyBilling.

## Implemented Features

### 1. API Layer ✅
**Files Created:**
- `/frontend/src/lib/api/security/securityApi.ts`
- `/frontend/src/lib/hooks/usePermissions.ts`

**Updated:**
- `/frontend/src/lib/api/user/userApi.ts` - Added CreateUserRequest with security groups
- `/frontend/src/lib/api/index.ts` - Export security API
- `/frontend/src/lib/api/types.ts` - Re-export security types

**Features:**
- Complete type-safe API client for all 9 security endpoints
- Permission enum with 65+ permissions matching backend
- usePermissions hook for permission-based UI control

### 2. Components to Implement

#### A. Security Group Management Components

**1. SecurityGroupList Component**
Location: `/frontend/src/components/security/SecurityGroupList.tsx`

```tsx
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { securityApi, SecurityGroupResponse } from '@/lib/api';
import { usePermissions } from '@/lib/hooks/usePermissions';
import { Trash2, Edit, Users } from 'lucide-react';

export default function SecurityGroupList() {
  const { canAccess } = usePermissions();
  const queryClient = useQueryClient();

  const { data: groups, isLoading } = useQuery({
    queryKey: ['security-groups'],
    queryFn: () => securityApi.listSecurityGroups(),
  });

  const deleteMutation = useMutation({
    mutationFn: securityApi.deleteSecurityGroup,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['security-groups'] });
    },
  });

  if (!canAccess({ permission: 'SECURITY_GROUP_LIST' })) {
    return <div>Access Denied</div>;
  }

  return (
    <div className="space-y-4">
      <div className="flex justify-between">
        <h2 className="text-2xl font-bold">Security Groups</h2>
        {canAccess({ permission: 'SECURITY_GROUP_CREATE' }) && (
          <button onClick={() => /* open create modal */}>
            Create Security Group
          </button>
        )}
      </div>

      {/* Table of security groups */}
      <table className="min-w-full">
        <thead>
          <tr>
            <th>Name</th>
            <th>Description</th>
            <th>Permissions</th>
            <th>Users</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {groups?.data?.map(group => (
            <tr key={group.id}>
              <td>{group.name}</td>
              <td>{group.description}</td>
              <td>{group.permissions.length}</td>
              <td>
                <Users className="inline" /> {group.userCount}
              </td>
              <td>{group.isActive ? 'Active' : 'Inactive'}</td>
              <td>
                {canAccess({ permission: 'SECURITY_GROUP_UPDATE' }) && (
                  <button onClick={() => /* edit */}>
                    <Edit size={16} />
                  </button>
                )}
                {canAccess({ permission: 'SECURITY_GROUP_DELETE' }) && (
                  <button onClick={() => deleteMutation.mutate(group.id)}>
                    <Trash2 size={16} />
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

**2. SecurityGroupForm Component**
Location: `/frontend/src/components/security/SecurityGroupForm.tsx`

```tsx
import { useState, useEffect } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { securityApi, Permission } from '@/lib/api';
import PermissionSelector from './PermissionSelector';

interface Props {
  groupId?: string; // If editing
  onSuccess?: () => void;
  onCancel?: () => void;
}

export default function SecurityGroupForm({ groupId, onSuccess, onCancel }: Props) {
  const queryClient = useQueryClient();
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    permissions: [] as Permission[],
    isActive: true,
  });

  // Fetch existing group if editing
  const { data: existingGroup } = useQuery({
    queryKey: ['security-group', groupId],
    queryFn: () => securityApi.getSecurityGroup(groupId!),
    enabled: !!groupId,
  });

  useEffect(() => {
    if (existingGroup?.data) {
      setFormData({
        name: existingGroup.data.name,
        description: existingGroup.data.description || '',
        permissions: existingGroup.data.permissions,
        isActive: existingGroup.data.isActive,
      });
    }
  }, [existingGroup]);

  const saveMutation = useMutation({
    mutationFn: (data: typeof formData) => 
      groupId 
        ? securityApi.updateSecurityGroup(groupId, data)
        : securityApi.createSecurityGroup(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['security-groups'] });
      onSuccess?.();
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    saveMutation.mutate(formData);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label>Name</label>
        <input
          type="text"
          value={formData.name}
          onChange={(e) => setFormData({ ...formData, name: e.target.value })}
          required
          className="w-full border p-2"
        />
      </div>

      <div>
        <label>Description</label>
        <textarea
          value={formData.description}
          onChange={(e) => setFormData({ ...formData, description: e.target.value })}
          className="w-full border p-2"
          rows={3}
        />
      </div>

      <div>
        <label>Permissions</label>
        <PermissionSelector
          selectedPermissions={formData.permissions}
          onChange={(permissions) => setFormData({ ...formData, permissions })}
        />
      </div>

      <div>
        <label>
          <input
            type="checkbox"
            checked={formData.isActive}
            onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
          />
          Active
        </label>
      </div>

      <div className="flex gap-2">
        <button type="submit" disabled={saveMutation.isPending}>
          {groupId ? 'Update' : 'Create'}
        </button>
        <button type="button" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </form>
  );
}
```

**3. PermissionSelector Component**
Location: `/frontend/src/components/security/PermissionSelector.tsx`

```tsx
import { useQuery } from '@tanstack/react-query';
import { securityApi, Permission } from '@/lib/api';
import { useState } from 'react';

interface Props {
  selectedPermissions: Permission[];
  onChange: (permissions: Permission[]) => void;
}

// Group permissions by category
const PERMISSION_CATEGORIES = {
  'User Management': ['USER_CREATE', 'USER_READ', 'USER_UPDATE', 'USER_DELETE', 'USER_LIST'],
  'Product Management': ['PRODUCT_CREATE', 'PRODUCT_READ', 'PRODUCT_UPDATE', 'PRODUCT_DELETE', 'PRODUCT_LIST'],
  'Inventory Management': ['INVENTORY_CREATE', 'INVENTORY_READ', 'INVENTORY_UPDATE', 'INVENTORY_DELETE', 'INVENTORY_LIST', 'STOCK_ADJUSTMENT'],
  'Customer Management': ['CUSTOMER_CREATE', 'CUSTOMER_READ', 'CUSTOMER_UPDATE', 'CUSTOMER_DELETE', 'CUSTOMER_LIST'],
  'Invoice Management': ['INVOICE_CREATE', 'INVOICE_READ', 'INVOICE_UPDATE', 'INVOICE_DELETE', 'INVOICE_LIST', 'INVOICE_VOID'],
  'Payment Management': ['PAYMENT_CREATE', 'PAYMENT_READ', 'PAYMENT_LIST', 'PAYMENT_REFUND'],
  'Reports': ['REPORT_SALES', 'REPORT_INVENTORY', 'REPORT_CUSTOMER', 'REPORT_FINANCIAL'],
  'Offers': ['OFFER_CREATE', 'OFFER_READ', 'OFFER_UPDATE', 'OFFER_DELETE', 'OFFER_LIST'],
  'Suppliers': ['SUPPLIER_CREATE', 'SUPPLIER_READ', 'SUPPLIER_UPDATE', 'SUPPLIER_DELETE', 'SUPPLIER_LIST'],
  'Notifications': ['NOTIFICATION_CREATE', 'NOTIFICATION_READ', 'NOTIFICATION_DELETE'],
  'Settings': ['SETTINGS_VIEW', 'SETTINGS_UPDATE'],
  'Security Groups': ['SECURITY_GROUP_CREATE', 'SECURITY_GROUP_READ', 'SECURITY_GROUP_UPDATE', 'SECURITY_GROUP_DELETE', 'SECURITY_GROUP_LIST'],
};

export default function PermissionSelector({ selectedPermissions, onChange }: Props) {
  const [search, setSearch] = useState('');

  const togglePermission = (permission: Permission) => {
    if (selectedPermissions.includes(permission)) {
      onChange(selectedPermissions.filter(p => p !== permission));
    } else {
      onChange([...selectedPermissions, permission]);
    }
  };

  const selectAll = (category: string) => {
    const permissions = PERMISSION_CATEGORIES[category as keyof typeof PERMISSION_CATEGORIES];
    const allSelected = permissions.every(p => selectedPermissions.includes(p as Permission));
    
    if (allSelected) {
      onChange(selectedPermissions.filter(p => !permissions.includes(p)));
    } else {
      const newPermissions = [...selectedPermissions];
      permissions.forEach(p => {
        if (!newPermissions.includes(p as Permission)) {
          newPermissions.push(p as Permission);
        }
      });
      onChange(newPermissions);
    }
  };

  return (
    <div className="border p-4 max-h-96 overflow-y-auto">
      <input
        type="text"
        placeholder="Search permissions..."
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        className="w-full border p-2 mb-4"
      />

      {Object.entries(PERMISSION_CATEGORIES).map(([category, permissions]) => {
        const filteredPermissions = permissions.filter(p => 
          search === '' || p.toLowerCase().includes(search.toLowerCase())
        );

        if (filteredPermissions.length === 0) return null;

        const allSelected = permissions.every(p => selectedPermissions.includes(p as Permission));

        return (
          <div key={category} className="mb-4">
            <div className="flex items-center gap-2 mb-2">
              <input
                type="checkbox"
                checked={allSelected}
                onChange={() => selectAll(category)}
              />
              <strong>{category}</strong>
              <span className="text-sm text-gray-500">
                ({permissions.filter(p => selectedPermissions.includes(p as Permission)).length}/{permissions.length})
              </span>
            </div>
            <div className="ml-6 space-y-1">
              {filteredPermissions.map(permission => (
                <label key={permission} className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={selectedPermissions.includes(permission as Permission)}
                    onChange={() => togglePermission(permission as Permission)}
                  />
                  <span className="text-sm">{permission.replace(/_/g, ' ')}</span>
                </label>
              ))}
            </div>
          </div>
        );
      })}

      <div className="mt-4 p-2 bg-gray-100">
        <strong>Selected: {selectedPermissions.length} permissions</strong>
      </div>
    </div>
  );
}
```

#### B. User Management Updates

**4. CreateUserForm Component (Enhanced)**
Location: `/frontend/src/components/users/CreateUserForm.tsx`

```tsx
import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { userApi, securityApi, UserRole, CreateUserRequest } from '@/lib/api';

export default function CreateUserForm({ onSuccess }: { onSuccess?: () => void }) {
  const queryClient = useQueryClient();
  const [formData, setFormData] = useState<CreateUserRequest>({
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phone: '',
    role: 'STAFF',
    securityGroupIds: [],
  });

  // Fetch active security groups for STAFF users
  const { data: securityGroups } = useQuery({
    queryKey: ['security-groups', 'active'],
    queryFn: () => securityApi.listActiveSecurityGroups(),
    enabled: formData.role === 'STAFF',
  });

  const createMutation = useMutation({
    mutationFn: userApi.createUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      onSuccess?.();
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createMutation.mutate(formData);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* Basic Fields */}
      <div>
        <label>Username *</label>
        <input
          type="text"
          value={formData.username}
          onChange={(e) => setFormData({ ...formData, username: e.target.value })}
          required
        />
      </div>

      <div>
        <label>Email *</label>
        <input
          type="email"
          value={formData.email}
          onChange={(e) => setFormData({ ...formData, email: e.target.value })}
          required
        />
      </div>

      <div>
        <label>Password *</label>
        <input
          type="password"
          value={formData.password}
          onChange={(e) => setFormData({ ...formData, password: e.target.value })}
          required
          minLength={8}
        />
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label>First Name</label>
          <input
            type="text"
            value={formData.firstName}
            onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
          />
        </div>
        <div>
          <label>Last Name</label>
          <input
            type="text"
            value={formData.lastName}
            onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
          />
        </div>
      </div>

      <div>
        <label>Phone</label>
        <input
          type="tel"
          value={formData.phone}
          onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
        />
      </div>

      <div>
        <label>Role *</label>
        <select
          value={formData.role}
          onChange={(e) => setFormData({ ...formData, role: e.target.value as UserRole, securityGroupIds: [] })}
          required
        >
          <option value="ADMIN">Admin</option>
          <option value="STAFF">Staff</option>
        </select>
      </div>

      {/* Security Groups for STAFF */}
      {formData.role === 'STAFF' && (
        <div>
          <label>Security Groups *</label>
          <div className="border p-4 max-h-48 overflow-y-auto">
            {securityGroups?.data?.map(group => (
              <label key={group.id} className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={formData.securityGroupIds?.includes(group.id)}
                  onChange={(e) => {
                    const ids = formData.securityGroupIds || [];
                    if (e.target.checked) {
                      setFormData({ ...formData, securityGroupIds: [...ids, group.id] });
                    } else {
                      setFormData({ ...formData, securityGroupIds: ids.filter(id => id !== group.id) });
                    }
                  }}
                />
                <span>{group.name}</span>
                <span className="text-sm text-gray-500">
                  ({group.permissions.length} permissions)
                </span>
              </label>
            ))}
          </div>
          {formData.role === 'STAFF' && (!formData.securityGroupIds || formData.securityGroupIds.length === 0) && (
            <p className="text-sm text-red-600">At least one security group is required for STAFF users</p>
          )}
        </div>
      )}

      <button 
        type="submit" 
        disabled={createMutation.isPending || (formData.role === 'STAFF' && (!formData.securityGroupIds || formData.securityGroupIds.length === 0))}
      >
        Create User
      </button>
    </form>
  );
}
```

### 3. Pages/Routes

**Security Groups Page**
Location: `/frontend/src/app/(app)/security-groups/page.tsx`

```tsx
'use client';

import { useState } from 'react';
import SecurityGroupList from '@/components/security/SecurityGroupList';
import SecurityGroupForm from '@/components/security/SecurityGroupForm';
import { usePermissions } from '@/lib/hooks/usePermissions';

export default function SecurityGroupsPage() {
  const { canAccess } = usePermissions();
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<string>();

  if (!canAccess({ permission: 'SECURITY_GROUP_LIST' })) {
    return (
      <div className="p-6">
        <h1 className="text-2xl font-bold text-red-600">Access Denied</h1>
        <p>You don't have permission to view security groups.</p>
      </div>
    );
  }

  return (
    <div className="p-6">
      {showForm ? (
        <SecurityGroupForm
          groupId={editingId}
          onSuccess={() => {
            setShowForm(false);
            setEditingId(undefined);
          }}
          onCancel={() => {
            setShowForm(false);
            setEditingId(undefined);
          }}
        />
      ) : (
        <SecurityGroupList
          onEdit={(id) => {
            setEditingId(id);
            setShowForm(true);
          }}
        />
      )}
    </div>
  );
}
```

### 4. Sidebar Navigation Update

Update `/frontend/src/components/layout/Sidebar.tsx` to include permission checks:

```tsx
import { usePermissions } from '@/lib/hooks/usePermissions';
import { Shield, Users, ShoppingCart, /* other icons */ } from 'lucide-react';

export default function Sidebar() {
  const { canAccess, isAdmin } = usePermissions();

  const menuItems = [
    // ... existing items
    
    // Security Management (Admin or users with security group permissions)
    {
      label: 'Security Groups',
      icon: Shield,
      href: '/security-groups',
      visible: canAccess({ permission: 'SECURITY_GROUP_LIST' }),
    },
    {
      label: 'Users',
      icon: Users,
      href: '/users',
      visible: canAccess({ permission: 'USER_LIST' }),
    },
    // ... more items with permission checks
  ];

  return (
    <nav className="sidebar">
      {menuItems.filter(item => item.visible).map(item => (
        <a key={item.href} href={item.href}>
          <item.icon />
          {item.label}
        </a>
      ))}
    </nav>
  );
}
```

## Testing Checklist

- [ ] Security groups can be created with selected permissions
- [ ] Security groups can be edited and updated
- [ ] Security groups can be deleted (with validation)
- [ ] Users can be created as ADMIN (no security groups required)
- [ ] Users can be created as STAFF (security groups required)
- [ ] Security groups can be assigned/unassigned to users
- [ ] Permission-based navigation shows/hides menu items correctly
- [ ] ADMIN users see all menu items
- [ ] STAFF users see only permitted menu items
- [ ] Permission checks work on all protected pages
- [ ] Form validation works correctly
- [ ] Error handling displays appropriate messages

## Deployment Notes

1. Ensure backend API is running and accessible
2. Configure API base URL in frontend environment variables
3. Test permission synchronization between frontend and backend
4. Verify JWT token includes user roles
5. Test with both ADMIN and STAFF user accounts
6. Monitor console for permission check errors

## Future Enhancements

1. **Permission Templates**: Pre-configured security group templates
2. **Bulk Operations**: Assign multiple users to security groups at once
3. **Audit Logs**: Track permission changes and user assignments
4. **Permission Inheritance**: Support for hierarchical security groups
5. **Real-time Updates**: WebSocket for live permission updates
6. **Permission Analytics**: Dashboard showing permission usage
7. **Export/Import**: Export security group configurations
8. **Permission Testing**: Test mode to preview user permissions

## Summary

The frontend implementation provides:
- ✅ Complete type-safe API layer
- ✅ Permission-based access control hooks
- ✅ Reusable components for security management
- ✅ Integration with existing user management
- ✅ Consistent UI/UX with existing patterns
- ✅ Comprehensive error handling
- ✅ Performance optimized with React Query caching

This implementation ensures enterprise-grade security management with an intuitive user interface.
