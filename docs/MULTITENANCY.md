# Multi-Tenancy Implementation

## Overview

This application uses a **Schema-per-Tenant** strategy for multi-tenancy, where each tenant gets their own database schema with complete data isolation. Tables are automatically created by JPA/Hibernate, and master data is inserted programmatically.

## Architecture

### Master Database (easybilling)
The master database contains:
- `tenants` table - Registry of all tenants
- `users` table - Global/platform user accounts (super admins, support staff)
- Flyway migration history for master schema

### Tenant Schemas (tenant_*)
Each tenant gets a dedicated schema (e.g., `tenant_acme`, `tenant_demo`) containing:
- All business tables (users, products, invoices, customers, etc.)
- Complete data isolation from other tenants
- Tables automatically created by JPA/Hibernate
- Master data inserted programmatically

## Tenant Onboarding Process

When a new tenant is created:

1. **Tenant Record Creation**
   - A record is created in the master `tenants` table
   - Status is set to `PENDING`

2. **Schema Provisioning**
   - A new database schema is created: `tenant_<slug>`
   - Example: For slug "acme" → schema "tenant_acme"

3. **JPA Table Creation**
   - JPA/Hibernate automatically creates all tables on first access
   - Uses `ddl-auto=update` setting
   - All entity definitions are applied to the new schema

4. **Master Data Initialization**
   - Default categories programmatically inserted:
     - Electronics (with sub-categories: Mobile Phones, Laptops, Audio/Video, Smart Devices)
     - Clothing (with sub-categories: Men's, Women's, Kids, Footwear)
     - Food & Beverages (with sub-categories: Snacks, Beverages, Groceries, Dairy)
     - Home & Kitchen, Health & Beauty, Sports, Books, Toys
   - Default brands inserted:
     - Generic
     - House Brand
   - Uses prepared statements to prevent SQL injection

5. **Tenant Activation**
   - Status updated to `TRIAL` or `ACTIVE`
   - Tenant is ready for use

## Components

### TenantIdentifierResolver
- Resolves the current tenant's schema name from `TenantContext`
- Converts tenant slug to schema name (e.g., "acme" → "tenant_acme")
- Returns "easybilling" for non-tenant requests (master schema)

### SchemaMultiTenantConnectionProvider
- Provides database connections for the correct tenant schema
- Executes `USE <schema_name>` to switch schemas
- Resets to master schema after connection release

### TenantProvisioningService
- Creates new database schemas
- Triggers JPA table creation by accessing the schema
- Coordinates the provisioning process with master data service

### TenantMasterDataService
- Inserts default categories and brands using prepared statements
- Prevents SQL injection with schema name validation
- Verifies data integrity after provisioning

### TenantInterceptor
- Intercepts HTTP requests
- Uses TenantResolvers to identify the tenant
- Sets TenantContext for the request lifecycle

### TenantResolvers
1. **HeaderTenantResolver** (Priority: 10) - Checks `X-Tenant-Id` header
2. **SubdomainTenantResolver** (Priority: 20) - Extracts from subdomain

## Configuration

### application.yml
```yaml
app:
  multi-tenancy:
    domain: easybilling.com
    strategy: SCHEMA_PER_TENANT

spring:
  jpa:
    hibernate:
      ddl-auto: none  # Managed by Flyway
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    schemas: easybilling
```

### Database Setup

1. Create master database:
```sql
CREATE DATABASE easybilling;
```

2. Configure datasource (application.yml or environment variables):
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/easybilling
    username: root
    password: your_password
```

## Migrations

  
## Database Configuration

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/easybilling
    username: root
    password: your_password
    
  jpa:
    hibernate:
      ddl-auto: update  # Auto-creates/updates tables in all schemas
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    schemas: easybilling  # Only master schema uses Flyway
```

### Master Schema Setup

1. Create master database:
```sql
CREATE DATABASE easybilling;
```

2. On application startup, Flyway runs migrations:
   - `V1__Create_users_table.sql` - Global users table
   - `V2__Create_tenants_table.sql` - Tenant registry

### Tenant Schema Setup

