-- V101__create_station_schema.sql
-- Creates the station schema and PostGIS extension

SET search_path TO public;

CREATE EXTENSION IF NOT EXISTS postgis;

CREATE SCHEMA IF NOT EXISTS station;

COMMENT ON SCHEMA station IS 'Station Management: charging stations, connectors, and location data';
