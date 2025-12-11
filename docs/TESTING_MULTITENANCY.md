# Testing Multi-Tenancy

## Manual Testing Steps

### 1. Setup Database

```bash
# Create MySQL database
mysql -u root -p

CREATE DATABASE easybilling;
USE easybilling;

# Verify connection
SHOW DATABASES;
```

### 2. Start the Application

```bash
cd easybilling
mvn spring-boot:run
```

The application will:
- Run Flyway migrations on the master schema (easybilling)
- Create the `tenants` and `users` tables

### 3. Test Tenant Creation

#### Create a new tenant via API:

```bash
curl -X POST http://localhost:8080/api/v1/tenants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Demo Store",
    "slug": "demo",
    "description": "Demo tenant for testing",
    "plan": "PRO",
    "contactEmail": "admin@demo.com",
    "contactPhone": "+1234567890",
    "address": "123 Main St",
    "city": "New York",
    "state": "NY",
    "country": "USA",
    "postalCode": "10001"
  }'
```

#### Expected Response:

```json
{
  "success": true,
  "message": "Tenant created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Demo Store",
    "slug": "demo",
    "status": "TRIAL",
    "plan": "PRO",
    "schemaName": "tenant_demo",
    ...
  }
}
```

### 4. Verify Schema Creation

```bash
# Connect to MySQL
mysql -u root -p

# List all databases/schemas
SHOW DATABASES;

# Should see:
# - easybilling (master)
# - tenant_demo (new tenant schema)

# Verify tables in tenant schema
USE tenant_demo;
SHOW TABLES;

# Should see all tenant tables:
# - users
# - stores
# - products
# - categories
# - brands
# - invoices
# - customers
# - etc.
```

### 5. Verify Master Data

```bash
# Check default categories
SELECT * FROM tenant_demo.categories WHERE id <= 10;

# Should see 8 default categories:
# - Electronics
# - Clothing
# - Food & Beverages
# - Home & Kitchen
# - Health & Beauty
# - Sports & Outdoors
# - Books & Stationery
# - Toys & Games

# Check default brands
SELECT * FROM tenant_demo.brands;

# Should see 2 default brands:
# - Generic
# - House Brand
```

### 6. Test Tenant Access

#### Test with Subdomain (requires DNS/hosts setup):

```bash
# Add to /etc/hosts (Linux/Mac) or C:\Windows\System32\drivers\etc\hosts (Windows)
127.0.0.1 demo.easybilling.com

# Make request
curl http://demo.easybilling.com:8080/api/v1/products \
  -H "Authorization: Bearer <token>"
```

#### Test with Header:

```bash
curl http://localhost:8080/api/v1/products \
  -H "X-Tenant-Id: demo" \
  -H "Authorization: Bearer <token>"
```

### 7. Test Multiple Tenants

```bash
# Create second tenant
curl -X POST http://localhost:8080/api/v1/tenants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Retail Corp",
    "slug": "retail",
    "plan": "ENTERPRISE",
    "contactEmail": "admin@retail.com"
  }'

# Verify both schemas exist
mysql -u root -p -e "SHOW DATABASES LIKE 'tenant_%';"

# Should see:
# - tenant_demo
# - tenant_retail
```

### 8. Verify Data Isolation

```bash
# Create a product in demo tenant
curl -X POST http://localhost:8080/api/v1/inventory/products \
  -H "X-Tenant-Id: demo" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "sku": "TEST-001",
    "barcode": "1234567890",
    "sellingPrice": 99.99,
    "costPrice": 50.00,
    "mrp": 99.99
  }'

# Try to access from retail tenant
curl http://localhost:8080/api/v1/inventory/products \
  -H "X-Tenant-Id: retail"

# Should NOT see the product created in demo tenant
```

## Automated Testing

### Unit Tests

Create test cases for:

1. **TenantProvisioningService**
   - Schema creation
   - Migration execution
   - Master data initialization
   - Error handling

2. **TenantIdentifierResolver**
   - Correct schema resolution
   - Default schema fallback
   - Slug to schema name conversion

3. **SchemaMultiTenantConnectionProvider**
   - Schema switching
   - Connection management
   - Error recovery

### Integration Tests

```java
@SpringBootTest
@Transactional
class TenantProvisioningIntegrationTest {
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Test
    void testTenantCreation() {
        // Create tenant
        TenantRequest request = TenantRequest.builder()
            .name("Test Tenant")
            .slug("test")
            .plan(SubscriptionPlan.BASIC)
            .contactEmail("test@test.com")
            .build();
        
        TenantResponse response = tenantService.createTenant(request);
        
        // Verify tenant record
        assertNotNull(response.getId());
        assertEquals("test", response.getSlug());
        assertEquals("tenant_test", response.getSchemaName());
        
        // Verify schema exists
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = 'tenant_test'",
            Integer.class
        );
        assertEquals(1, count);
        
        // Verify master data
        jdbcTemplate.execute("USE tenant_test");
        Integer categoryCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM categories WHERE id <= 10",
            Integer.class
        );
        assertTrue(categoryCount > 0);
    }
}
```

## Troubleshooting

### Schema Creation Fails

**Symptom**: Error during tenant creation
**Check**:
```bash
# Check MySQL user permissions
SHOW GRANTS FOR 'your_user'@'localhost';

# User needs CREATE DATABASE permission
GRANT CREATE ON *.* TO 'your_user'@'localhost';
```

### Migration Failures

**Symptom**: Tenant created but tables missing
**Check**:
```bash
# Check Flyway history
USE tenant_demo;
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

# Check for failed migrations
SELECT * FROM flyway_schema_history WHERE success = 0;
```

### Data Not Isolated

**Symptom**: Seeing data from other tenants
**Check**:
```bash
# Verify current schema
SELECT DATABASE();

# Check TenantContext in logs
# Should see: "Resolved tenant schema: tenant_demo"
```

### Connection Issues

**Symptom**: Cannot connect to tenant schema
**Check**:
```bash
# Verify schema exists
SHOW DATABASES LIKE 'tenant_%';

# Test connection manually
USE tenant_demo;
SHOW TABLES;
```

## Performance Testing

### Load Test Tenant Creation

```bash
# Create multiple tenants concurrently
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/v1/tenants \
    -H "Content-Type: application/json" \
    -d "{
      \"name\": \"Tenant $i\",
      \"slug\": \"tenant$i\",
      \"plan\": \"BASIC\",
      \"contactEmail\": \"admin$i@test.com\"
    }" &
done
wait
```

### Measure Provisioning Time

Monitor logs for:
- Schema creation time
- Migration execution time
- Master data initialization time
- Total provisioning time

Expected: < 5 seconds per tenant

## Cleanup

### Delete Test Tenants

```bash
# Drop tenant schemas
mysql -u root -p -e "DROP DATABASE IF EXISTS tenant_demo;"
mysql -u root -p -e "DROP DATABASE IF EXISTS tenant_retail;"
mysql -u root -p -e "DROP DATABASE IF EXISTS tenant_test;"

# Clean up tenant records
mysql -u root -p easybilling -e "DELETE FROM tenants WHERE slug LIKE 'test%';"
```
