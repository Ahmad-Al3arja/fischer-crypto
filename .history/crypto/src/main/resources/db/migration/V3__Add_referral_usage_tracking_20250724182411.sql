-- crypto/src/main/resources/db/migration/V3__Add_referral_usage_tracking.sql

-- Create referral_usage table to track referral code usage limits
CREATE TABLE referral_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    referrer_id BIGINT NOT NULL,
    usage_count INT NOT NULL DEFAULT 0,
    usage_limit INT NOT NULL DEFAULT 100,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Add foreign key constraints
    FOREIGN KEY (referrer_id) REFERENCES users(id),
    
    -- Add unique constraint to ensure one record per referrer
    UNIQUE KEY uk_referral_usage_referrer (referrer_id)
);

-- Create index for better performance
CREATE INDEX idx_referral_usage_active ON referral_usage(is_active);
CREATE INDEX idx_referral_usage_limit ON referral_usage(usage_count, usage_limit);

-- Insert referral usage records for existing users
INSERT INTO referral_usage (referrer_id, usage_count, usage_limit, is_active, created_at, updated_at)
SELECT 
    u.id,
    (SELECT COUNT(*) FROM users ref WHERE ref.referrer_id = u.id) as current_usage,
    100 as default_limit,
    CASE WHEN (SELECT COUNT(*) FROM users ref WHERE ref.referrer_id = u.id) < 100 THEN TRUE ELSE FALSE END as is_active,
    NOW(),
    NOW()
FROM users u 
WHERE u.role = 'USER';

-- Update admin settings to include referral configuration
INSERT INTO admin_settings (key_name, value, description, created_at, updated_at) VALUES
('default_referral_limit', '100', 'Default referral usage limit for new users', NOW(), NOW()),
('referral_system_enabled', 'true', 'Enable or disable referral system', NOW(), NOW()),
('max_referral_limit', '1000', 'Maximum referral limit that can be set', NOW(), NOW());

-- Verify the setup
SELECT 'Referral usage tracking setup complete!' as message;

SELECT 
    'Referral usage records created:' as info,
    COUNT(*) as total_records,
    SUM(usage_count) as total_current_usage,
    AVG(usage_limit) as average_limit
FROM referral_usage;