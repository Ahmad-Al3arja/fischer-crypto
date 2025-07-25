-- V3__Add_default_usage_limit.sql
-- Add default usage limit for promo codes and admin control

-- Update existing promo codes to have default usage limit of 100 if not set
UPDATE promo_codes SET usage_limit = 100 WHERE usage_limit IS NULL OR usage_limit = 0;

-- Add admin setting for default promo code usage limit
INSERT INTO admin_settings (key_name, value, description, created_at, updated_at) VALUES
('default_promo_usage_limit', '100', 'Default usage limit for new promo codes', NOW(), NOW());

-- Verify the setup
SELECT 'Default usage limit setup complete!' as message;
SELECT 
    'Promo codes updated:' as info,
    COUNT(*) as total_promo_codes,
    SUM(CASE WHEN usage_limit = 100 THEN 1 ELSE 0 END) as with_default_limit
FROM promo_codes; 