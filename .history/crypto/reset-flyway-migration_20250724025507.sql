-- Reset Flyway Migration (Alternative Fix)
-- Run this script in phpMyAdmin to completely reset Flyway schema history

USE investment_platform;

-- Step 1: Drop the Flyway schema history table
DROP TABLE IF EXISTS flyway_schema_history;

-- Step 2: Verify the table is dropped
SHOW TABLES LIKE 'flyway_schema_history';

-- Step 3: The application will recreate the table and run all migrations from scratch
-- This will resolve the checksum mismatch issue 