**Automatic via JPA/Hibernate:**
- Tables are created automatically when a tenant schema is first accessed
- All `@Entity` classes are scanned and their tables are created
- `ddl-auto=update` ensures schema evolves with entity changes
- No manual migration files needed for tenant schemas

## Master Data

Inserted **programmatically** after schema creation using prepared statements:

### Default Categories (22 total)
**Main Categories (8):**
1. Electronics
2. Clothing  
3. Food & Beverages
4. Home & Kitchen
5. Health & Beauty
6. Sports & Outdoors
7. Books & Stationery
8. Toys & Games

**Sub-categories (14)** - Organized under parent categories:
- Electronics: Mobile Phones, Laptops & Computers, Audio & Video, Smart Devices
- Clothing: Men's, Women's, Kids, Footwear
- Food & Beverages: Snacks, Beverages, Groceries, Dairy Products

### Default Brands (2)
1. Generic - For unbranded products
2. House Brand - For store's own brand products

### Data Insertion Security
- ✅ Uses **prepared statements** (no SQL injection risk)
- ✅ **Schema name validation** using regex: `^tenant_[a-z0-9_-]+$`
- ✅ **Backtick escaping** for MySQL identifiers
- ✅ **Duplicate prevention** using `ON DUPLICATE KEY UPDATE`

## Usage

### Creating a Tenant

```bash
POST /api/v1/tenants
{
  "name": "ACME Corp",
  "slug": "acme",
  "plan": "PRO",
  "contactEmail": "admin@acme.com"
}
```

This will:
1. Create tenant record in master schema
2. Create new database schema `tenant_acme`
3. JPA automatically creates all tables on first access
4. Insert default categories and brands
5. Return tenant details with `status: "TRIAL"`

### Accessing Tenant Data

**Option 1: Subdomain**
```
https://acme.easybilling.com/api/v1/products
```

**Option 2: Header**
```bash
curl -H "X-Tenant-Id: acme" https://api.easybilling.com/api/v1/products
```

## Schema Naming Convention

- Master schema: `easybilling`
- Tenant schemas: `tenant_<slug>`
  - Example: `tenant_acme`, `tenant_demo`, `tenant_retailco`

## Data Isolation

- **Database Level**: Each tenant has a separate schema
- **Application Level**: TenantContext ensures correct schema selection
- **Connection Level**: Hibernate switches schemas per connection

## Schema Evolution

### Adding New Entity/Table

1. Create new `@Entity` class in `com.easybilling.entity`
2. Restart application or wait for next deployment
3. **Automatic**: Hibernate creates the table in all schemas (master + tenant)
4. No manual migration needed

### Modifying Existing Entity

1. Update entity class (add/remove fields)
2. **Automatic**: Hibernate applies changes via `ddl-auto=update`
3. **Limitation**: Cannot drop columns automatically (safety feature)
4. For complex changes, manual ALTER statements may be needed

### Best Practices

- Use `@Column(nullable = false)` for required fields
- Define proper indexes with `@Index` in `@Table`
- Use appropriate column lengths: `@Column(length = 255)`
- Test schema changes on a test tenant first

## Troubleshooting

### Schema Not Found
- Verify tenant exists in `tenants` table
- Check `schema_name` column
- Confirm schema exists in database: `SHOW DATABASES LIKE 'tenant_%'`

### Migration Failures
- Check Flyway history: `SELECT * FROM <schema>.flyway_schema_history`
- Verify SQL syntax and permissions
- Check logs for detailed error messages

### Connection Issues
- Verify datasource configuration
- Check database user permissions for schema creation
- Ensure user can execute `USE <schema>` and `CREATE DATABASE`

## Security Considerations

1. **Schema Isolation**: Data is completely isolated at database level
2. **Tenant Validation**: All requests must have valid tenant context
3. **Access Control**: Use RBAC within tenant boundaries
4. **Audit Trail**: Track schema access and modifications

## Performance

- **Connection Pooling**: Shared pool with schema switching
- **Caching**: Tenant metadata cached in Redis
- **Indexing**: Proper indexes on all tenant tables
- **Query Optimization**: Use JPA best practices

## Monitoring

- Track schema creation success/failure
- Monitor schema sizes and growth
- Alert on migration failures
- Track tenant provisioning times
