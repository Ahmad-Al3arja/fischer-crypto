-- Migration V4: Update withdrawal fee to 12%
-- This migration updates the withdrawal fee percentage from 2% to 12%

-- Update admin settings
UPDATE admin_settings 
SET value = '12.00', 
    updated_at = NOW() 
WHERE key_name = 'withdrawal_fee';

-- Insert if not exists (for new installations)
INSERT IGNORE INTO admin_settings (key_name, value, description, created_at, updated_at) 
VALUES ('withdrawal_fee', '12.00', 'Withdrawal fee percentage (12%)', NOW(), NOW());

-- Update system settings withdrawal fee
UPDATE system_settings 
SET value = '12.00', 
    updated_at = NOW() 
WHERE category = 'WITHDRAWAL' AND key_name = 'fee_percentage';

-- Insert if not exists in system settings
INSERT IGNORE INTO system_settings (category, key_name, value, data_type, description, is_editable, created_at, updated_at) 
VALUES ('WITHDRAWAL', 'fee_percentage', '12.00', 'DECIMAL', 'Withdrawal fee percentage (12%)', TRUE, NOW(), NOW());

SELECT 'Withdrawal fee updated to 12% successfully!' as migration_status; 