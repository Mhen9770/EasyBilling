-- Tenant Schema Migration V2: Insert master/default data
-- This migration adds initial configuration and reference data for new tenants

-- Insert default categories
INSERT INTO categories (id, name, description, parent_id, is_active, created_at) VALUES
(1, 'Electronics', 'Electronic devices and accessories', NULL, TRUE, CURRENT_TIMESTAMP),
(2, 'Clothing', 'Apparel and fashion items', NULL, TRUE, CURRENT_TIMESTAMP),
(3, 'Food & Beverages', 'Food items and drinks', NULL, TRUE, CURRENT_TIMESTAMP),
(4, 'Home & Kitchen', 'Home appliances and kitchen items', NULL, TRUE, CURRENT_TIMESTAMP),
(5, 'Health & Beauty', 'Health care and beauty products', NULL, TRUE, CURRENT_TIMESTAMP),
(6, 'Sports & Outdoors', 'Sports equipment and outdoor gear', NULL, TRUE, CURRENT_TIMESTAMP),
(7, 'Books & Stationery', 'Books, office supplies, and stationery', NULL, TRUE, CURRENT_TIMESTAMP),
(8, 'Toys & Games', 'Toys, games, and entertainment', NULL, TRUE, CURRENT_TIMESTAMP);

-- Insert sub-categories for Electronics
INSERT INTO categories (id, name, description, parent_id, is_active, created_at) VALUES
(101, 'Mobile Phones', 'Smartphones and feature phones', 1, TRUE, CURRENT_TIMESTAMP),
(102, 'Laptops & Computers', 'Laptops, desktops, and accessories', 1, TRUE, CURRENT_TIMESTAMP),
(103, 'Audio & Video', 'Headphones, speakers, cameras', 1, TRUE, CURRENT_TIMESTAMP),
(104, 'Smart Devices', 'Smartwatches, fitness trackers', 1, TRUE, CURRENT_TIMESTAMP);

-- Insert sub-categories for Clothing
INSERT INTO categories (id, name, description, parent_id, is_active, created_at) VALUES
(201, 'Men''s Clothing', 'Men''s apparel', 2, TRUE, CURRENT_TIMESTAMP),
(202, 'Women''s Clothing', 'Women''s apparel', 2, TRUE, CURRENT_TIMESTAMP),
(203, 'Kids Clothing', 'Children''s apparel', 2, TRUE, CURRENT_TIMESTAMP),
(204, 'Footwear', 'Shoes and sandals', 2, TRUE, CURRENT_TIMESTAMP);

-- Insert sub-categories for Food & Beverages
INSERT INTO categories (id, name, description, parent_id, is_active, created_at) VALUES
(301, 'Snacks', 'Chips, biscuits, and snacks', 3, TRUE, CURRENT_TIMESTAMP),
(302, 'Beverages', 'Soft drinks, juices, water', 3, TRUE, CURRENT_TIMESTAMP),
(303, 'Groceries', 'Daily grocery items', 3, TRUE, CURRENT_TIMESTAMP),
(304, 'Dairy Products', 'Milk, cheese, yogurt', 3, TRUE, CURRENT_TIMESTAMP);

-- Insert default brands
INSERT INTO brands (id, name, description, is_active, created_at) VALUES
(1, 'Generic', 'Generic/Unbranded products', TRUE, CURRENT_TIMESTAMP),
(2, 'House Brand', 'Store''s own brand', TRUE, CURRENT_TIMESTAMP);

-- Note: Additional brands will be added by users as needed

-- Reset AUTO_INCREMENT for proper ID sequencing
-- Categories start at 1000 for user-added categories
ALTER TABLE categories AUTO_INCREMENT = 1000;

-- Brands start at 100 for user-added brands
ALTER TABLE brands AUTO_INCREMENT = 100;

-- Products will start at 1 (no pre-populated products)
ALTER TABLE products AUTO_INCREMENT = 1;
