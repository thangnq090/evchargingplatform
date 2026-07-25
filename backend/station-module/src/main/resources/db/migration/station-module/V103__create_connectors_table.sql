-- V103__create_connectors_table.sql
-- Creates the connectors table

CREATE TABLE station.connectors (
    id UUID PRIMARY KEY,
    station_id UUID NOT NULL REFERENCES station.stations(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL CHECK (type IN ('CCS', 'CHADEMO', 'TYPE_2')),
    max_power_kw INTEGER NOT NULL CHECK (max_power_kw > 0 AND max_power_kw <= 500),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
        CHECK (status IN ('AVAILABLE', 'IN_USE', 'UNAVAILABLE')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_connectors_station_id ON station.connectors (station_id);

COMMENT ON TABLE station.connectors IS 'Physical charging connectors on a station';
