# Metadata Seeder System - Billing Edition

## Overview

The Metadata Seeder system provides initial baseline metadata for the Domain-Agnostic Runtime Business Engine, specifically tailored for billing software. It automatically populates the database with billing-specific entities, fields, rules, workflows, and permissions when the application starts for the first time.

## What Gets Seeded

### 1. Billing Entities (4 entities)

#### Customer Entity
Customer management entity with the following fields:
- **customerName** (string, required) - Customer name (2-100 chars)
- **email** (string, required) - Email address with pattern validation
- **phoneNumber** (string, required) - Phone number (max 20 chars)
- **address** (text, optional) - Physical address
- **city** (string, optional) - City name
- **postalCode** (string, optional) - Postal/ZIP code
- **taxId** (string, optional) - Tax identification number
- **balance** (decimal, optional) - Current account balance
- **creditLimit** (decimal, optional) - Maximum credit allowed (0-999,999,999)
- **status** (string, optional) - Enum: active, inactive, suspended, blocked

#### Product Entity
Product/Service catalog entity:
- **productName** (string, required) - Product name (2-200 chars)
- **description** (text, optional) - Detailed description
- **sku** (string, required) - Stock Keeping Unit code
- **price** (decimal, required) - Unit selling price (min 0)
- **cost** (decimal, optional) - Unit cost (min 0)
- **taxRate** (decimal, optional) - Tax rate percentage (0-100)
- **category** (string, optional) - Product category
- **stockQuantity** (integer, optional) - Available stock (min 0)
- **unit** (string, optional) - Enum: piece, kg, liter, meter, hour, service
- **isActive** (boolean, optional) - Active status

#### Invoice Entity
Invoice management with versioning:
- **invoiceNumber** (string, required) - Unique invoice number (3-50 chars)
- **customerId** (string, required) - Reference to customer
- **customerName** (string, required) - Customer name snapshot
- **invoiceDate** (date, required) - Invoice issue date
- **dueDate** (date, required) - Payment due date
- **subtotal** (decimal, required) - Pre-tax amount (min 0)
- **taxAmount** (decimal, optional) - Total tax (min 0)
- **discountAmount** (decimal, optional) - Discount applied (min 0)
- **totalAmount** (decimal, required) - Final amount (min 0)
- **paidAmount** (decimal, optional) - Amount paid so far (min 0)
- **status** (string, required) - Enum: draft, sent, paid, partial, overdue, cancelled
- **notes** (text, optional) - Additional notes (max 2000 chars)

#### Payment Entity
Payment transaction tracking:
- **paymentReference** (string, required) - Payment reference number (3-50 chars)
- **invoiceId** (string, required) - Reference to invoice
- **customerId** (string, required) - Reference to customer
- **amount** (decimal, required) - Payment amount (min 0.01)
- **paymentDate** (date, required) - Payment date
- **paymentMethod** (string, required) - Enum: cash, credit_card, debit_card, bank_transfer, check, mobile_payment
- **transactionId** (string, optional) - External transaction ID
- **status** (string, required) - Enum: pending, completed, failed, refunded
- **notes** (text, optional) - Additional notes (max 1000 chars)

### 2. Billing Rules (5 rules)

#### CustomerEmailValidation
- **Entity**: Customer
- **Trigger**: pre_create
- **Condition**: Email field is not null and not empty
- **Action**: Validate email format
- **Priority**: 10

#### InvoiceTotalCalculation
- **Entity**: Invoice
- **Trigger**: pre_create
- **Condition**: Subtotal field is not null
- **Action**: Calculate total from subtotal, tax, and discount
- **Priority**: 15

#### PaymentAmountValidation
- **Entity**: Payment
- **Trigger**: pre_create
- **Condition**: Amount field is not null
- **Action**: Validate amount is positive
- **Priority**: 10

#### ProductPriceValidation
- **Entity**: Product
- **Trigger**: pre_create
- **Condition**: Price and cost fields are not null
- **Action**: Ensure price is greater than cost
- **Priority**: 10

#### CustomerCreditLimitCheck
- **Entity**: Customer
- **Trigger**: post_update
- **Condition**: Balance and credit limit fields are not null
- **Action**: Check if customer has exceeded credit limit
- **Priority**: 5

### 3. Billing Workflows (3 workflows)

