-- R__seed_dev_sample_data.sql
-- Repeatable Flyway Migration: Comprehensive Dev & Test Sample Data Across All Bounded Contexts
-- Executed whenever checksum changes. Seeds realistic data for Identity, Station, Session, Billing, Payment, and Vehicle modules.

--------------------------------------------------------------------------------
-- 1. IDENTITY & VENDORS
--------------------------------------------------------------------------------

-- Seed Vendors
INSERT INTO identity.vendors (id, name, status, created_at, updated_at)
VALUES 
  ('10000000-0000-0000-0000-000000000001', 'GreenCharge Networks', 'ACTIVE', NOW(), NOW()),
  ('10000000-0000-0000-0000-000000000002', 'VoltPower Solutions', 'ACTIVE', NOW(), NOW()),
  ('10000000-0000-0000-0000-000000000003', 'EcoMobility Charging', 'ACTIVE', NOW(), NOW())
ON CONFLICT (name) DO UPDATE SET status = 'ACTIVE', updated_at = NOW();

-- Seed Vendor Markup Rules
INSERT INTO station.vendor_markups (id, vendor_id, markup_basis_points, effective_from, created_at, updated_at)
VALUES
  ('10000000-0000-0000-0000-000000000101', '10000000-0000-0000-0000-000000000001', 1500, NOW() - INTERVAL '30 days', NOW(), NOW()),
  ('10000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000002', 1000, NOW() - INTERVAL '30 days', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Seed Users (Vendor Admins, Vendor Staff, Customers)
-- Password for all seeded sample accounts: TestPass123! ($2a$12$e0MYzXyjpJS7Pd0RVvHwHe1T9a5FjS5lV.gR/0Rk67w8QWb7y3aGe)
INSERT INTO identity.users (id, name, email, password_hash, role, vendor_id, status, phone, account_number, created_at, updated_at)
VALUES
  -- Vendor Admins
  ('20000000-0000-0000-0000-000000000001', 'GreenCharge Admin', 'admin@greencharge.test', '$2a$12$e0MYzXyjpJS7Pd0RVvHwHe1T9a5FjS5lV.gR/0Rk67w8QWb7y3aGe', 'VENDOR_ADMIN', '10000000-0000-0000-0000-000000000001', 'ACTIVE', '+15550000001', NULL, NOW(), NOW()),
  ('20000000-0000-0000-0000-000000000002', 'VoltPower Admin', 'admin@voltpower.test', '$2a$12$e0MYzXyjpJS7Pd0RVvHwHe1T9a5FjS5lV.gR/0Rk67w8QWb7y3aGe', 'VENDOR_ADMIN', '10000000-0000-0000-0000-000000000002', 'ACTIVE', '+15550000002', NULL, NOW(), NOW()),

  -- Vendor Staff
  ('20000000-0000-0000-0000-000000000011', 'GreenCharge Tech', 'tech@greencharge.test', '$2a$12$e0MYzXyjpJS7Pd0RVvHwHe1T9a5FjS5lV.gR/0Rk67w8QWb7y3aGe', 'VENDOR_USER', '10000000-0000-0000-0000-000000000001', 'ACTIVE', '+15550000011', NULL, NOW(), NOW()),

  -- Customers
  ('30000000-0000-0000-0000-000000000001', 'Alice EV Driver', 'alice.customer@evcharging.test', '$2a$12$e0MYzXyjpJS7Pd0RVvHwHe1T9a5FjS5lV.gR/0Rk67w8QWb7y3aGe', 'CUSTOMER', NULL, 'ACTIVE', '+15559990001', 'ACC-CUST-30000001', NOW(), NOW()),
  ('30000000-0000-0000-0000-000000000002', 'Bob Commuter', 'bob.customer@evcharging.test', '$2a$12$e0MYzXyjpJS7Pd0RVvHwHe1T9a5FjS5lV.gR/0Rk67w8QWb7y3aGe', 'CUSTOMER', NULL, 'ACTIVE', '+15559990002', 'ACC-CUST-30000002', NOW(), NOW())
ON CONFLICT (email) DO UPDATE SET updated_at = NOW();


--------------------------------------------------------------------------------
-- 2. STATIONS & CONNECTORS
--------------------------------------------------------------------------------

INSERT INTO station.stations (id, vendor_id, name, group_label, unit_price_tenth_cents, status, location, created_at, updated_at)
VALUES
  ('40000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'Downtown Plaza Hub #1', 'DOWNTOWN-ZONE', 2000, 'AVAILABLE', ST_SetSRID(ST_MakePoint(-122.4194, 37.7749), 4326), NOW(), NOW()),
  ('40000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 'Airport Express Charger', 'AIRPORT-ZONE', 3500, 'AVAILABLE', ST_SetSRID(ST_MakePoint(-122.3789, 37.6213), 4326), NOW(), NOW()),
  ('40000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002', 'Tech Park Fast Charger', 'NORTH-BAY', 2500, 'AVAILABLE', ST_SetSRID(ST_MakePoint(-122.0840, 37.4220), 4326), NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO station.connectors (id, station_id, type, max_power_kw, status, created_at)
VALUES
  ('40000000-0000-0000-0000-000000000101', '40000000-0000-0000-0000-000000000001', 'CCS', 150, 'AVAILABLE', NOW()),
  ('40000000-0000-0000-0000-000000000102', '40000000-0000-0000-0000-000000000001', 'TYPE_2', 22, 'AVAILABLE', NOW()),
  ('40000000-0000-0000-0000-000000000201', '40000000-0000-0000-0000-000000000002', 'CCS', 350, 'AVAILABLE', NOW()),
  ('40000000-0000-0000-0000-000000000301', '40000000-0000-0000-0000-000000000003', 'CHADEMO', 50, 'AVAILABLE', NOW())
ON CONFLICT (id) DO NOTHING;


--------------------------------------------------------------------------------
-- 3. VEHICLES & RFID
--------------------------------------------------------------------------------

INSERT INTO vehicle.vehicles (id, registration_plate, rfid_number, current_owner_id, status, created_at, version)
VALUES
  ('50000000-0000-0000-0000-000000000001', 'EV-ALICE-1', 'RFID-ALICE-9901', '30000000-0000-0000-0000-000000000001', 'ACTIVE', NOW() - INTERVAL '10 days', 0),
  ('50000000-0000-0000-0000-000000000002', 'EV-BOB-99',  'RFID-BOB-8802',   '30000000-0000-0000-0000-000000000002', 'ACTIVE', NOW() - INTERVAL '5 days', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO vehicle.ownership_records (id, vehicle_id, customer_id, start_date, end_date)
VALUES
  ('50000000-0000-0000-0000-000000000101', '50000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', NOW() - INTERVAL '10 days', NULL),
  ('50000000-0000-0000-0000-000000000102', '50000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', NOW() - INTERVAL '5 days', NULL)
ON CONFLICT (id) DO NOTHING;


--------------------------------------------------------------------------------
-- 4. SESSIONS & METER READINGS
--------------------------------------------------------------------------------

-- Completed Session for Alice
INSERT INTO session.charging_sessions (
  id, station_id, connector_id, customer_id, vehicle_id, status, start_time, end_time,
  unit_rate_amount, unit_rate_currency, total_energy_kwh, total_amount_amount, total_amount_currency, created_at, version
)
VALUES (
  '60000000-0000-0000-0000-000000000001',
  '40000000-0000-0000-0000-000000000001',
  1,
  '30000000-0000-0000-0000-000000000001',
  '50000000-0000-0000-0000-000000000001',
  'COMPLETED',
  NOW() - INTERVAL '2 hours',
  NOW() - INTERVAL '1 hour',
  2.3000,
  'EUR',
  20.0000,
  46.0000,
  'EUR',
  NOW() - INTERVAL '2 hours',
  0
)
ON CONFLICT (id) DO NOTHING;

-- Meter Readings for Completed Session
INSERT INTO session.meter_readings (id, session_id, timestamp, energy_delivered_kwh, power_kw)
VALUES
  ('60000000-0000-0000-0000-000000000101', '60000000-0000-0000-0000-000000000001', NOW() - INTERVAL '1 hour 50 mins', 5.0000, 50.0000),
  ('60000000-0000-0000-0000-000000000102', '60000000-0000-0000-0000-000000000001', NOW() - INTERVAL '1 hour 30 mins', 12.0000, 50.0000),
  ('60000000-0000-0000-0000-000000000103', '60000000-0000-0000-0000-000000000001', NOW() - INTERVAL '1 hour', 20.0000, 0.0000)
ON CONFLICT (id) DO NOTHING;


--------------------------------------------------------------------------------
-- 5. BILLING & INVOICES
--------------------------------------------------------------------------------

INSERT INTO billing.billing_accounts (id, customer_id, balance_amount, balance_currency, total_spent_amount, total_spent_currency, last_billed_at, version)
VALUES
  ('70000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 0.0000, 'EUR', 46.0000, 'EUR', NOW() - INTERVAL '1 hour', 0),
  ('70000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 0.0000, 'EUR', 0.0000, 'EUR', NULL, 0)
ON CONFLICT (customer_id) DO NOTHING;

INSERT INTO billing.invoices (id, session_id, customer_id, vendor_id, total_amount, currency, status, created_at, version)
VALUES (
  '70000000-0000-0000-0000-000000000101',
  '60000000-0000-0000-0000-000000000001',
  '30000000-0000-0000-0000-000000000001',
  '10000000-0000-0000-0000-000000000001',
  46.0000,
  'EUR',
  'PAID',
  NOW() - INTERVAL '1 hour',
  0
)
ON CONFLICT (session_id) DO NOTHING;

INSERT INTO billing.invoice_line_items (id, invoice_id, description, unit_price_amount, unit_price_currency, quantity, total_amount, currency)
VALUES
  ('70000000-0000-0000-0000-000000000201', '70000000-0000-0000-0000-000000000101', 'Base Charging Fee (2.0000 EUR/kWh)', 2.0000, 'EUR', 20.0000, 40.0000, 'EUR'),
  ('70000000-0000-0000-0000-000000000202', '70000000-0000-0000-0000-000000000101', 'Platform Markup Fee (15% Markup)', 0.3000, 'EUR', 20.0000, 6.0000, 'EUR')
ON CONFLICT (id) DO NOTHING;


--------------------------------------------------------------------------------
-- 6. PAYMENTS
--------------------------------------------------------------------------------

INSERT INTO payment.payments (
  id, session_id, customer_id, vehicle_id, charge_point_id, amount, currency, status,
  payment_method_id, provider_payment_id, idempotency_key, created_at, updated_at
)
VALUES (
  '80000000-0000-0000-0000-000000000001',
  '60000000-0000-0000-0000-000000000001',
  '30000000-0000-0000-0000-000000000001',
  '50000000-0000-0000-0000-000000000001',
  '40000000-0000-0000-0000-000000000001',
  46.00,
  'EUR',
  'SUCCESS',
  '90000000-0000-0000-0000-000000000001',
  'mock-pay-tx-600001',
  'KEY-60000000-0000-0000-0000-000000000001',
  NOW() - INTERVAL '55 mins',
  NOW() - INTERVAL '55 mins'
)
ON CONFLICT (idempotency_key) DO NOTHING;

INSERT INTO payment.payment_attempts (id, payment_id, attempt_number, status, error_code, error_message, attempted_at)
VALUES (
  '80000000-0000-0000-0000-000000000101',
  '80000000-0000-0000-0000-000000000001',
  1,
  'SUCCESS',
  NULL,
  NULL,
  NOW() - INTERVAL '55 mins'
)
ON CONFLICT (id) DO NOTHING;
