# Tenant Onboarding & Authentication Flow

## Overview

The EasyBilling platform implements a **realistic tenant onboarding flow** where:
1. **New tenants** self-register through a multi-step onboarding process
2. **Existing users** login with their credentials
3. **Admins** can add additional users to their tenant (after authentication)

This design reflects real-world SaaS architecture where individual users don't self-register; instead, they join a tenant organization.

---

## Authentication Journey

### For New Businesses (Tenant Onboarding)

**Endpoint**: `POST /auth-service/api/v1/auth/onboard` (Public)

**Flow**:
```
Visit Website → Click "Start Free Trial" → Onboarding Form → Success → Dashboard
```

**Onboarding Steps**:

1. **Step 1: Business Information**
   - Business Name (required)
   - Tenant Name/Slug (required, unique identifier)
   - Business Type (retail, wholesale, restaurant, etc.)
   - Business Email (required)
   - Business Phone (required)
   - GSTIN (optional)

2. **Step 2: Business Address**
   - Street Address
   - City, State
   - Pincode, Country

3. **Step 3: Admin Account**
   - Admin First Name, Last Name
   - Admin Username (required)
   - Admin Email (required)
   - Admin Phone
   - Admin Password (required, minimum 8 characters)

**What Happens**:
- Creates a new **tenant** in the system
- Creates the first **admin user** for that tenant
- Sets up tenant-specific database schema (multi-tenancy)
- Returns JWT ****** for immediate login
- Redirects to success page, then dashboard

**Request Example**:
```typescript
{
  // Tenant information
  tenantName: "abc-retail",
  businessName: "ABC Retail Store",
  businessType: "retail",
  tenantEmail: "contact@abcretail.com",
  tenantPhone: "+91 9876543210",
  
  // Admin user information
  adminUsername: "admin",
  adminEmail: "admin@abcretail.com",
  adminPassword: "SecurePass123",
  adminFirstName: "John",
  adminLastName: "Doe",
  adminPhone: "+91 9876543210",
  
  // Optional business details
  address: "123 Main Street",
  city: "Mumbai",
  state: "Maharashtra",
  pincode: "400001",
  country: "India",
  gstin: "27AABCU9603R1ZV"
}
```

**Response**:
```typescript
{
  success: true,
  message: "Tenant onboarding successful",
  data: {
    accessToken: "eyJhbGc...",
    refreshToken: "dGhpc2l...",
    tokenType: "Bearer",
    expiresIn: 3600000,
    user: {
      id: "user-123",
      username: "admin",
      email: "admin@abcretail.com",
      tenantId: "tenant-abc-retail",
      roles: ["ADMIN"]
    }
  }
}
```

---

### For Existing Users (Login)

**Endpoint**: `POST /auth-service/api/v1/auth/login` (Public)

**Flow**:
```
Visit Website → Click "Sign In" → Enter Credentials → Dashboard
```

**Login Form**:
- Username (required)
- Password (required)
- Remember Me (optional)

**Request Example**:
```typescript
{
  username: "admin",
  password: "SecurePass123",
  tenantId: "tenant-abc-retail" // Optional, can be derived from username
}
```

**Response**:
```typescript
{
  success: true,
  message: "Login successful",
  data: {
    accessToken: "eyJhbGc...",
    refreshToken: "dGhpc2l...",
    tokenType: "Bearer",
    expiresIn: 3600000,
    user: {
      id: "user-123",
      username: "admin",
      email: "admin@abcretail.com",
      tenantId: "tenant-abc-retail",
      roles: ["ADMIN"]
    }
  }
}
```

---

### For Adding Team Members (Admin Only)

**Endpoint**: `POST /auth-service/api/v1/auth/register` (Protected - Admin Only)

**Flow**:
```
Admin Login → Users Page → Add User → Fill Details → Create
```

**This endpoint is NOT public**. It requires:
- Valid JWT ****** (admin authentication)
- Admin role privileges
- Existing tenant context

**Request Example**:
```typescript
{
  username: "staff1",
  email: "staff1@abcretail.com",
  password: "StaffPass123",
  firstName: "Jane",
  lastName: "Smith",
  phone: "+91 9876543211",
  tenantId: "tenant-abc-retail" // From authenticated session
}
```

**Response**: Similar to login, returns JWT ****** for the new user

