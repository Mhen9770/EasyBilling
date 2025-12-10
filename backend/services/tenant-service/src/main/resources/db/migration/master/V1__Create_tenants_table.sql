-- Create tenants table in master schema
CREATE TABLE IF NOT EXISTS tenants (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    plan VARCHAR(20) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(20),
    address VARCHAR(500),
    city VARCHAR(50),
    state VARCHAR(50),
    country VARCHAR(50),
    postal_code VARCHAR(20),
    tax_number VARCHAR(50),
    subscription_start_date TIMESTAMP,
    subscription_end_date TIMESTAMP,
    trial_end_date TIMESTAMP,
    max_users INTEGER,
    max_stores INTEGER,
    database_name VARCHAR(100),
    schema_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50)
);

-- Create indexes
CREATE INDEX idx_tenant_slug ON tenants(slug);
CREATE INDEX idx_tenant_status ON tenants(status);
CREATE INDEX idx_tenant_created_at ON tenants(created_at);

-- Comments (MySQL uses table/column comments differently)
-- Master table for all tenants in the platform
-- slug: Unique identifier used for subdomain (e.g., tenant1.easybilling.com)
-- schema_name: MySQL database name for database-per-tenant strategy
