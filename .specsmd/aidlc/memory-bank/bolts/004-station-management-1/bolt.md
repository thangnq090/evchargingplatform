---
id: 004-station-management-1
unit: 002-station-management
intent: 001-ev-charging-mvp
type: ddd-construction-bolt
status: planned
stories:
  - 002-001-chargepoint-crud
  - 002-002-markup-configuration
created: "2026-07-24T15:00:00Z"
started: null
completed: null
current_stage: null
stages_completed: []

requires_bolts:
  - 001-identity-service-1
  - 003-identity-service-3
enables_bolts:
  - 005-session-management-1
requires_units: []
blocks: false

complexity:
  avg_complexity: 2
  avg_uncertainty: 1
  max_dependencies: 1
  testing_scope: 2
---

# Bolt: 004-station-management-1

## Overview
Chargepoint CRUD with geospatial location (PostGIS), vendor markup configuration, and availability management.

## Stories Included
- **002-001-chargepoint-crud**: Chargepoint CRUD with location (Must)
- **002-002-markup-configuration**: Admin markup configuration (Must)

## Bolt Type
**Type**: DDD Construction Bolt

## Stages
- [ ] **1. Model**: Pending — Vendor, Station, Connector entities + PostGIS
- [ ] **2. Design**: Pending — Ports, services, API design
- [ ] **3. Implement**: Pending — Source code
- [ ] **4. Test**: Pending — Test report

## Dependencies

### Requires
- 001-identity-service-1 (Vendor identity)
- 003-identity-service-3 (Auth infrastructure)

### Enables
- 005-session-management-1 (Station reference)
