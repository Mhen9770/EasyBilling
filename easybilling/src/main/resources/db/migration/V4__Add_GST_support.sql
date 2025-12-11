-- Add GST support for India market

-- GST rates master table
CREATE TABLE IF NOT EXISTS gst_rates (
    id VARCHAR(36) PRIMARY KEY,
    hsn_code VARCHAR(20),
    sac_code VARCHAR(20),
    tax_category VARCHAR(50) NOT NULL,
    cgst_rate DECIMAL(5,2) NOT NULL DEFAULT 0,
    sgst_rate DECIMAL(5,2) NOT NULL DEFAULT 0,
    igst_rate DECIMAL(5,2) NOT NULL DEFAULT 0,
    cess_rate DECIMAL(5,2) DEFAULT 0,
    effective_from DATE NOT NULL,
    effective_to DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    INDEX idx_hsn_code (hsn_code),
    INDEX idx_sac_code (sac_code),
    INDEX idx_active (is_active)
);

-- Update invoice_items table for GST
ALTER TABLE invoice_items 
ADD COLUMN IF NOT EXISTS hsn_code VARCHAR(20),
ADD COLUMN IF NOT EXISTS sac_code VARCHAR(20),
ADD COLUMN IF NOT EXISTS cgst_rate DECIMAL(5,2) DEFAULT 0,
ADD COLUMN IF NOT EXISTS sgst_rate DECIMAL(5,2) DEFAULT 0,
ADD COLUMN IF NOT EXISTS igst_rate DECIMAL(5,2) DEFAULT 0,
ADD COLUMN IF NOT EXISTS cess_rate DECIMAL(5,2) DEFAULT 0,
ADD COLUMN IF NOT EXISTS cgst_amount DECIMAL(10,2) DEFAULT 0,
ADD COLUMN IF NOT EXISTS sgst_amount DECIMAL(10,2) DEFAULT 0,
ADD COLUMN IF NOT EXISTS igst_amount DECIMAL(10,2) DEFAULT 0,
ADD COLUMN IF NOT EXISTS cess_amount DECIMAL(10,2) DEFAULT 0;

-- Update invoices table for GST
ALTER TABLE invoices
ADD COLUMN IF NOT EXISTS total_cgst DECIMAL(10,2) DEFAULT 0,
ADD COLUMN IF NOT EXISTS total_sgst DECIMAL(10,2) DEFAULT 0,
ADD COLUMN IF NOT EXISTS total_igst DECIMAL(10,2) DEFAULT 0,
ADD COLUMN IF NOT EXISTS total_cess DECIMAL(10,2) DEFAULT 0,
ADD COLUMN IF NOT EXISTS place_of_supply VARCHAR(50),
ADD COLUMN IF NOT EXISTS supplier_gstin VARCHAR(15),
ADD COLUMN IF NOT EXISTS customer_gstin VARCHAR(15),
ADD COLUMN IF NOT EXISTS reverse_charge BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS is_interstate BOOLEAN DEFAULT FALSE;

-- Update tenants table for GSTIN
ALTER TABLE tenants
ADD COLUMN IF NOT EXISTS gstin VARCHAR(15),
ADD COLUMN IF NOT EXISTS state_code VARCHAR(2),
ADD COLUMN IF NOT EXISTS pan_number VARCHAR(10);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_invoice_gstin ON invoices(supplier_gstin);
CREATE INDEX IF NOT EXISTS idx_invoice_place_supply ON invoices(place_of_supply);
CREATE INDEX IF NOT EXISTS idx_tenant_gstin ON tenants(gstin);

-- Insert common GST rates (as of 2024)
INSERT INTO gst_rates (id, tax_category, cgst_rate, sgst_rate, igst_rate, cess_rate, effective_from, is_active) VALUES
(UUID(), 'GST_0', 0, 0, 0, 0, '2017-07-01', TRUE),
(UUID(), 'GST_5', 2.5, 2.5, 5, 0, '2017-07-01', TRUE),
(UUID(), 'GST_12', 6, 6, 12, 0, '2017-07-01', TRUE),
(UUID(), 'GST_18', 9, 9, 18, 0, '2017-07-01', TRUE),
(UUID(), 'GST_28', 14, 14, 28, 0, '2017-07-01', TRUE);

-- Comments
-- hsn_code: Harmonized System of Nomenclature (for goods)
-- sac_code: Service Accounting Code (for services)
-- CGST: Central GST (intra-state)
-- SGST: State GST (intra-state)
-- IGST: Integrated GST (inter-state)
-- CESS: Compensation Cess (additional tax on luxury items)
