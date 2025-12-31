-- =====================================================
-- ENABLE SUPABASE REALTIME FOR M-PESA TRANSACTIONS
-- =====================================================
-- Run this in Supabase SQL Editor to enable real-time updates
-- for instant M-Pesa payment notifications in the mobile app
--
-- NOTE: Run each statement one at a time if you encounter errors

-- =====================================================
-- STEP 1: ENABLE REALTIME FOR TABLES
-- =====================================================
-- Enable Realtime for mpesa_transactions table
-- This allows the mobile app to receive instant updates when
-- transaction status changes from 'pending' to 'completed'

ALTER PUBLICATION supabase_realtime ADD TABLE mpesa_transactions;

-- Enable Realtime for sales table (optional - for dashboard updates)
ALTER PUBLICATION supabase_realtime ADD TABLE sales;


-- =====================================================
-- STEP 2: ADD PERFORMANCE INDEXES (if not exist)
-- =====================================================
-- These indexes speed up lookups and realtime filtering

CREATE INDEX IF NOT EXISTS idx_mpesa_transactions_checkout_request_id 
ON mpesa_transactions(checkout_request_id);

CREATE INDEX IF NOT EXISTS idx_mpesa_transactions_status 
ON mpesa_transactions(status);

CREATE INDEX IF NOT EXISTS idx_mpesa_transactions_created_at 
ON mpesa_transactions(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_sales_checkout_request_id 
ON sales(checkout_request_id);

CREATE INDEX IF NOT EXISTS idx_sales_transaction_status 
ON sales(transaction_status);

CREATE INDEX IF NOT EXISTS idx_mpesa_token_cache_expires 
ON mpesa_token_cache(expires_at);


-- =====================================================
-- STEP 3: GRANT REALTIME PERMISSIONS
-- =====================================================
-- Grant necessary permissions for anon role to receive realtime updates

GRANT SELECT ON mpesa_transactions TO anon;
GRANT SELECT ON sales TO anon;


-- =====================================================
-- VERIFY REALTIME IS ENABLED
-- =====================================================
-- Run this query to verify realtime is enabled for your tables:
-- SELECT * FROM pg_publication_tables WHERE pubname = 'supabase_realtime';

-- Expected output should include:
-- - mpesa_transactions
-- - sales


-- =====================================================
-- NOTES ON REALTIME PERFORMANCE
-- =====================================================
-- 
-- With Realtime enabled, the mobile app will:
-- 1. Receive INSTANT payment confirmations (< 1 second)
-- 2. Use less battery (no repeated API polling)
-- 3. Provide better user experience
--
-- The flow is now:
-- 1. Mobile app sends STK Push request
-- 2. Mobile app subscribes to realtime channel
-- 3. Customer enters M-Pesa PIN
-- 4. Safaricom calls your callback
-- 5. Callback updates mpesa_transactions table
-- 6. Supabase Realtime pushes update to mobile app
-- 7. Mobile app shows instant success notification
--
-- Total time from PIN entry to confirmation: < 1 second!
