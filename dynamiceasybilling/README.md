# Domain-Agnostic Runtime Business Engine

A complete, metadata-driven, entity-agnostic Spring Boot 3 + Java 17 backend system capable of handling dynamic entities, rules, workflows, templates, plugins, and permissions for ANY business domain.

## Overview

This system is built to be completely dynamic and configurable at runtime, with no hardcoded business entities like Product, Customer, Invoice, etc. All entities, fields, validations, and business logic are defined through metadata and executed through a unified runtime pipeline.

## Key Features

### 1. Dynamic Entity Engine
- **EntityDefinition**: Metadata-driven entity schemas
- **FieldDefinition**: Dynamic field definitions with validation rules
- **DynamicEntity**: Generic entity storage with JSON attributes
- **EntityRegistry**: In-memory registry for fast entity lookup
- **EntityEngine**: Core entity creation, update, and query operations
- **EntityCrudService**: CRUD operations with tenant isolation
- **EntityValidator**: Field-level and custom validation

### 2. Rule Engine
- **RuleDefinition**: Configurable business rules with conditions and actions
- **RuleEngine**: Rule execution with priority-based ordering
- **ConditionEvaluator**: SpEL-based condition evaluation with expression caching
- **ActionExecutor**: Execute multiple action types (set_variable, set_field, validate, transform, log, metadata)

### 3. Workflow Engine
- **WorkflowDefinition**: Multi-step workflow definitions
- **WorkflowStep**: Individual workflow steps with conditional execution
- **WorkflowEngine**: Sequential step execution with error handling
- **WorkflowRegistry**: Workflow registration and lookup

### 4. Template Engine
- **TemplateDefinition**: Dynamic templates with variable substitution
- **TemplateEngine**: Template rendering with nested variable support

### 5. Plugin Engine
- **Plugin**: Interface for custom plugin implementations
- **PluginDefinition**: Plugin metadata and configuration
- **PluginEngine**: Plugin lifecycle management
- **PluginRegistry**: Trigger-based plugin organization

### 6. Permission Engine
- **PermissionDefinition**: Entity + action-based permissions
- **SecurityGroup**: Group-based permission management
- **PermissionEngine**: Permission evaluation and enforcement

### 7. Tenant Configuration
- **TenantConfig**: Tenant-specific settings and features
- **TenantConfigService**: Tenant configuration management
- **TenantCache**: In-memory caching for tenant configs

### 8. Runtime Execution Pipeline
- **RuntimeExecutionContext**: Execution context with variables, metadata, and permissions
- **RuntimePipeline**: Unified execution pipeline orchestrating all engines

### 9. Dynamic REST API
- **DynamicEntityController**: Completely generic REST endpoints:
  - `POST /api/{entity}/create` - Create entity
  - `POST /api/{entity}/update` - Update entity
  - `GET /api/{entity}/find` - Query entities
  - `GET /api/{entity}/{id}` - Find by ID
  - `DELETE /api/{entity}/{id}` - Delete entity
  - `POST /api/{entity}/action/{actionName}` - Execute custom actions

## Architecture

### Package Structure
```
com.runtime.engine/
├── entity/          # Entity management
├── rule/            # Business rules
├── workflow/        # Workflow execution
├── template/        # Template rendering
├── plugin/          # Plugin system
├── permission/      # Security & permissions
├── tenant/          # Multi-tenancy
├── pipeline/        # Execution pipeline
├── controller/      # REST API
├── registry/        # Metadata registries
├── repo/            # JPA repositories
├── config/          # Spring configuration
└── util/            # Utilities
```

### Technology Stack
- **Spring Boot 3.4.0**
- **Java 17**
- **Spring Data JPA**
- **MySQL** (JSON column support)
- **Spring Expression Language (SpEL)** for rule evaluation
- **Jackson** for JSON serialization
- **Lombok** for boilerplate reduction

## Getting Started

