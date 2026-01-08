-- C2B Transactions Table for M-Pesa Payment Linking
-- Run this in your Supabase SQL Editor

CREATE TABLE IF NOT EXISTS public.c2b_transactions (
  id SERIAL PRIMARY KEY,
  mpesa_receipt VARCHAR NOT NULL UNIQUE,
  phone VARCHAR,
  amount NUMERIC NOT NULL,
  transaction_time TIMESTAMP WITH TIME ZONE DEFAULT now(),
  account_reference VARCHAR,
  customer_name VARCHAR,
  is_linked BOOLEAN DEFAULT false,
  linked_sale_id INTEGER REFERENCES sales(sale_id),
  linked_at TIMESTAMP WITH TIME ZONE,
  linked_by INTEGER,
  station_id INTEGER REFERENCES stations(station_id),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Index for faster queries
CREATE INDEX IF NOT EXISTS idx_c2b_transactions_is_linked ON c2b_transactions(is_linked);
CREATE INDEX IF NOT EXISTS idx_c2b_transactions_station_id ON c2b_transactions(station_id);
CREATE INDEX IF NOT EXISTS idx_c2b_transactions_created_at ON c2b_transactions(created_at DESC);

-- RLS Policies
ALTER TABLE c2b_transactions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow service role full access" ON c2b_transactions
  FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Allow authenticated read" ON c2b_transactions
  FOR SELECT TO authenticated USING (true);

CREATE POLICY "Allow authenticated insert" ON c2b_transactions
  FOR INSERT TO authenticated WITH CHECK (true);

CREATE POLICY "Allow authenticated update" ON c2b_transactions
  FOR UPDATE TO authenticated USING (true) WITH CHECK (true);

-- Grant permissions
GRANT ALL ON c2b_transactions TO authenticated;
GRANT ALL ON c2b_transactions TO service_role;
GRANT USAGE, SELECT ON SEQUENCE c2b_transactions_id_seq TO authenticated;
GRANT USAGE, SELECT ON SEQUENCE c2b_transactions_id_seq TO service_role;
