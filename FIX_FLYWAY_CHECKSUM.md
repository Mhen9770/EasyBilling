# Fix Flyway Checksum Mismatch

## Problem
After migrating from PostgreSQL to MySQL, Flyway detects a checksum mismatch because the migration file was changed but already applied to the database.

## Solution Options

### Option 1: Drop and Recreate Flyway History Table (Recommended for Development)

Connect to MySQL and drop the billing service's Flyway history table:

```bash
# Connect to MySQL
docker exec -it easybilling-mysql mysql -u postgres -proot easybilling

# Or if using local MySQL
mysql -u postgres -proot easybilling
```

Then run:
```sql
DROP TABLE IF EXISTS flyway_billing_schema_history;
```

Restart the billing service - it will recreate the table and re-run migrations.

### Option 2: Run Flyway Repair

You can create a simple Java class or use Flyway CLI to repair:

```bash
# Using Flyway CLI (if installed)
flyway repair -url=jdbc:mysql://localhost:3306/easybilling -user=postgres -password=root -table=flyway_billing_schema_history
```

### Option 3: Manual SQL Update

Update the checksum in the history table:

```sql
UPDATE flyway_billing_schema_history 
SET checksum = 1793075850 
WHERE version = '1';
```

## What Was Changed

Each service now uses its own Flyway history table:
- `flyway_billing_schema_history` - Billing service
- `flyway_auth_schema_history` - Auth service  
- `flyway_inventory_schema_history` - Inventory service
- `flyway_tenant_schema_history` - Tenant service
- `flyway_customer_schema_history` - Customer service

This prevents migration version conflicts when multiple services share the same database.

## Note

`validate-on-migrate: false` is temporarily set to allow startup. You can re-enable it after fixing the checksum issue by setting it to `true` in the application.yml files.

