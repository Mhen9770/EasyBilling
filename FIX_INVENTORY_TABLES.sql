-- Fix Inventory Service: Drop Flyway history table to force re-migration
-- Run this in MySQL to fix the missing tables issue

USE easybilling;

-- Drop the Flyway history table for inventory service
DROP TABLE IF EXISTS flyway_inventory_schema_history;

-- After running this, restart the inventory service
-- It will re-run the migration and create all tables

