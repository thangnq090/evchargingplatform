-- V102__create_stations_table.sql
-- Creates the stations table with PostGIS location

CREATE TABLE station.stations (
    id UUID PRIMARY KEY,
    vendor_id UUID NOT NULL REFERENCES identity.vendors(id),
    name VARCHAR(100) NOT NULL,
    group_label VARCHAR(50),
    unit_price_tenth_cents INTEGER NOT NULL DEFAULT 0 CHECK (unit_price_tenth_cents >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
        CHECK (status IN ('AVAILABLE', 'UNAVAILABLE', 'MAINTENANCE')),
    location GEOGRAPHY(Point, 4326) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

-- Unique constraint: vendor_id + name (only for non-deleted)
CREATE UNIQUE INDEX idx_stations_vendor_name_unique
    ON station.stations (vendor_id, name)
    WHERE deleted_at IS NULL;

-- Indexes
CREATE INDEX idx_stations_vendor_id ON station.stations (vendor_id);
CREATE INDEX idx_stations_status ON station.stations (status);
CREATE INDEX idx_stations_location ON station.stations USING GIST (location);
CREATE INDEX idx_stations_deleted_at ON station.stations (deleted_at) WHERE deleted_at IS NOT NULL;

COMMENT ON TABLE station.stations IS 'Charging stations (chargepoints) owned by vendors';
COMMENT ON COLUMN station.stations.unit_price_tenth_cents IS 'Price per kWh in tenths of cents (integer to avoid floating point)';
COMMENT ON COLUMN station.stations.location IS 'WGS 84 coordinates (EPSG:4326) as PostGIS GEOGRAPHY';
