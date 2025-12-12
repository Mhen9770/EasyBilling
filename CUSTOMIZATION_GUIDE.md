# Customization System - Implementation Guide

## Overview

The EasyBilling platform now includes a comprehensive customization framework that allows customers to tailor the software to their specific business needs without requiring code changes. This makes EasyBilling stand out from competitors by offering enterprise-level flexibility.

## Key Features

### 1. **Configuration Management**
Multi-level configuration system with hierarchy:
- **System Level**: Default configurations for all tenants
- **Tenant Level**: Override system defaults per tenant
- **Hierarchical Lookup**: Tenant config overrides system config automatically

**Benefits:**
- No code changes needed for business rule modifications
- Instant updates without deployments
- Centralized configuration with caching

### 2. **Custom Themes & Branding**
Complete white-label customization:
- **Color Scheme**: Primary, secondary, accent, success, warning, error colors
- **Typography**: Custom fonts for headings and body text
- **Branding**: Logo, favicon, company name, tagline
- **Layout**: Sidebar position, layout mode (light/dark), border radius
- **Custom CSS**: Advanced styling with custom CSS
- **Invoice Branding**: Separate logo, footer text, watermark for invoices

**Benefits:**
- Complete brand identity control
- Professional appearance matching company branding
- Multiple themes for different use cases

### 3. **Custom Fields**
Dynamically add fields to any entity:
- **Supported Entities**: Invoices, Customers, Products, Suppliers
- **Field Types**: Text, Number, Date, Boolean, Dropdown, Multi-select, Email, Phone, URL
- **Validation**: Required fields, min/max values, regex patterns
- **Display Options**: Show in lists, searchable, display order
- **Rich Metadata**: Labels, descriptions, placeholders, help text

**Benefits:**
- Adapt to industry-specific requirements
- Capture custom data without schema changes
- Support diverse business processes

### 4. **Document Templates**
Customizable templates for all documents:
- **Template Types**: Invoice, Receipt, Quotation, Delivery Note, Credit Note, Statement
- **Formats**: HTML, PDF, Excel, CSV
- **Page Settings**: Size (A4, Letter, Thermal), orientation, margins
- **Content Customization**: Header, footer, main content, CSS styling
- **Display Options**: Toggle logo, company details, tax details, terms

**Benefits:**
- Professional document presentation
- Compliance with regional requirements
- Brand consistency across all documents

### 5. **Workflows & Automation**
Visual workflow builder for business automation:
- **Triggers**: Invoice created/paid, Customer created, Low stock, etc.
- **Conditions**: Field-based conditions with operators
- **Actions**: Send email, create task, update record, call webhook
- **Execution Control**: Order priority, active/inactive status
- **Monitoring**: Execution count, failure tracking

**Benefits:**
- Automate repetitive tasks
- Enforce business rules automatically
- Reduce manual errors

### 6. **Webhooks**
Event-driven integrations:
- **Events**: All major entity lifecycle events
- **HTTP Methods**: POST, PUT, PATCH
- **Custom Headers**: Authorization, content-type, custom headers
- **Payload Templates**: Customize JSON payload with merge fields
- **Security**: Secret key for HMAC signatures
- **Reliability**: Retry logic with configurable intervals
- **Monitoring**: Success/failure counts, last triggered time

**Benefits:**
- Real-time integration with external systems
- Event-driven architecture
- Flexible data exchange

## Database Schema

### Core Tables

#### system_configurations
Global configuration settings that apply to all tenants.

```sql
CREATE TABLE system_configurations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    category VARCHAR(50),
    data_type VARCHAR(20),
    description VARCHAR(500),
    is_editable BOOLEAN DEFAULT TRUE,
    is_sensitive BOOLEAN DEFAULT FALSE,
    default_value TEXT,
    validation_rule VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    INDEX idx_config_key (config_key),
    INDEX idx_config_category (category)
);
```

#### tenant_configurations
Tenant-specific configuration that overrides system defaults.

```sql
CREATE TABLE tenant_configurations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT,
    category VARCHAR(50),
    data_type VARCHAR(20),
    description VARCHAR(500),
    is_override BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    UNIQUE KEY idx_tenant_config_key (tenant_id, config_key),
    INDEX idx_tenant_config_category (tenant_id, category)
);
```

#### custom_themes
Branding and theme customization per tenant.

```sql
CREATE TABLE custom_themes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL UNIQUE,
    theme_name VARCHAR(100) NOT NULL,
    primary_color VARCHAR(7),
    secondary_color VARCHAR(7),
    accent_color VARCHAR(7),
    background_color VARCHAR(7),
    text_color VARCHAR(7),
    success_color VARCHAR(7),
    warning_color VARCHAR(7),
    error_color VARCHAR(7),
    logo_url VARCHAR(500),
    favicon_url VARCHAR(500),
    company_name VARCHAR(200),
    tagline VARCHAR(500),
    font_family VARCHAR(100),
    heading_font VARCHAR(100),
    custom_css TEXT,
    sidebar_position VARCHAR(20),
    layout_mode VARCHAR(20),
    border_radius VARCHAR(10),
    invoice_logo_url VARCHAR(500),
    invoice_footer_text TEXT,
    invoice_watermark VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    INDEX idx_theme_tenant (tenant_id),
    INDEX idx_theme_active (is_active)
);
```

