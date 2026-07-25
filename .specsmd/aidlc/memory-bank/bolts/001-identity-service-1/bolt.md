---
id: 001-identity-service-1
unit: 001-identity-service
intent: 001-ev-charging-mvp
type: ddd-construction-bolt
status: completed
stories:
  - 001-001-admin-registration
  - 001-002-vendor-user-registration
created: "2026-07-24T15:00:00Z"
started: "2026-07-24T20:17:17Z"
completed: "2026-07-25T01:36:00Z"
current_stage: complete
stages_completed:
  - name: model
    completed: "2026-07-24T20:23:32Z"
    artifact: ddd-01-domain-model.md
  - name: design
    completed: "2026-07-24T20:32:49Z"
    artifact: ddd-02-technical-design.md
  - name: adr-analysis
    completed: "2026-07-24T20:39:06Z"
    artifact: "No new ADRs - ADR-007, ADR-017 cover existing decisions"
  - name: implement
    completed: "2026-07-24T20:50:00Z"
    artifact: "backend/identity-module"
  - name: test
    completed: "2026-07-25T01:30:00Z"
    artifact: "scripts/smoke-test-identity-bolt1.sh"

requires_bolts: []
enables_bolts:
  - 002-identity-service-2
  - 003-identity-service-3
  - 004-station-management-1
requires_units: []
blocks: false

complexity:
  avg_complexity: 2
  avg_uncertainty: 1
  max_dependencies: 0
  testing_scope: 2
---

# Bolt: 001-identity-service-1

## Overview
First bolt for Identity & Access — foundational user registration and authentication. Covers Admin registration, Vendor user creation with invitation, and Customer registration.

## Objective
Implement core user registration: Admin (email+password), Vendor users (VENDOR_ADMIN/VENDOR_USER roles), and login token issuing via HMAC-SHA256.

## Stories Included
- **001-001-admin-registration**: Admin registration and login (Must)
- **001-002-vendor-user-registration**: Vendor user registration and invitation (Must)

## Bolt Type
**Type**: DDD Construction Bolt
**Definition**: `.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md`

## Stages
- [x] **1. Model**: Complete → Domain model (User, Role, Permission, Vendor)
- [x] **2. Design**: Complete → Technical design (ports, services, API)
- [x] **3. ADR Analysis**: Complete → No new ADRs (ADR-007, ADR-017 cover all decisions)
- [x] **4. Implement**: Complete → Source code generated in identity-module
- [x] **5. Test**: Complete → Unit tests & cURL integration script (scripts/smoke-test-identity-bolt1.sh)

## Dependencies

### Requires
- None (foundational bolt)

### Enables
- 002-identity-service-2 (Auth and RBAC)
- 004-station-management-1 (Vendor identity)

## Success Criteria
- [x] Admin registration with email+password (protected with ROLE_ADMIN)
- [x] Vendor creation with invited user (VENDOR_ADMIN)
- [x] Login endpoint issuing HS256 access tokens
- [x] All stories acceptance criteria met
- [x] All unit & smoke integration tests passing
