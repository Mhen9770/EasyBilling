# Frontend-Backend Integration Guide

## Overview
This document describes the complete integration between the React frontend (`dynamicfrontend/`) and Spring Boot backend (`dynamiceasybilling/`).

## Architecture

```
┌─────────────────────────────────────────┐
│   React Frontend (Port 5173)            │
│                                         │
│  ┌──────────────────────────────────┐  │
│  │  Component Registry              │  │
│  │  - Text, Number, Select, etc.    │  │
│  └──────────────────────────────────┘  │
│            │                            │
│  ┌──────────────────────────────────┐  │
│  │  Metadata Client (Zustand)       │  │
│  │  - Caches metadata                │  │
│  │  - Handles form/list/page/wf     │  │
│  └──────────────────────────────────┘  │
│            │                            │
│  ┌──────────────────────────────────┐  │
│  │  API Client (Axios)              │  │
│  │  - Entity CRUD                    │  │
│  │  - Metadata fetch                 │  │
│  │  - Headers: X-Tenant-Id, etc.    │  │
│  └──────────────────────────────────┘  │
│            │                            │
│            │ HTTP/REST                  │
│            ▼                            │
└─────────────────────────────────────────┘
             │
             │ CORS Enabled
             │
┌─────────────────────────────────────────┐
│   Spring Boot Backend (Port 8081)       │
│                                         │
│  ┌──────────────────────────────────┐  │
│  │  CorsConfig                       │  │
│  │  - Allows localhost:5173          │  │
│  └──────────────────────────────────┘  │
│            │                            │
│  ┌──────────────────────────────────┐  │
│  │  MetadataController               │  │
│  │  - GET /api/metadata/form/{name}  │  │
│  │  - GET /api/metadata/list/{name}  │  │
│  │  - GET /api/metadata/page/{id}    │  │
│  │  - GET /api/metadata/workflow/{id}│  │
│  │  - GET /api/metadata/entities     │  │
│  └──────────────────────────────────┘  │
│            │                            │
│  ┌──────────────────────────────────┐  │
│  │  DynamicEntityController          │  │
│  │  - POST /api/{entity}/create      │  │
│  │  - POST /api/{entity}/update      │  │
│  │  - GET  /api/{entity}/find        │  │
│  │  - GET  /api/{entity}/{id}        │  │
│  │  - DELETE /api/{entity}/{id}      │  │
│  │  - POST /api/{entity}/action/{n}  │  │
│  └──────────────────────────────────┘  │
│            │                            │
│  ┌──────────────────────────────────┐  │
│  │  Runtime Pipeline                 │  │
│  │  - Rules, Workflows, Permissions  │  │
│  └──────────────────────────────────┘  │
│            │                            │
│  ┌──────────────────────────────────┐  │
│  │  Repositories (JPA)               │  │
│  │  - EntityDefinition               │  │
│  │  - FieldDefinition                │  │
│  │  - DynamicEntity                  │  │
│  │  - Workflow, Rules, etc.          │  │
│  └──────────────────────────────────┘  │
│            │                            │
│  ┌──────────────────────────────────┐  │
│  │  MySQL Database                   │  │
│  │  - entity_definitions             │  │
│  │  - field_definitions              │  │
│  │  - dynamic_entities               │  │
│  └──────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

## Startup Sequence

### 1. Backend Startup
```bash
cd dynamiceasybilling
mvn spring-boot:run
```

**What Happens:**
1. Spring Boot starts on port 8081
2. MySQL connection established
3. JPA creates tables if needed
4. `MetadataSeeder` runs on first startup:
   - Creates 4 entities: Customer, Product, Invoice, Payment
   - Creates ~41 field definitions
   - Creates 5 business rules
   - Creates 3 workflows
   - Creates 4 security groups
5. All controllers registered and ready

**Log Output:**
```
INFO  c.r.e.DynamicEasyBillingApplication : Started DynamicEasyBillingApplication in X.XXX seconds
INFO  c.r.e.s.MetadataSeeder               : Metadata seeding completed successfully
```

### 2. Frontend Startup
```bash
cd dynamicfrontend
npm install  # First time only
npm run dev
```

**What Happens:**
1. Vite dev server starts on port 5173
2. React app initializes
3. Component registry loads primitives (Text, Number, Select, etc.)
4. Widget registry loads (KPICard, QuickActions, etc.)
5. Offline sync manager initializes IndexedDB
6. App sets default tenant: "default", user: "admin"

**Browser Console:**
```
Offline sync initialized
Component registry: 11 components registered
```

## API Endpoints

### Metadata Endpoints

#### Get Form Metadata
```
GET http://localhost:8081/api/metadata/form/{entityName}
Headers:
  X-Tenant-Id: default
  X-User-Id: admin

