# Multi-Tenancy Implementation

## Overview

This application uses a **Schema-per-Tenant** strategy for multi-tenancy, where each tenant gets their own database schema with complete data isolation.

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
- Independent migrations and master data

## Tenant Onboarding Process

When a new tenant is created:

1. **Tenant Record Creation**
   - A record is created in the master `tenants` table
   - Status is set to `PENDING`

2. **Schema Provisioning**
   - A new database schema is created: `tenant_<slug>`
   - Example: For slug "acme" → schema "tenant_acme"

3. **Migration Execution**
   - Flyway runs all migrations from `db/migration/tenant/`
   - Creates all required tables in the tenant schema

4. **Master Data Initialization**
   - Default categories (Electronics, Clothing, Food, etc.)
   - Default brands (Generic, House Brand)
   - Initial configuration data

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
- Runs Flyway migrations for tenant schemas
- Coordinates the provisioning process

### TenantMasterDataService
- Initializes master/reference data in new tenant schemas
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

### Master Schema Migrations
Location: `src/main/resources/db/migration/`
- `V1__Create_users_table.sql` - Global users
- `V2__Create_tenants_table.sql` - Tenant registry

### Tenant Schema Migrations  
Location: `src/main/resources/db/migration/tenant/`
- `V1__Create_tenant_schema_tables.sql` - All tenant tables
- `V2__Insert_master_data.sql` - Default categories, brands

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
1. Create tenant record
2. Create schema `tenant_acme`
3. Run all migrations
4. Populate master data
5. Return tenant details

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

## Migration Management

### Adding New Tables

1. Create migration in `db/migration/tenant/V<N>__Description.sql`
2. Run migration on existing tenants:

```java
// Manual migration for existing tenants
tenantRepository.findAll().forEach(tenant -> {
    provisioningService.runMigrations(tenant.getSchemaName());
});
```

### Rolling Back

Schema-level rollback requires manual intervention:
1. Identify the schema
2. Execute rollback SQL manually or via Flyway repair

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