### Prerequisites
- Java 17 or higher
- MySQL 5.7+ or MySQL 8.0+ (for JSON column support)
- Maven 3.6+

### Configuration

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/dynamiceasybilling
spring.datasource.username=your_username
spring.datasource.password=your_password

# Server Port
server.port=8081
```

### Build and Run

```bash
cd dynamiceasybilling
mvn clean install
mvn spring-boot:run
```

## Usage Examples

### 1. Define a Dynamic Entity

```json
POST /api/entity-definitions
{
  "name": "Contact",
  "tableName": "contacts",
  "tenantId": "tenant1",
  "fields": [
    {
      "name": "fullName",
      "dataType": "string",
      "required": true,
      "maxLength": 100
    },
    {
      "name": "email",
      "dataType": "string",
      "required": true,
      "pattern": "^[A-Za-z0-9+_.-]+@(.+)$"
    },
    {
      "name": "phoneNumber",
      "dataType": "string",
      "maxLength": 20
    }
  ]
}
```

### 2. Create Entity Instance

```json
POST /api/Contact/create
Headers:
  X-Tenant-Id: tenant1
  X-User-Id: user123
  
Body:
{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890"
}
```

### 3. Define a Rule

```json
POST /api/rule-definitions
{
  "name": "EmailValidationRule",
  "entityType": "Contact",
  "trigger": "pre_create",
  "condition": "#email != null && #email.contains('@')",
  "actions": {
    "validate": {
      "field": "email",
      "message": "Email must contain @"
    }
  }
}
```

### 4. Define a Workflow

```json
POST /api/workflow-definitions
{
  "name": "ContactCreationWorkflow",
  "entityType": "Contact",
  "trigger": "post_create",
  "steps": [
    {
      "name": "SendWelcomeEmail",
      "stepType": "notification",
      "stepOrder": 1
    },
    {
      "name": "AssignToSales",
      "stepType": "enrichment",
      "stepOrder": 2
    }
  ]
}
```

## Key Design Principles

1. **No Domain Assumptions**: System works for ANY business domain
2. **Metadata-Driven**: All configuration through metadata, not code
3. **100% Dynamic**: Entities, fields, rules, workflows all configurable at runtime
4. **Tenant Isolated**: Complete multi-tenancy support
5. **Extensible**: Plugin system for custom functionality
6. **Type-Safe**: Strong typing with validation
7. **Performance**: In-memory caching and expression compilation

## Database Schema

The system auto-creates the following tables:
- `entity_definitions` - Entity metadata
- `field_definitions` - Field metadata
- `dynamic_entities` - Generic entity data (JSON attributes)
- `rule_definitions` - Business rules
- `workflow_definitions` - Workflow metadata
- `workflow_steps` - Workflow steps
- `template_definitions` - Templates
- `plugin_definitions` - Plugin metadata
- `permission_definitions` - Permissions
- `security_groups` - Security groups
- `tenant_configs` - Tenant configuration

## API Reference

All endpoints follow the pattern: `/api/{entityName}/{operation}`

### Standard Operations
- **create**: Create new entity instance
- **update**: Update existing entity
- **find**: Query entities with pagination
- **delete**: Delete entity by ID

### Custom Actions
Execute any custom action via: `POST /api/{entity}/action/{actionName}`

### Headers
- `X-Tenant-Id`: Tenant identifier (required)
- `X-User-Id`: User identifier (required)

## Security

The Permission Engine supports:
- Entity-level permissions
- Action-level permissions
- Condition-based permissions
- Security group membership
- Tenant isolation

## Extensibility

### Creating Custom Plugins

```java
@Component
public class MyCustomPlugin implements Plugin {
    
    @Override
    public String getName() {
        return "MyCustomPlugin";
    }
    
    @Override
    public void execute(Map<String, Object> data, RuntimeExecutionContext context) {
        // Custom logic
    }
    
    // Implement other interface methods
}
```

## License

Copyright 2024. All rights reserved.

## Support

For issues or questions, please refer to the main EasyBilling repository.