Example: GET /api/metadata/form/Customer

Response:
{
  "id": "Customer.form.basic",
  "entity": "Customer",
  "title": "Customer",
  "layout": {
    "type": "two-column",
    "areas": [
      { "name": "left", "width": 60 },
      { "name": "right", "width": 40 }
    ]
  },
  "fields": [
    {
      "name": "customerName",
      "label": "Customer Name",
      "component": "Text",
      "required": true,
      "validation": { "maxLength": 100 }
    },
    {
      "name": "email",
      "label": "Email",
      "component": "Text",
      "required": true
    },
    // ... more fields
  ],
  "actions": [
    {
      "id": "save",
      "label": "Save",
      "type": "submit",
      "permission": "entity:Customer:write"
    }
  ]
}
```

#### Get List Metadata
```
GET http://localhost:8081/api/metadata/list/{entityName}
Headers:
  X-Tenant-Id: default
  X-User-Id: admin

Example: GET /api/metadata/list/Customer

Response:
{
  "id": "Customer.list",
  "entity": "Customer",
  "columns": [
    { "field": "customerName", "label": "Customer Name", "sortable": true },
    { "field": "email", "label": "Email", "sortable": true },
    { "field": "balance", "label": "Balance", "sortable": true, "format": "currency" }
  ],
  "pageSize": 20,
  "rowActions": [
    { "id": "edit", "label": "Edit", "type": "navigate", "target": "/entity/Customer/edit/{id}" },
    { "id": "delete", "label": "Delete", "type": "action", "target": "/api/Customer/{id}" }
  ]
}
```

#### Get All Entities
```
GET http://localhost:8081/api/metadata/entities
Headers:
  X-Tenant-Id: default

Response:
[
  {
    "name": "Customer",
    "label": "Customer",
    "tableName": "customers"
  },
  {
    "name": "Product",
    "label": "Product",
    "tableName": "products"
  },
  // ... more entities
]
```

### Entity CRUD Endpoints

#### Create Entity
```
POST http://localhost:8081/api/{entity}/create
Headers:
  X-Tenant-Id: default
  X-User-Id: admin
  Content-Type: application/json

Body:
{
  "customerName": "Acme Corporation",
  "email": "contact@acme.com",
  "phoneNumber": "+1-555-0123",
  "address": "123 Main St, City, State 12345",
  "balance": 0,
  "creditLimit": 50000,
  "status": "active"
}

Response:
{
  "id": 1,
  "entityType": "Customer",
  "tenantId": "default",
  "attributes": {
    "customerName": "Acme Corporation",
    "email": "contact@acme.com",
    // ... other fields
  },
  "createdAt": "2025-12-11T20:00:00",
  "updatedAt": "2025-12-11T20:00:00"
}
```

#### Find Entities
```
GET http://localhost:8081/api/{entity}/find?page=0&size=20
Headers:
  X-Tenant-Id: default
  X-User-Id: admin