#### InvoiceCreationWorkflow
Executes after a new invoice is created:
1. **ValidateInvoiceData** (validation, mandatory) - Validates total amount > 0
2. **GenerateInvoiceNumber** (enrichment, optional) - Generates invoice number if missing
3. **SendInvoiceNotification** (notification, optional) - Sends invoice if status is 'sent'
4. **UpdateCustomerBalance** (enrichment, optional) - Updates customer balance

#### PaymentProcessingWorkflow
Executes when a payment is processed:
1. **ValidatePayment** (validation, mandatory) - Checks amount > 0
2. **UpdateInvoiceStatus** (enrichment, mandatory) - Updates related invoice
3. **UpdateCustomerBalance** (enrichment, optional) - Updates customer balance
4. **SendPaymentReceipt** (notification, optional) - Sends receipt if completed

#### CustomerOnboardingWorkflow
Executes when a new customer is created:
1. **ValidateCustomerData** (validation, mandatory) - Validates email exists
2. **InitializeBalance** (enrichment, optional) - Sets initial balance to 0
3. **SendWelcomeEmail** (notification, optional) - Sends welcome email if active

### 4. Billing Permissions (4 security groups)

#### BillingAdministrators
Full billing system access:
- Customer: create, update, find, delete
- Product: create, update, find, delete
- Invoice: create, update, find, delete
- Payment: create, update, find, delete

#### Accountants
Full access to invoices and payments:
- Customer: find, update
- Product: find
- Invoice: create, update, find
- Payment: create, update, find

#### SalesTeam
Access to customers, products, and invoices:
- Customer: create, update, find
- Product: find
- Invoice: create, find
- Payment: find

#### BillingViewers
Read-only access:
- Customer: find
- Product: find
- Invoice: find
- Payment: find

## How It Works

1. **Auto-Execution**: The seeder runs automatically on application startup via Spring Boot's `CommandLineRunner`
2. **Order Control**: Uses `@Order(1)` to run before the `RuntimeRegistryLoader` (@Order(2))
3. **Idempotent**: Checks if entities already exist before seeding (only seeds if database is empty)
4. **Transactional**: All seeding operations run in a single transaction for data consistency

## Usage

### Automatic Seeding

Simply start the application. If the database is empty, seeding happens automatically:

```bash
mvn spring-boot:run
```

You'll see logs like:
```
Starting metadata seeding...
Seeding basic entities...
Contact entity created with 6 fields
Task entity created with 7 fields
Note entity created with 5 fields
Basic entities seeded successfully
Seeding basic rules...
Basic rules seeded successfully
Seeding basic workflows...
Basic workflows seeded successfully
Seeding basic permissions...
Basic permissions seeded successfully
Metadata seeding completed successfully
```

### Testing Seeded Data

After the application starts, you can test the seeded billing entities:

#### Create a Customer
```bash
curl -X POST http://localhost:8081/api/Customer/create \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: default" \
  -H "X-User-Id: admin" \
  -d '{
    "customerName": "Acme Corporation",
    "email": "billing@acme.com",
    "phoneNumber": "+1-555-0123",
    "address": "123 Business St",
    "city": "New York",
    "postalCode": "10001",
    "taxId": "12-3456789",
    "balance": 0,
    "creditLimit": 50000,
    "status": "active"
  }'
```

#### Create a Product
```bash
curl -X POST http://localhost:8081/api/Product/create \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: default" \
  -H "X-User-Id: admin" \
  -d '{
    "productName": "Professional Services",
    "description": "Consulting and professional services",
    "sku": "PROF-SVC-001",
    "price": 150.00,
    "cost": 75.00,
    "taxRate": 10.0,
    "category": "Services",
    "unit": "hour",
    "isActive": true
  }'
```

#### Create an Invoice
```bash
curl -X POST http://localhost:8081/api/Invoice/create \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: default" \
  -H "X-User-Id: admin" \
  -d '{
    "invoiceNumber": "INV-2025-0001",
    "customerId": "CUST-001",
    "customerName": "Acme Corporation",
    "invoiceDate": "2025-01-15",
    "dueDate": "2025-02-15",
    "subtotal": 1500.00,
    "taxAmount": 150.00,
    "discountAmount": 0,
    "totalAmount": 1650.00,
    "paidAmount": 0,
    "status": "sent",
    "notes": "Net 30 payment terms"
  }'
```

#### Create a Payment
```bash
curl -X POST http://localhost:8081/api/Payment/create \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: default" \
  -H "X-User-Id: admin" \
  -d '{
    "paymentReference": "PAY-2025-0001",
    "invoiceId": "INV-2025-0001",
    "customerId": "CUST-001",
    "amount": 1650.00,
    "paymentDate": "2025-01-20",
    "paymentMethod": "bank_transfer",
    "transactionId": "TXN-ABC123",
    "status": "completed",
    "notes": "Full payment received"
  }'
```

