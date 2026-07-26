-- V401__create_vehicle_schema.sql
-- Creates the vehicle schema and tables for vehicle lifecycle management

CREATE SCHEMA IF NOT EXISTS vehicle;

COMMENT ON SCHEMA vehicle IS 'Vehicle Management: registration, RFID, ownership tracking, de-listing';

-- Enable pg_trgm extension for partial plate search (if not already enabled)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ── vehicle.vehicles ──────────────────────────────────────────────────────────

CREATE TABLE vehicle.vehicles (
    id                 UUID         PRIMARY KEY,
    registration_plate VARCHAR(20)  NOT NULL,
    rfid_number        VARCHAR(50)  UNIQUE,                   -- globally unique when non-null
    current_owner_id   UUID         NOT NULL,                 -- ref to identity.customers (no FK — cross-schema boundary)
    status             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                           CHECK (status IN ('ACTIVE', 'DE_LISTED')),
    created_at         TIMESTAMPTZ  NOT NULL,
    delisted_at        TIMESTAMPTZ,                           -- set when status = DE_LISTED
    version            INTEGER      NOT NULL DEFAULT 0
);

-- Partial unique index: a registration plate may only be ACTIVE once globally on the platform.
-- DE_LISTED plates are excluded, enabling re-registration of the same plate after de-listing.
CREATE UNIQUE INDEX uidx_vehicles_plate_active
    ON vehicle.vehicles (registration_plate)
    WHERE status = 'ACTIVE';

-- Owner + status lookup (list my active vehicles)
CREATE INDEX idx_vehicles_owner_status
    ON vehicle.vehicles (current_owner_id, status);

-- RFID index for fast lookup during session start
CREATE INDEX idx_vehicles_rfid
    ON vehicle.vehicles (rfid_number)
    WHERE rfid_number IS NOT NULL;

-- GIN trigram index for partial plate search (ILIKE queries)
CREATE INDEX idx_vehicles_plate_trgm
    ON vehicle.vehicles USING gin (registration_plate gin_trgm_ops);

COMMENT ON TABLE vehicle.vehicles IS 'Registered vehicles participating in the EV charging platform';
COMMENT ON COLUMN vehicle.vehicles.registration_plate IS 'Normalised uppercase alphanumeric plate (1-20 chars)';
COMMENT ON COLUMN vehicle.vehicles.rfid_number IS 'Optional RFID tag; globally unique when set';
COMMENT ON COLUMN vehicle.vehicles.current_owner_id IS 'Customer who currently owns this vehicle';
COMMENT ON COLUMN vehicle.vehicles.delisted_at IS 'Timestamp of de-listing (soft-delete); NULL for ACTIVE vehicles';
COMMENT ON COLUMN vehicle.vehicles.version IS 'Optimistic locking version counter';

-- ── vehicle.ownership_records ─────────────────────────────────────────────────

CREATE TABLE vehicle.ownership_records (
    id           UUID        PRIMARY KEY,
    vehicle_id   UUID        NOT NULL REFERENCES vehicle.vehicles(id),
    customer_id  UUID        NOT NULL,                        -- ref to identity.customers (no FK — cross-schema boundary)
    start_date   TIMESTAMPTZ NOT NULL,
    end_date     TIMESTAMPTZ                                  -- NULL = currently active ownership
);

CREATE INDEX idx_ownership_vehicle_id
    ON vehicle.ownership_records (vehicle_id);

CREATE INDEX idx_ownership_customer_active
    ON vehicle.ownership_records (customer_id, end_date NULLS FIRST);

COMMENT ON TABLE vehicle.ownership_records IS 'Immutable ownership history tracking which customer owned a vehicle and when';
COMMENT ON COLUMN vehicle.ownership_records.end_date IS 'NULL indicates active (current) ownership';