Response:
[
  {
    "id": 1,
    "entityType": "Customer",
    "attributes": { /* customer data */ }
  },
  // ... more entities
]
```

## Frontend Integration Points

### 1. Form Tab - Dynamic Form from Metadata

**File:** `dynamicfrontend/src/App.tsx` - `DemoForm` component

**Flow:**
1. Component mounts
2. Calls `metadataClient.fetchForm('Customer')`
3. MetadataClient checks Zustand cache
4. If not cached, fetches from `GET /api/metadata/form/Customer`
5. Stores in cache and returns
6. `FormRenderer` receives metadata
7. Builds Zod validation schema from metadata
8. Renders fields using component registry
9. User fills form
10. User clicks "Save"
11. `onSuccess` callback triggered
12. Calls `apiClient.createEntity('Customer', data)`
13. POST to `/api/Customer/create`
14. Success/error alert shown

**Code Snippet:**
```typescript
function DemoForm() {
  const [formMeta, setFormMeta] = useState<FormMetadata | null>(null);
  
  useEffect(() => {
    const loadForm = async () => {
      metadataClient.setHeaders('default', 'admin');
      const metadata = await metadataClient.fetchForm('Customer');
      setFormMeta(metadata);
    };
    loadForm();
  }, []);
  
  const handleSuccess = async (data: any) => {
    apiClient.setCredentials('default', 'admin');
    const result = await apiClient.createEntity('Customer', data);
    alert('Customer created!');
  };
  
  return <FormRenderer formMeta={formMeta} onSuccess={handleSuccess} />;
}
```

### 2. List Tab - Dynamic Table from Metadata

**File:** `dynamicfrontend/src/App.tsx` - `DemoList` component

**Flow:**
1. Component mounts
2. Calls `metadataClient.fetchList('Customer')`
3. Fetches from `GET /api/metadata/list/Customer`
4. Caches and returns
5. `ListRenderer` receives metadata
6. Renders table with columns from metadata
7. User can sort, filter, paginate (future)

### 3. Component Registry

**File:** `dynamicfrontend/src/engine/registry.ts`

Maps component names to React components:
```typescript
componentRegistry.registerBatch({
  Text: TextInput,
  Number: NumberInput,
  Select: SelectInput,
  // etc.
});
```

When metadata says `"component": "Text"`, the registry resolves it to the `TextInput` component.

### 4. Metadata Client Caching

**File:** `dynamicfrontend/src/engine/metadataClient.ts`

Uses Zustand to cache metadata:
```typescript
const useMetadataStore = create<MetadataStore>((set, get) => ({
  forms: new Map(),
  lists: new Map(),
  pages: new Map(),
  workflows: new Map(),
  // ...
}));
```

- Metadata is cached after first fetch
- Cache invalidation via websocket (future)
- Manual invalidation via `metadataClient.invalidate(type, id)`

## Backend Components

### MetadataController

**File:** `dynamiceasybilling/src/main/java/com/runtime/engine/controller/MetadataController.java`

**Responsibilities:**
- Serve form metadata by querying `EntityDefinition` and `FieldDefinition`
- Serve list metadata with column configuration
- Map field data types to frontend component names
- Parse options for select fields
- Return tenant-scoped metadata

**Key Methods:**
```java
@GetMapping("/form/{entityName}")
public ResponseEntity<?> getFormMetadata(@PathVariable String entityName, ...)

@GetMapping("/list/{entityName}")
public ResponseEntity<?> getListMetadata(@PathVariable String entityName, ...)

private String mapFieldTypeToComponent(String fieldType) {
  // TEXT -> "Text"
  // NUMBER -> "Number"
  // etc.
}
```

### DynamicEntityController

**File:** `dynamiceasybilling/src/main/java/com/runtime/engine/controller/DynamicEntityController.java`

**Responsibilities:**
- Handle all entity CRUD operations generically
- Execute runtime pipeline for each action
- Apply rules, workflows, permissions
- Return results as JSON

**Key Endpoints:**
```java
@PostMapping("/create")
@PostMapping("/update")
@GetMapping("/find")
@GetMapping("/{id}")
@DeleteMapping("/{id}")
@PostMapping("/action/{actionName}")
```

### CorsConfig

**File:** `dynamiceasybilling/src/main/java/com/runtime/engine/config/CorsConfig.java`

**Configuration:**
```java
config.setAllowedOrigins(Arrays.asList(
  "http://localhost:5173",
  "http://localhost:3000"
));
config.setAllowedHeaders(Collections.singletonList("*"));
config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
```

## Data Flow Example: Creating a Customer

### Step-by-Step

1. **User navigates to Form tab**
   - Browser: `http://localhost:5173`
   - Tab: "Form"

2. **Frontend requests form metadata**
   ```
   GET http://localhost:8081/api/metadata/form/Customer
   Headers:
     X-Tenant-Id: default
     X-User-Id: admin
   ```

3. **Backend processes request**
   - `MetadataController.getFormMetadata("Customer", "default")`
   - Queries `EntityDefinitionRepository.findByNameAndTenantId("Customer", "default")`
   - Gets `EntityDefinition` with id=1
   - Queries `FieldDefinitionRepository.findByEntityDefinitionIdOrderByOrderIndexAsc(1)`
   - Gets 10 `FieldDefinition` objects
   - Builds JSON response with fields mapped to components

