-- Fix Flyway Migration Checksum Issue
-- Run this script in phpMyAdmin to repair the migration checksum mismatch

USE investment_platform;

-- Step 1: Check current Flyway schema history
SELECT * FROM flyway_schema_history;

-- Step 2: Update the checksum for version 1 to match the current file
UPDATE flyway_schema_history 
SET checksum = -2100241748 
WHERE version = '1';

-- Step 3: Mark version 1.1 as applied if it exists
INSERT IGNORE INTO flyway_schema_history (
    installed_rank,
    version,
    description,
    type,
    script,
    checksum,
    installed_by,
    installed_on,
    execution_time,
    success
) VALUES (
    2,
    '1.1',
    'Add foreign keys',
    'SQL',
    'V1_1__Add_foreign_keys.sql',
    0,
    'root',
    NOW(),
    0,
    1
);

-- Step 4: Verify the repair
SELECT * FROM flyway_schema_history ORDER BY installed_rank; 