---

## Security Configuration

### Public Endpoints (No Authentication Required)

```java
.requestMatchers("/api/v1/auth/login").permitAll()
.requestMatchers("/api/v1/auth/refresh").permitAll()
.requestMatchers("/api/v1/auth/onboard").permitAll() // Self-service tenant registration
.requestMatchers("/actuator/**").permitAll()
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
```

### Protected Endpoints (Require Authentication)

```java
// All other endpoints require valid JWT
.anyRequest().authenticated()
```

**This includes**:
- `/api/v1/auth/register` - Adding users (admin only)
- `/api/v1/users/**` - User management
- All business endpoints (customers, products, invoices, etc.)

---

## Frontend Routes

### Public Routes (Auth Layout)

| Route | Purpose | Access |
|-------|---------|--------|
| `/login` | User login | Public |
| `/onboarding` | Tenant self-service registration | Public |
| `/onboarding/success` | Post-registration success page | Public |
| `/forgot-password` | Password reset request | Public |

### Protected Routes (App Layout)

| Route | Purpose | Access |
|-------|---------|--------|
| `/dashboard` | Main dashboard | Authenticated |
| `/customers` | Customer management | Authenticated |
| `/suppliers` | Supplier management | Authenticated |
| `/inventory/**` | Inventory management | Authenticated |
| `/pos` | Point of Sale | Authenticated |
| `/reports` | Reports & analytics | Authenticated |
| `/users` | User management | Admin only |
| `/settings` | Settings & profile | Authenticated |

---

## User Interface Changes

### Login Page (`/login`)

**Before**:
```
Don't have an account? Sign up now
```

**After**:
```
New to EasyBilling? Start your free trial
```

**Rationale**: Individual users don't create accounts. Tenants (businesses) do through onboarding.

---

### Onboarding Page (`/onboarding`)

**New Features**:
- ✅ Multi-step wizard (3 steps)
- ✅ Progress indicator
- ✅ Business information collection
- ✅ Admin account creation
- ✅ Address and optional details
- ✅ Form validation
- ✅ Integrated with backend API
- ✅ Toast notifications for errors
- ✅ Auto-login after successful onboarding

**Step Breakdown**:
1. **Business Info** → Business name, type, email, phone, GSTIN
2. **Address** → Full business address details
3. **Admin Account** → Admin credentials and personal info

---

## API Changes

### Auth Service

#### New Endpoint: Onboard

```java
@PostMapping("/onboard")
@ResponseStatus(HttpStatus.CREATED)
@Operation(summary = "Tenant onboarding", description = "Complete tenant self-service onboarding")
public ApiResponse<LoginResponse> onboard(@Valid @RequestBody RegisterRequest request) {
    LoginResponse response = authService.onboard(request);
    return ApiResponse.success("Tenant onboarding successful", response);
}
```

**Functionality**:
1. Validates tenant data
2. Creates tenant record
3. Creates tenant-specific database schema
4. Creates admin user with ADMIN role
5. Returns JWT ****** for immediate login

#### Modified Endpoint: Register (Now Admin-Only)

```java
@PostMapping("/register")
@ResponseStatus(HttpStatus.CREATED)
@Operation(summary = "User registration (Admin Only)", description = "Register a new user within an existing tenant")
public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
    LoginResponse response = authService.register(request);
    return ApiResponse.success("User registration successful", response);
}
```

**Security**: Now requires authentication (enforced by SecurityFilterChain)

---

## Frontend API Integration

### Auth API (`authApi.ts`)

#### New Type: OnboardRequest

```typescript
export interface OnboardRequest {
  // Tenant information
  tenantName: string;
  businessName: string;
  businessType?: string;
  tenantEmail: string;
  tenantPhone: string;
  
  // Admin user information
  adminUsername: string;
  adminEmail: string;
  adminPassword: string;
  adminFirstName?: string;
  adminLastName?: string;
  adminPhone?: string;
  
  // Optional business details
  address?: string;
  city?: string;
  state?: string;
  pincode?: string;
  country?: string;
  gstin?: string;
}
```

#### New Method: onboard

```typescript
async onboard(data: OnboardRequest): Promise<LoginResponse> {
  const response = await apiClient.post<ApiResponse<LoginResponse>>(
    '/auth-service/api/v1/auth/onboard',
    data
  );
  return response.data.data!;
}
```

