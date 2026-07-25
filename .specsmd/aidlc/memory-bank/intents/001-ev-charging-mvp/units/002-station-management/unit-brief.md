---
unit: 002-station-management
intent: 001-ev-charging-mvp
phase: inception
status: complete
created: '2026-07-24T15:00:00Z'
updated: '2026-07-25T15:00:00Z'
---

# Unit Brief: Station Management

## Purpose
Manage chargepoint lifecycle (vendor-owned stations with geospatial location data), availability tracking, and admin markup configuration. Chargepoints have unique identifiers, group labels, unit prices, and availability status.

## Scope

### In Scope
- Chargepoint CRUD (add, update, remove — soft-delete)
- Unique identifier for each chargepoint
- Group label for vendor chargepoint grouping
- Unit price in tenths of cents (integer)
- Availability status: AVAILABLE, UNAVAILABLE, MAINTENANCE
- Geospatial location (latitude + longitude) using PostGIS `GEOGRAPHY(Point, 4326)`
- Vendor ownership scoping (all chargepoints assigned to a vendor)
- Admin markup configuration per vendor
- Chargepoint query endpoints (by ID, by vendor, by availability)

### Out of Scope
- Real-time charger state (handled by Device Gateway)
- Firmware management (deferred from MVP)
- Charging session lifecycle (handled by Session Management)
- OCPP communication (handled by Device Gateway)

---

## Assigned Requirements

| FR | Requirement | Priority |
|----|-------------|----------|
| FR-5 | Chargepoint Management (CRUD, location, pricing, availability) | Must |
| FR-6 | Admin Markup Configuration | Must |

---

## Domain Concepts

### Key Entities
| Entity | Description | Attributes |
|--------|-------------|------------|
| Station (Chargepoint) | Physical EV charger | id (unique identifier), vendor_id, name, group_label, unit_price_tenth_cents, status, location (GEOGRAPHY Point), created_at, updated_at, deleted_at |
| Vendor | Charging station vendor | id, name, account_number, markup_percentage, created_at |
| Connector | Charging connector on a station | id, station_id, type (CCS/CHAdeMO/Type2), max_power_kw, status |

### Key Operations
| Operation | Description | Inputs | Outputs |
|-----------|-------------|--------|---------|
| Create Chargepoint | Add new chargepoint | name, group_label, unit_price, vendor_id, location | Station |
| Update Chargepoint | Modify chargepoint fields | station_id, fields to update | Station |
| Remove Chargepoint | Soft-delete chargepoint | station_id | Success (historical data preserved) |
| Set Availability | Toggle chargepoint status | station_id, status (AVAILABLE/UNAVAILABLE/MAINTENANCE) | Station |
| Set Vendor Markup | Admin sets vendor markup | vendor_id, markup_percentage | Vendor |
| Find Stations Near | Proximity query | lat, lng, radius | Station[] |

---

## Story Summary

| Metric | Count |
|--------|-------|
| Total Stories | ~4 |
| Must Have | 3 |
| Should Have | 1 |
| Could Have | 0 |

---

## Dependencies

### Depends On
| Unit | Reason |
|------|--------|
| `001-identity-service` | Vendor and admin auth context, vendor identity |

### Depended By
| Unit | Reason |
|------|--------|
| `003-session-management` | Sessions reference chargepoints |
| `010-device-gateway` | Device auth and charger identity |
| `004-billing-pricing` | Pricing per vendor |
| `007-admin-portal` | Aggregates station data |

### External Dependencies
| System | Purpose | Risk |
|--------|---------|------|
| PostgreSQL (station schema) | Station and vendor data storage + PostGIS | Low — PostGIS well-established |

---

## Technical Context

### Suggested Technology
| Component | Technology |
|-----------|------------|
| ORM | Spring Data JPA + Hibernate + Hibernate Spatial |
| GIS Extension | PostGIS (`GEOGRAPHY(Point, 4326)`) |
| Migration | Flyway (`db/migration/station/`) |
| Schema | `station` schema in PostgreSQL |
| API | REST controllers under `/api/v1/stations/`, `/api/v1/vendors/`, `/api/v1/admin/markup/` |

### Integration Points
| Integration | Type | Protocol |
|-------------|------|----------|
| Vendor identity | Internal API | Java interface (port) |
| Station status events | Domain events | `StationStatusChangedEvent` |
| Proximity search | Internal API | PostgreSQL spatial query |

### Data Storage
| Data | Type | Volume | Retention |
|------|------|--------|-----------|
| Stations | SQL (station schema) | 10K+ rows | Indefinite (soft-delete) |
| Vendors | SQL | Hundreds | Indefinite |
| Location data | PostGIS GEOGRAPHY | Spatial index | Indefinite |

---

## Constraints

- Unit price stored as integer representing tenths of cents (avoid floating point)
- Location stored as `GEOGRAPHY(Point, 4326)` WGS 84 — enables PostGIS distance/spatial queries
- Spatial index on location for future proximity search
- Chargepoint unique ID is user-assigned (displayed on physical unit for support calls)
- Soft-delete preserves historical session data
- All chargepoints scoped to owning vendor (vendor_id enforced in queries via RLS)

---

## Success Criteria

### Functional
- [ ] Vendor can create chargepoint with location coordinates
- [ ] Vendor can update chargepoint name, group, price, availability
- [ ] Admin can set vendor-specific markup percentage
- [ ] Chargepoints filtered by vendor identity (VENDOR sees own; ADMIN sees all)
- [ ] Soft-delete preserves session history

### Non-Functional
- [ ] Spatial index query performs < 100ms for proximity search
- [ ] Unit price calculations precise (integer-based, no floating point)

### Quality
- [ ] Test coverage > 80%
- [ ] PostGIS spatial queries tested with Testcontainers

---

## Bolt Suggestions

| Bolt | Type | Stories | Objective |
|------|------|---------|-----------|
| bolt-002-station-1 | DDD | S1, S2 | Chargepoint CRUD with location, vendor scoping |
| bolt-002-station-2 | DDD | S3, S4 | Markup configuration, availability, integrations |