#### Query Invoices
```bash
curl -X GET "http://localhost:8081/api/Invoice/find?page=0&size=10" \
  -H "X-Tenant-Id: default" \
  -H "X-User-Id: admin"
```

## Customization

### Adding More Seed Data

To add additional entities, edit `MetadataSeeder.java`:

```java
private void seedBasicEntities() {
    log.info("Seeding basic entities...");
    
    seedContactEntity();
    seedTaskEntity();
    seedNoteEntity();
    seedYourCustomEntity(); // Add your entity here
    
    log.info("Basic entities seeded successfully");
}

private void seedYourCustomEntity() {
    EntityDefinition entity = EntityDefinition.builder()
        .name("YourEntity")
        .tableName("your_entities")
        .description("Description of your entity")
        .tenantId(DEFAULT_TENANT)
        .active(true)
        .build();
    
    entity = entityDefinitionRepository.save(entity);
    
    // Add fields, rules, workflows as needed
}
```

### Disabling Seeding

To disable automatic seeding, you can:

1. **Delete the seeder** (not recommended):
   ```bash
   rm src/main/java/com/runtime/engine/seeder/MetadataSeeder.java
   ```

2. **Add a property** to control seeding:
   Add to `application.properties`:
   ```properties
   runtime.engine.seeder.enabled=false
   ```

   Then update MetadataSeeder:
   ```java
   @ConditionalOnProperty(name = "runtime.engine.seeder.enabled", havingValue = "true", matchIfMissing = true)
   @Component
   public class MetadataSeeder implements CommandLineRunner {
       // ...
   }
   ```

### Tenant-Specific Seeding

To seed data for multiple tenants, modify the seeder:

```java
private static final List<String> TENANTS = List.of("tenant1", "tenant2", "tenant3");

@Override
public void run(String... args) {
    for (String tenantId : TENANTS) {
        if (shouldSeed(tenantId)) {
            seedForTenant(tenantId);
        }
    }
}
```

## Database Impact

The seeder creates the following records on first run:

- **4 Entity Definitions** (Customer, Product, Invoice, Payment)
- **41 Field Definitions** (10 + 10 + 12 + 9 fields)
- **5 Rule Definitions** (validation and business logic rules)
- **3 Workflow Definitions** (invoice, payment, customer workflows)
- **11 Workflow Steps** (4 + 4 + 3 steps)
- **4 Security Groups** (BillingAdministrators, Accountants, SalesTeam, BillingViewers)

Total: ~78 metadata records for a complete billing system foundation

## Best Practices

1. **Keep It Simple**: Only seed essential baseline data needed for the system to function
2. **Use Defaults**: Always use the default tenant for initial seeds
3. **Document Changes**: Update this README when adding new seed data
4. **Test Thoroughly**: Test all seeded entities through the API before committing
5. **Version Control**: Keep seeds in sync with schema changes

## Troubleshooting

### Seeder Not Running

Check:
- Database connection is working
- Tables are created (JPA auto-creates them)
- No exceptions in logs

### Duplicate Data

The seeder only runs when `entityDefinitionRepository.count() == 0`. If you need to re-seed:

```sql
-- Clear all metadata (use with caution!)
DELETE FROM field_definitions;
DELETE FROM entity_definitions;
DELETE FROM workflow_steps;
DELETE FROM workflow_definitions;
DELETE FROM rule_definitions;
DELETE FROM security_group_permissions;
DELETE FROM security_group_members;
DELETE FROM security_groups;
```

### Validation Errors

If seeded data doesn't validate:
- Check field definitions match validation rules
- Ensure required fields are marked correctly
- Verify enum values in seed data

## Next Steps

After seeding:

1. **Test the APIs** - Verify all seeded entities work via REST endpoints
2. **Add Custom Entities** - Define entities specific to your domain
3. **Create Workflows** - Build workflows for your business processes
4. **Configure Permissions** - Set up security groups for your users
5. **Build UI** - Create forms and tables to interact with seeded entities

## Related Documentation

- [Main README](../README.md) - System overview and API documentation
- [Entity Engine](../entity/) - Entity definition and management
- [Rule Engine](../rule/) - Business rules configuration
- [Workflow Engine](../workflow/) - Workflow execution
- [Permission Engine](../permission/) - Security and authorization
