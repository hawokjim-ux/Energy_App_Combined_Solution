-- ============================================================
-- ALPHA ENERGY APP LICENSES TABLE
-- Run this in Supabase SQL Editor
-- ============================================================

-- Create app_licenses table for stringent license management
CREATE TABLE IF NOT EXISTS app_licenses (
    license_id SERIAL PRIMARY KEY,
    license_key VARCHAR(50) NOT NULL UNIQUE,
    license_type VARCHAR(10) NOT NULL,
    client_name VARCHAR(255),
    client_phone VARCHAR(20),
    duration_days INTEGER NOT NULL DEFAULT 360,
    max_devices INTEGER NOT NULL DEFAULT 1,
    
    -- Activation tracking
    is_activated BOOLEAN NOT NULL DEFAULT FALSE,
    activation_device_id VARCHAR(50),
    activation_date TIMESTAMPTZ,
    expiration_date TIMESTAMPTZ,
    
    -- Creation tracking
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) DEFAULT 'superuser',
    
    -- Usage tracking
    activation_count INTEGER NOT NULL DEFAULT 0,
    last_check_date TIMESTAMPTZ,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMPTZ,
    revoked_reason VARCHAR(255),
    
    -- Device info
    device_manufacturer VARCHAR(100),
    device_model VARCHAR(100),
    
    CONSTRAINT valid_license_type CHECK (
        license_type IN ('T02D', 'T10D', 'T20D', 'F02D', 'F03D', 'F04D', 'F05D', 'ENTP')
    )
);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_license_key ON app_licenses(license_key);
CREATE INDEX IF NOT EXISTS idx_device_id ON app_licenses(activation_device_id);
CREATE INDEX IF NOT EXISTS idx_client_phone ON app_licenses(client_phone);

-- Enable Row Level Security
ALTER TABLE app_licenses ENABLE ROW LEVEL SECURITY;

-- Create policy for anonymous access (for app to check licenses)
CREATE POLICY "Allow all operations on app_licenses" ON app_licenses
    FOR ALL
    USING (true)
    WITH CHECK (true);

-- ============================================================
-- SAMPLE DATA (Optional - for testing)
-- ============================================================

-- You can generate licenses from the app using the superuser login

-- ============================================================
-- USEFUL QUERIES
-- ============================================================

-- View all licenses:
-- SELECT * FROM app_licenses ORDER BY created_at DESC;

-- View active licenses:
-- SELECT * FROM app_licenses WHERE is_activated = TRUE AND is_revoked = FALSE;

-- Check license by key:
-- SELECT * FROM app_licenses WHERE license_key = 'ALPHA-ENERGY-46E4-XXXX-XXXX-XXXX';

-- Revoke a license:
-- UPDATE app_licenses SET is_revoked = TRUE, revoked_at = NOW(), revoked_reason = 'Manual revocation' WHERE license_key = 'ALPHA-ENERGY-46E4-XXXX-XXXX-XXXX';
