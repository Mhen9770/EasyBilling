-- Invoices table
CREATE TABLE invoices (
    id VARCHAR(36) PRIMARY KEY,
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    store_id VARCHAR(36) NOT NULL,
    counter_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36),
    customer_name VARCHAR(255),
    customer_phone VARCHAR(20),
    customer_email VARCHAR(255),
    created_by VARCHAR(36) NOT NULL,
    completed_by VARCHAR(36),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    completed_at TIMESTAMP,
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    paid_amount DECIMAL(10,2) DEFAULT 0,
    balance_amount DECIMAL(10,2) DEFAULT 0,
    notes TEXT,
    INDEX idx_tenant_created (tenant_id, created_at),
    INDEX idx_invoice_number (invoice_number),
    INDEX idx_customer (customer_id),
    INDEX idx_store_created (store_id, created_at)
);

-- Invoice items table
CREATE TABLE invoice_items (
    id VARCHAR(36) PRIMARY KEY,
    invoice_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_code VARCHAR(100),
    barcode VARCHAR(100),
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    discount_type VARCHAR(20),
    discount_value DECIMAL(5,2),
    tax_amount DECIMAL(10,2) DEFAULT 0,
    tax_rate DECIMAL(5,2),
    line_total DECIMAL(10,2) NOT NULL,
    notes TEXT,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    INDEX idx_invoice (invoice_id),
    INDEX idx_product (product_id)
);

-- Payments table
CREATE TABLE payments (
    id VARCHAR(36) PRIMARY KEY,
    invoice_id VARCHAR(36) NOT NULL,
    mode VARCHAR(50) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    reference_number VARCHAR(100),
    card_last4 VARCHAR(4),
    upi_id VARCHAR(255),
    paid_at TIMESTAMP NOT NULL,
    notes TEXT,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    INDEX idx_invoice (invoice_id),
    INDEX idx_mode (mode)
);

-- Held invoices table
CREATE TABLE held_invoices (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    store_id VARCHAR(36) NOT NULL,
    counter_id VARCHAR(36) NOT NULL,
    hold_reference VARCHAR(50) UNIQUE NOT NULL,
    invoice_data TEXT NOT NULL,
    held_by VARCHAR(36) NOT NULL,
    held_at TIMESTAMP NOT NULL,
    notes TEXT,
    INDEX idx_tenant_store (tenant_id, store_id),
    INDEX idx_hold_reference (hold_reference)
);
