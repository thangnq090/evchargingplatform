---
id: 001-identity-service-1
unit: 001-identity-service
intent: 001-ev-charging-mvp
type: ddd-construction-bolt
status: planned
stories:
  - 001-001-admin-registration
  - 001-002-vendor-user-registration
created: "2026-07-24T15:00:00Z"
started: null
completed: null
current_stage: null
stages_completed: []

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
Implement core user registration: Admin (email+password), Vendor users (VENDOR_ADMIN/VENDOR_USER roles), and Customers (with auto-generated account numbers).

## Stories Included
- **001-001-admin-registration**: Admin registration and login (Must)
- **001-002-vendor-user-registration**: Vendor user registration and invitation (Must)

## Bolt Type
**Type**: DDD Construction Bolt
**Definition**: `.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md`

## Stages
- [ ] **1. Model**: Pending → Domain model (User, Role, Permission, Vendor)
- [ ] **2. Design**: Pending → Technical design (ports, services, API)
- [ ] **3. Implement**: Pending → Source code
- [ ] **4. Test**: Pending → Test report

## Dependencies

### Requires
- None (foundational bolt)

### Enables
- 002-identity-service-2 (Auth and RBAC)
- 004-station-management-1 (Vendor identity)

## Success Criteria
- [ ] Admin registration with email+password
- [ ] Vendor creation with invited user (VENDOR_ADMIN)
- [ ] Customer registration with account number
- [ ] All stories acceptance criteria met
- [ ] Tests passing