#### custom_fields
Define custom fields for entities.

```sql
CREATE TABLE custom_fields (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    field_label VARCHAR(200) NOT NULL,
    field_type VARCHAR(20) NOT NULL,
    field_description VARCHAR(500),
    default_value VARCHAR(500),
    dropdown_options TEXT,
    validation_rules TEXT,
    is_required BOOLEAN DEFAULT FALSE,
    is_searchable BOOLEAN DEFAULT FALSE,
    is_displayed_in_list BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    placeholder_text VARCHAR(200),
    help_text VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    INDEX idx_custom_field_entity (tenant_id, entity_type),
    INDEX idx_custom_field_active (is_active)
);
```

#### custom_field_values
Store actual values for custom fields.

```sql
CREATE TABLE custom_field_values (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL,
    custom_field_id BIGINT NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    field_value TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    INDEX idx_custom_field_value_entity (tenant_id, entity_type, entity_id),
    INDEX idx_custom_field_value_field (custom_field_id)
);
```

#### document_templates
Customizable document templates.

```sql
CREATE TABLE document_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL,
    template_name VARCHAR(100) NOT NULL,
    template_type VARCHAR(50) NOT NULL,
    template_format VARCHAR(20),
    template_content LONGTEXT,
    template_css TEXT,
    header_content TEXT,
    footer_content TEXT,
    page_size VARCHAR(20),
    page_orientation VARCHAR(20),
    margin_top INT,
    margin_bottom INT,
    margin_left INT,
    margin_right INT,
    show_logo BOOLEAN DEFAULT TRUE,
    show_company_details BOOLEAN DEFAULT TRUE,
    show_tax_details BOOLEAN DEFAULT TRUE,
    show_terms BOOLEAN DEFAULT TRUE,
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    INDEX idx_template_tenant_type (tenant_id, template_type),
    INDEX idx_template_default (tenant_id, is_default)
);
```

#### custom_workflows
Automated business workflows.

```sql
CREATE TABLE custom_workflows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL,
    workflow_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    trigger_event VARCHAR(50) NOT NULL,
    conditions TEXT,
    actions TEXT,
    execution_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    last_executed_at TIMESTAMP,
    execution_count BIGINT DEFAULT 0,
    failure_count BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    INDEX idx_workflow_tenant (tenant_id),
    INDEX idx_workflow_trigger (trigger_event),
    INDEX idx_workflow_active (is_active)
);
```

#### webhooks
Webhook configurations for external integrations.

```sql
CREATE TABLE webhooks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL,
    webhook_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    event_type VARCHAR(50) NOT NULL,
    target_url VARCHAR(500) NOT NULL,
    http_method VARCHAR(10) DEFAULT 'POST',
    headers TEXT,
    payload_template TEXT,
    secret_key VARCHAR(100),
    retry_count INT DEFAULT 3,
    retry_interval INT DEFAULT 300,
    timeout INT DEFAULT 30,
    is_active BOOLEAN DEFAULT TRUE,
    last_triggered_at TIMESTAMP,
    success_count BIGINT DEFAULT 0,
    failure_count BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    INDEX idx_webhook_tenant (tenant_id),
    INDEX idx_webhook_event (event_type),
    INDEX idx_webhook_active (is_active)
);
```

## API Endpoints

### Configuration Management

```
GET    /api/v1/configurations/system                    - Get all system configurations
GET    /api/v1/configurations/system/category/{category} - Get system configs by category
POST   /api/v1/configurations/system                    - Create system configuration
PUT    /api/v1/configurations/system/{configKey}        - Update system configuration

GET    /api/v1/configurations/tenant                    - Get tenant configurations
GET    /api/v1/configurations/tenant/category/{category} - Get tenant configs by category
POST   /api/v1/configurations/tenant                    - Create/update tenant configuration
DELETE /api/v1/configurations/tenant/{configKey}        - Delete tenant configuration

GET    /api/v1/configurations/value/{configKey}         - Get config value (with tenant override)
```

### Theme Customization

```
GET    /api/v1/customization/theme      - Get current tenant theme
POST   /api/v1/customization/theme      - Create/update theme
DELETE /api/v1/customization/theme      - Delete theme (revert to default)
```

### Custom Fields

