# 🏗️ EasyBilling Customization Architecture

## Complete Enterprise Customization Framework for Multi-Tenant Billing/POS SaaS

**Version:** 1.0  
**Last Updated:** December 2025  
**Technology Stack:** Spring Boot 3.4.0, React, TypeScript, MySQL 8.0

---

## 📋 Table of Contents

1. [High-Level Architecture](#1-high-level-architecture)
2. [Metadata Engine Design](#2-metadata-engine-design)
3. [Dynamic Entity Design](#3-dynamic-entity-design)
4. [Rule Engine Design](#4-rule-engine-design)
5. [Plugin Framework Design](#5-plugin-framework-design)
6. [Permission System Design](#6-permission-system-design)
7. [Invoice Template System](#7-invoice-template-system)
8. [Workflow Engine](#8-workflow-engine)
9. [Frontend Dynamic UI Architecture](#9-frontend-dynamic-ui-architecture)
10. [Tenant Settings Engine](#10-tenant-settings-engine)
11. [Folder Structures](#11-folder-structures)
12. [Example Configurations](#12-example-configurations)
13. [Best Practices & Anti-Patterns](#13-best-practices--anti-patterns)

---

## 1. High-Level Architecture

### 1.1 System Overview

EasyBilling implements a comprehensive customization framework that enables zero-code tenant-level customization of:

- **Forms and Fields**: Dynamic form rendering with custom fields
- **Business Rules**: Runtime-editable discount, pricing, workflow, and validation rules
- **Plugins**: Extensible add-on architecture for integrations (Tally, WhatsApp, Shopify)
- **Templates**: Customizable invoice templates (thermal and A4)
- **Workflows**: Configurable approval and process flows
- **UI**: Dynamic rendering based on metadata
- **Permissions**: Flexible security group-based access control

### 1.2 Architecture Layers

```
┌─────────────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                               │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐               │
│  │  React SPA   │  │  Dynamic UI  │  │   Theme     │               │
│  │  Next.js     │  │  Renderer    │  │   Engine    │               │
│  └──────────────┘  └──────────────┘  └─────────────┘               │
└─────────────────────────────────┬───────────────────────────────────┘
                                  │ REST API (JSON)
┌─────────────────────────────────▼───────────────────────────────────┐
│                     APPLICATION LAYER                                │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐               │
│  │  Controllers │  │  Services    │  │   DTOs      │               │
│  │  (REST)      │  │  (Business)  │  │   Mappers   │               │
│  └──────────────┘  └──────────────┘  └─────────────┘               │
└─────────────────────────────────┬───────────────────────────────────┘
                                  │
┌─────────────────────────────────▼───────────────────────────────────┐
│                     CUSTOMIZATION ENGINE LAYER                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │   Metadata   │  │     Rule     │  │    Plugin    │              │
│  │    Engine    │  │    Engine    │  │   Framework  │              │
│  └──────────────┘  └──────────────┘  └──────────────┘              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │   Template   │  │   Workflow   │  │   Dynamic    │              │
│  │    Engine    │  │    Engine    │  │  Validator   │              │
│  └──────────────┘  └──────────────┘  └──────────────┘              │
└─────────────────────────────────┬───────────────────────────────────┘
                                  │
┌─────────────────────────────────▼───────────────────────────────────┐
│                     PERSISTENCE LAYER                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │  JPA Entities│  │  Repositories│  │  Hibernate   │              │
│  │  + JSON cols │  │  (Spring)    │  │  Filters     │              │
│  └──────────────┘  └──────────────┘  └──────────────┘              │
└─────────────────────────────────┬───────────────────────────────────┘
                                  │
┌─────────────────────────────────▼───────────────────────────────────┐
│                     DATA LAYER                                       │
│  ┌──────────────────────────────────────────────────────────┐       │
│  │  MySQL Database (Single Schema, Multi-Tenant)           │       │
│  │  - Entity Tables (tenant_id column)                     │       │
│  │  - Metadata Tables                                       │       │
│  │  - Rule Tables                                           │       │
│  │  - Plugin Registry                                       │       │
│  │  - JSON Columns for Dynamic Attributes                  │       │
│  └──────────────────────────────────────────────────────────┘       │
│  ┌──────────────────────────────────────────────────────────┐       │
│  │  Redis Cache                                             │       │
│  │  - Metadata Cache                                         │       │
│  │  - Rule Cache                                             │       │
│  │  - Session Cache                                          │       │
│  └──────────────────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────────────────┘
```

### 1.3 Key Design Decisions

| Aspect | Decision | Rationale |
|--------|----------|-----------|
| Multi-tenancy | Single schema with tenant_id | Simpler operations, easier backups, cost-effective |
| Dynamic Fields | JSON columns | Flexibility without schema migrations |
| Rule Storage | JSON in database | Version control, audit trail, easy rollback |
| Caching | Redis + Spring Cache | Performance, metadata changes infrequent |
| API Design | RESTful JSON | Industry standard, easy integration |
| Rule Engine | Custom with SpEL support | Full control, no external dependencies |
| Template Engine | JSON-based with Thymeleaf | Flexible, supports both thermal & A4 |

---

## 2. Metadata Engine Design

### 2.1 Purpose

The Metadata Engine is the cornerstone of customization, defining:
- Form structures and layouts
- Field definitions (type, validation, display)
- UI rendering rules
- Data validation schemas

### 2.2 Database Schema

Create the metadata tables as follows:

```sql
-- Form Metadata: Defines form structure
CREATE TABLE form_metadata (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id INT NOT NULL,
    entity_type VARCHAR(100) NOT NULL COMMENT 'product, customer, invoice, etc.',
    form_name VARCHAR(100) NOT NULL COMMENT 'create_product, edit_customer',
    display_label VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(50) COMMENT 'sales, inventory, customer',
    is_active BOOLEAN DEFAULT TRUE,
    display_order INT DEFAULT 0,
    layout_config JSON COMMENT 'Grid layout, sections, columns',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    INDEX idx_form_tenant (tenant_id),
    INDEX idx_form_entity (entity_type),
    INDEX idx_form_active (is_active),
    UNIQUE KEY uk_form_tenant_entity (tenant_id, entity_type, form_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Field Metadata: Defines individual fields
CREATE TABLE field_metadata (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id INT NOT NULL,
    form_id VARCHAR(36) COMMENT 'NULL for standalone fields',
    entity_type VARCHAR(100) NOT NULL,
    field_name VARCHAR(100) NOT NULL COMMENT 'DB column or JSON path',
    field_label VARCHAR(200) NOT NULL,
    field_type VARCHAR(50) NOT NULL COMMENT 'text, number, select, date, etc.',
    data_type VARCHAR(50) NOT NULL COMMENT 'string, integer, decimal, boolean',
    default_value TEXT,
    placeholder VARCHAR(200),
    help_text TEXT,
    validation_rules JSON COMMENT 'Validation configuration',
    display_order INT DEFAULT 0,
    is_required BOOLEAN DEFAULT FALSE,
    is_readonly BOOLEAN DEFAULT FALSE,
    is_visible BOOLEAN DEFAULT TRUE,
    is_searchable BOOLEAN DEFAULT FALSE,
    is_sortable BOOLEAN DEFAULT FALSE,
    options JSON COMMENT 'For select/radio/checkbox fields',
    conditional_logic JSON COMMENT 'Show/hide based on other fields',
    ui_attributes JSON COMMENT 'Additional HTML/UI attributes',
    grid_column VARCHAR(50) COMMENT 'CSS grid column position',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    FOREIGN KEY (form_id) REFERENCES form_metadata(id) ON DELETE CASCADE,
    INDEX idx_field_tenant (tenant_id),
    INDEX idx_field_form (form_id),
    INDEX idx_field_entity (entity_type),
    INDEX idx_field_name (field_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Metadata Cache Control
CREATE TABLE metadata_cache_control (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id INT NOT NULL UNIQUE,
    last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    cache_version VARCHAR(50),
    metadata_hash VARCHAR(64) COMMENT 'Hash of all metadata for integrity',
    INDEX idx_cache_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

