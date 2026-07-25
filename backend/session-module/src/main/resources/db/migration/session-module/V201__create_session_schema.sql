-- V201__create_session_schema.sql
-- Creates the session schema and tables for session management

CREATE SCHEMA IF NOT EXISTS session;

COMMENT ON SCHEMA session IS 'Session Management: charging sessions and meter readings';

CREATE TABLE session.charging_sessions (
    id UUID PRIMARY KEY,
    station_id UUID NOT NULL,
    connector_id INTEGER NOT NULL,
    customer_id UUID NOT NULL,
    vehicle_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'CHARGING', 'COMPLETED', 'FAILED')),
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ,
    unit_rate_amount NUMERIC(19,4) NOT NULL,
    unit_rate_currency VARCHAR(3) NOT NULL,
    total_energy_kwh NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    total_amount_amount NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    total_amount_currency VARCHAR(3) NOT NULL,
    error_code VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE session.meter_readings (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES session.charging_sessions(id) ON DELETE CASCADE,
    timestamp TIMESTAMPTZ NOT NULL,
    energy_delivered_kwh NUMERIC(19,4) NOT NULL,
    power_kw NUMERIC(19,4) NOT NULL
);

-- Indexes for performance (NFRs)
CREATE INDEX idx_sessions_customer_time ON session.charging_sessions(customer_id, start_time DESC);
CREATE INDEX idx_sessions_station_time ON session.charging_sessions(station_id, start_time DESC);
CREATE INDEX idx_readings_session_time ON session.meter_readings(session_id, timestamp ASC);

COMMENT ON TABLE session.charging_sessions IS 'EV charging sessions tracked by the platform';
COMMENT ON TABLE session.meter_readings IS 'Periodic meter readings collected during charging sessions';