```
GET    /api/v1/customization/fields                      - Get all custom fields
GET    /api/v1/customization/fields/entity/{entityType}  - Get fields for entity type
POST   /api/v1/customization/fields                      - Create custom field
PUT    /api/v1/customization/fields/{id}                 - Update custom field
DELETE /api/v1/customization/fields/{id}                 - Delete custom field

GET    /api/v1/customization/fields/values/{entityType}/{entityId}  - Get field values
POST   /api/v1/customization/fields/values/{entityType}/{entityId}  - Save field values
```

## Usage Examples

### 1. Setting Configuration Values

```java
// System-wide configuration
SystemConfigurationDTO config = SystemConfigurationDTO.builder()
    .configKey("billing.invoice.default_payment_terms")
    .configValue("30")
    .category("billing")
    .dataType("INTEGER")
    .description("Default payment terms in days")
    .isEditable(true)
    .build();
configurationService.createSystemConfiguration(config);

// Tenant-specific override
TenantConfigurationDTO tenantConfig = TenantConfigurationDTO.builder()
    .tenantId(1)
    .configKey("billing.invoice.default_payment_terms")
    .configValue("45")
    .category("billing")
    .dataType("INTEGER")
    .build();
configurationService.createOrUpdateTenantConfiguration(tenantConfig);

// Retrieve value (automatically uses tenant override if exists)
String paymentTerms = configurationService.getConfigValue("billing.invoice.default_payment_terms");
```

### 2. Applying Custom Theme

```java
CustomThemeDTO theme = CustomThemeDTO.builder()
    .themeName("Corporate Blue")
    .primaryColor("#1E3A8A")
    .secondaryColor("#64748B")
    .accentColor("#0EA5E9")
    .logoUrl("https://cdn.example.com/logo.png")
    .companyName("ACME Corporation")
    .fontFamily("Roboto")
    .layoutMode("light")
    .build();
customThemeService.createOrUpdateTheme(theme);
```

### 3. Adding Custom Fields

```java
// Create custom field for invoices
CustomFieldDTO field = CustomFieldDTO.builder()
    .entityType("INVOICE")
    .fieldName("purchase_order_number")
    .fieldLabel("PO Number")
    .fieldType("TEXT")
    .fieldDescription("Customer purchase order reference")
    .isRequired(false)
    .isSearchable(true)
    .placeholderText("Enter PO number")
    .build();
customFieldService.createCustomField(field);

// Save custom field value
Map<Long, String> fieldValues = new HashMap<>();
fieldValues.put(fieldId, "PO-2024-001");
customFieldService.saveCustomFieldValues(tenantId, "INVOICE", invoiceId, fieldValues);
```

## Competitive Advantages

1. **No-Code Customization**: Customers can configure the system through UI without technical expertise
2. **Multi-Level Configuration**: System, tenant, and user-level settings with automatic hierarchy
3. **Complete Branding Control**: Full white-label capability for resellers and enterprises
4. **Dynamic Schema**: Add custom fields without database migrations
5. **Visual Workflow Builder**: Create automation without coding
6. **Real-Time Integrations**: Webhook support for event-driven architecture
7. **Template Flexibility**: Customizable documents for compliance and branding
8. **Scalable Architecture**: Efficient caching and database design

## Configuration Categories

### Billing
- Invoice prefix/suffix
- Payment terms
- Tax calculations
- Discount rules
- Currency settings

### Customer
- Loyalty program rules
- Segment thresholds
- Wallet settings
- Credit limits

### Inventory
- Low stock thresholds
- Reorder quantities
- Stock valuation methods
- Batch/expiry tracking

### Notifications
- Email templates
- SMS settings
- Alert thresholds
- Notification channels

### Security
- Password policies
- Session timeouts
- IP restrictions
- MFA requirements

### UI/UX
- Date/time formats
- Number formats
- Language preferences
- Dashboard layouts

## Best Practices

1. **Configuration Naming**: Use dot notation (e.g., `billing.invoice.prefix`)
2. **Data Types**: Always specify data type for type-safe retrieval
3. **Sensitive Data**: Mark sensitive configs (passwords, API keys) as sensitive
4. **Validation**: Define validation rules for configuration values
5. **Caching**: Leverage Redis caching for frequently accessed configs
6. **Documentation**: Add clear descriptions for all configurations
7. **Defaults**: Always provide sensible default values

## Future Enhancements

1. **Configuration Versioning**: Track changes and rollback capability
2. **Configuration Import/Export**: Bulk configuration management
3. **Visual Workflow Designer**: Drag-and-drop workflow builder UI
4. **Template Marketplace**: Pre-built templates for different industries
5. **A/B Testing**: Multiple theme variants with analytics
6. **Configuration Audit**: Track who changed what and when
7. **Smart Recommendations**: AI-powered configuration suggestions
8. **Plugin System**: Extensible architecture for custom plugins

## Conclusion

The customization system transforms EasyBilling from a standard billing application into a highly adaptable platform that can meet diverse business requirements. This flexibility is a key differentiator that will attract customers who need tailored solutions without the cost of custom development.