4. **Backend returns metadata**
   ```json
   {
     "id": "Customer.form.basic",
     "entity": "Customer",
     "title": "Customer",
     "fields": [
       { "name": "customerName", "component": "Text", "label": "Customer Name", "required": true },
       // ... 9 more fields
     ],
     "actions": [
       { "id": "save", "label": "Save", "type": "submit" }
     ]
   }
   ```

5. **Frontend renders form**
   - `FormRenderer` receives metadata
   - Builds Zod schema: `z.object({ customerName: z.string(), ... })`
   - Creates React Hook Form instance
   - Loops through fields, resolves components from registry
   - Renders `<TextInput>`, `<NumberInput>`, etc.

6. **User fills form and clicks Save**
   - React Hook Form validates using Zod
   - If valid, calls `handleSubmit(data)`
   - `onSuccess` callback triggered
   - Calls `apiClient.createEntity('Customer', data)`

7. **Frontend sends create request**
   ```
   POST http://localhost:8081/api/Customer/create
   Headers:
     X-Tenant-Id: default
     X-User-Id: admin
     Content-Type: application/json
   Body:
     {
       "customerName": "Acme Corp",
       "email": "contact@acme.com",
       "phoneNumber": "+1-555-0123",
       "address": "123 Main St",
       "balance": 0,
       "creditLimit": 50000,
       "status": "active"
     }
   ```

8. **Backend processes create**
   - `DynamicEntityController.create("Customer", data, "default", "admin")`
   - Builds `RuntimeExecutionContext`
   - Calls `runtimePipeline.execute("create", "Customer", data, context)`
   - Pipeline runs:
     - **Pre-processing**: Check permissions, run pre-create plugins/rules
     - **Action**: `EntityCrudService.create()`
       - Creates `DynamicEntity` object
       - Sets `entityType = "Customer"`
       - Sets `tenantId = "default"`
       - Sets `attributes = data` (stored as JSON in DB)
       - Saves via `DynamicEntityRepository.save()`
     - **Post-processing**: Run post-create plugins/rules/workflows
   - Returns created entity

9. **Backend returns response**
   ```json
   {
     "id": 1,
     "entityType": "Customer",
     "tenantId": "default",
     "attributes": {
       "customerName": "Acme Corp",
       "email": "contact@acme.com",
       // ... other fields
     },
     "createdAt": "2025-12-11T20:00:00",
     "updatedAt": "2025-12-11T20:00:00"
   }
   ```

10. **Frontend shows success**
    - Alert: "Customer created successfully!"
    - Data displayed in alert

## Configuration

### Backend Configuration

**File:** `dynamiceasybilling/src/main/resources/application.properties`

```properties
server.port=8081
spring.datasource.url=jdbc:mysql://localhost:3306/easybilling
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
```

### Frontend Configuration

**File:** `dynamicfrontend/src/api/client.ts`

```typescript
constructor(baseURL: string = 'http://localhost:8081') {
  this.client = axios.create({
    baseURL,
    headers: {
      'Content-Type': 'application/json',
    },
  });
}
```

**File:** `dynamicfrontend/src/engine/metadataClient.ts`

```typescript
constructor(baseURL: string = 'http://localhost:8081') {
  this.api = axios.create({
    baseURL,
    headers: {
      'Content-Type': 'application/json',
    },
  });
}
```

## Troubleshooting

### CORS Errors

**Symptom:**
```
Access to XMLHttpRequest at 'http://localhost:8081/api/metadata/form/Customer' 
from origin 'http://localhost:5173' has been blocked by CORS policy
```

**Solution:**
- Check `CorsConfig.java` includes `http://localhost:5173`
- Restart backend after CORS changes
- Clear browser cache

### 404 Not Found on Metadata Endpoints

**Symptom:**
```
GET http://localhost:8081/api/metadata/form/Customer 404 (Not Found)
```

**Solution:**
- Check `MetadataController` is in correct package
- Verify `@RestController` and `@RequestMapping("/api/metadata")` annotations
- Check backend logs for controller registration
- Restart backend

### Empty Form / No Fields

**Symptom:**
Form loads but shows no fields