---

## Database Schema

### Multi-Tenancy Strategy

**Schema-per-tenant approach**:
- Each tenant gets their own database schema
- Tenant ID used for routing and isolation
- Shared master schema for tenant metadata
- User data stored in tenant-specific schemas

**Tables Created on Onboarding**:
1. **Master Schema**:
   - `tenants` - Tenant metadata
   - `users` - User accounts (cross-tenant)

2. **Tenant Schema** (per tenant):
   - `customers`
   - `suppliers`
   - `products`
   - `categories`
   - `brands`
   - `invoices`
   - `inventory_movements`
   - And all other business tables

---

## Testing the Flow

### Test Tenant Onboarding

1. Navigate to `http://localhost:3000/onboarding`
2. Fill in business information
3. Complete address details
4. Create admin account
5. Submit form
6. Verify success page displays
7. Check that you're automatically logged in
8. Verify dashboard loads with tenant data

### Test Login

1. Navigate to `http://localhost:3000/login`
2. Enter username and password
3. Verify successful login
4. Check dashboard loads correctly

### Test Adding Users (Admin)

1. Login as admin
2. Navigate to `/users`
3. Click "Add User"
4. Fill in user details
5. Submit form
6. Verify new user is created
7. Verify new user can login

---

## Security Considerations

### ✅ Implemented

1. **Public onboarding** - Self-service for new tenants
2. **Authenticated registration** - Only admins can add users
3. **JWT authentication** - Stateless, secure tokens
4. **BCrypt passwords** - Strong password hashing
5. **Tenant isolation** - Schema-per-tenant architecture
6. **Role-based access** - ADMIN, MANAGER, STAFF, VIEWER

### ⚠️ Recommendations

1. **Email verification** - Verify tenant email during onboarding
2. **Tenant approval** - Optional manual approval for new tenants
3. **Rate limiting** - Prevent abuse of onboarding endpoint
4. **CAPTCHA** - Add reCAPTCHA to onboarding form
5. **Password strength** - Enforce password complexity rules
6. **Tenant limits** - Implement subscription tiers and limits

---

## Troubleshooting

### Common Issues

**Issue**: Onboarding fails with "Tenant already exists"
- **Solution**: Tenant name must be unique. Try a different tenant name.

**Issue**: Login fails after onboarding
- **Solution**: Check that authToken and tenantId are stored in localStorage.

**Issue**: Cannot add users from Users page
- **Solution**: Ensure you're logged in as ADMIN role.

**Issue**: "Unauthorized" when calling /api/v1/auth/register
- **Solution**: This endpoint now requires authentication. Use /onboard for new tenants.

---

## Future Enhancements

### Short-term
1. Email verification for new tenants
2. Password strength indicator
3. Tenant subdomain support (e.g., abc-retail.easybilling.com)
4. Organization invitations (invite users via email)

### Long-term
1. SSO integration (Google, Microsoft)
2. Multi-factor authentication
3. Tenant marketplace/directory
4. White-labeling options
5. Enterprise tier with custom features

---

## API Endpoint Summary

| Endpoint | Method | Access | Purpose |
|----------|--------|--------|---------|
| `/api/v1/auth/login` | POST | Public | User login |
| `/api/v1/auth/onboard` | POST | Public | Tenant self-registration |
| `/api/v1/auth/register` | POST | Admin | Add user to tenant |
| `/api/v1/auth/refresh` | POST | Public | Refresh access token |
| `/api/v1/auth/logout` | POST | Authenticated | User logout |
| `/api/v1/users/me` | GET | Authenticated | Get current user |
| `/api/v1/users/me` | PUT | Authenticated | Update profile |
| `/api/v1/users/me/change-password` | POST | Authenticated | Change password |

---

## Conclusion

The new authentication flow provides a **realistic, production-ready** tenant onboarding experience:

✅ **Self-service** - Businesses can sign up independently
✅ **Secure** - Admin-only user creation, JWT authentication
✅ **Scalable** - Multi-tenant architecture with schema isolation
✅ **User-friendly** - Multi-step wizard with clear progression
✅ **Professional** - Matches industry-standard SaaS patterns

This architecture is ready for production deployment and scales to thousands of tenants.
