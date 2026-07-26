-- V502__add_session_search_indexes.sql
-- Enables pg_trgm and adds trigram / full-text search indexes for session search performance

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_sessions_error_code_trgm
    ON session.charging_sessions USING gin (error_code gin_trgm_ops)
    WHERE error_code IS NOT NULL;