**Solution:**
- Check metadata seeder ran successfully
- Query database: `SELECT * FROM entity_definitions WHERE name = 'Customer';`
- Query database: `SELECT * FROM field_definitions WHERE entity_definition_id = 1;`
- Check backend logs for SQL errors
- Verify tenant ID matches ("default")

### Form Validation Not Working

**Symptom:**
Form submits without validation

**Solution:**
- Check Zod schema build in browser console
- Verify metadata includes `validation` objects
- Check React Hook Form setup
- Verify `required` field in metadata

## Testing

### Backend Testing

```bash
cd dynamiceasybilling

# Test compilation
mvn clean compile

# Run tests (if any)
mvn test

# Start application
mvn spring-boot:run

# Test metadata endpoint
curl -H "X-Tenant-Id: default" \
     http://localhost:8081/api/metadata/form/Customer

# Test create endpoint
curl -X POST \
     -H "Content-Type: application/json" \
     -H "X-Tenant-Id: default" \
     -H "X-User-Id: admin" \
     -d '{"customerName":"Test Corp","email":"test@test.com"}' \
     http://localhost:8081/api/Customer/create
```

### Frontend Testing

```bash
cd dynamicfrontend

# Install dependencies
npm install

# Run dev server
npm run dev

# Run tests
npm run test

# Build for production
npm run build
```

### Integration Testing

1. Start backend: `mvn spring-boot:run`
2. Start frontend: `npm run dev`
3. Open browser: `http://localhost:5173`
4. Test each tab:
   - Dashboard: Should show KPIs and widgets
   - Form: Should load Customer form from backend
   - List: Should load Customer list metadata
   - Metadata Editor: Should allow form creation
   - Layout Builder: Should allow page creation
5. Test form submission:
   - Fill out Customer form
   - Click "Save Customer"
   - Check for success alert
   - Check backend logs for entity creation
6. Check database:
   ```sql
   SELECT * FROM dynamic_entities WHERE entity_type = 'Customer';
   ```

## Security Considerations

### Current Implementation

- **Tenant Isolation**: All entities scoped by `tenantId`
- **User Tracking**: `X-User-Id` header tracked in context
- **Permissions**: Backend has permission engine (not yet enforced in controllers)
- **CORS**: Restricted to specific origins

### Production Recommendations

1. **Authentication**: Add JWT or OAuth2
2. **Authorization**: Enforce permissions in controllers
3. **HTTPS**: Use SSL in production
4. **Rate Limiting**: Add rate limiting to API endpoints
5. **Input Validation**: Add server-side validation beyond metadata
6. **SQL Injection**: Use parameterized queries (JPA handles this)
7. **XSS**: Sanitize HTML in frontend
8. **CSRF**: Add CSRF tokens for state-changing operations

## Performance Optimization

### Backend

1. **Database Indexing**: Index `tenantId`, `entityType`, `name` fields
2. **Caching**: Add Redis for metadata caching
3. **Connection Pooling**: Configure HikariCP
4. **Query Optimization**: Use JPA query hints
5. **Lazy Loading**: Use `@Lazy` for relationships

### Frontend

1. **Metadata Caching**: Already implemented with Zustand
2. **Code Splitting**: Use React.lazy() for components
3. **Virtualization**: Use react-window for large lists
4. **Debouncing**: Debounce search and filter inputs
5. **Memoization**: Use React.memo for expensive components
6. **Bundle Optimization**: Analyze bundle with `npm run build --report`

## Next Steps

1. **Add Authentication**: Implement JWT authentication
2. **Enforce Permissions**: Connect permission engine to controllers
3. **Add Workflows**: Integrate workflow execution with UI
4. **Add Rules**: Show rule execution in UI
5. **Add Audit Logs**: Track all entity changes
6. **Add Bulk Operations**: Support batch create/update/delete
7. **Add Export/Import**: Allow metadata export/import
8. **Add Domain Packs**: Create billing, inventory, HR packs
9. **Add Versioning**: Version metadata changes
10. **Add Rollback**: Support metadata rollback

## Resources

- Backend: `http://localhost:8081`
- Frontend: `http://localhost:5173`
- API Docs: (future) `http://localhost:8081/swagger-ui.html`
- Database: MySQL on `localhost:3306/easybilling`

## Support

For issues or questions:
- Check logs: `dynamiceasybilling/logs/`
- Check browser console: F12 > Console
- Check network tab: F12 > Network
- Review this integration guide
