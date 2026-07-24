---
unit: 006-vehicle-management
intent: 001-ev-charging-mvp
phase: inception
status: draft
created: "2026-07-24T15:00:00Z"
updated: "2026-07-24T15:00:00Z"
---

# Unit Brief: Vehicle Management

## Purpose
Manage vehicle lifecycle as a first-class business entity: registration, RFID assignment, ownership, de-listing, and re-registration. Vehicles are independent of both customers and charging sessions — a separate module avoids coupling customer management with charging logic.

## Scope

### In Scope
- Vehicle registration with registration plate + optional RFID
- Vehicle assignment to customer owner
- RFID association (auto-identify, or associate during manual session selection)
- Vehicle de-listing (soft-delete, preserve historical sessions)
- Re-registration of de-listed plate by different customer
- Vehicle lookup by plate or RFID
- Domain events for vehicle lifecycle

### Out of Scope
- Vehicle maintenance history beyond ownership
- Vehicle type/model management (just plate + RFID for MVP)
- Direct vehicle-to-charger authentication (handled via session)

---

## Assigned Requirements

| FR | Requirement | Priority |
|----|-------------|----------|
| FR-10 | Vehicle Registration (plate, RFID, ownership) | Must |
| FR-11 | Vehicle De-listing (soft-delete, re-registration) | Must |

---

## Domain Concepts

### Key Entities
| Entity | Description | Attributes |
|--------|-------------|------------|
| Vehicle | Registered vehicle | id, registration_plate, rfid_number, current_owner_id, status (ACTIVE/DE_LISTED), created_at, deleted_at |
| OwnershipRecord | Ownership history | id, vehicle_id, customer_id, start_date, end_date |

### Key Operations
| Operation | Description | Inputs | Outputs |
|-----------|-------------|--------|---------|
| Register Vehicle | Register vehicle with plate + RFID | customer_id, plate, rfid(optional) | Vehicle |
| Associate RFID | Link RFID to vehicle during manual selection | vehicle_id, rfid_number | Vehicle |
| De-list Vehicle | Soft-delete vehicle | vehicle_id, reason | Vehicle (DE_LISTED) |
| Transfer Ownership | Transfer vehicle ownership | vehicle_id, new_owner_id | OwnershipRecord |
| Find by Plate | Search by registration plate | plate (partial match) | Vehicle[] |
| Find by RFID | Lookup by RFID | rfid_number | Vehicle |

---

## Dependencies

### Depends On
| Unit | Reason |
|------|--------|
| `001-identity-service` | Customer identity for vehicle ownership |

### Depended By
| Unit | Reason |
|------|--------|
| `003-session-management` | Session associates vehicle |
| `008-session-search` | Vehicle search indexed |

---

## Technical Context

### Suggested Technology
| Component | Technology |
|-----------|------------|
| Schema | `vehicle` schema in PostgreSQL |
| API | REST controllers under `/api/v1/vehicles/` |

---

## Success Criteria

- [ ] Customer registers vehicle with plate + RFID
- [ ] RFID can be associated during manual session
- [ ] Vehicle de-listed without affecting historical sessions
- [ ] De-listed plate can be re-registered by different customer
- [ ] Vehicle lookup by plate supports partial match
