---
unit: 003-session-management
intent: 001-ev-charging-mvp
phase: inception
status: stories-defined
created: "2026-07-24T15:00:00Z"
updated: "2026-07-25T15:00:00Z"
---

# Unit Brief: Session Management

## Purpose
Manage charging session lifecycle, meter readings, customer session history and monthly totals, and vendor session reports. Sessions belong to the month they start, record marked-up unit rate at start time, total energy (kWh), total amount charged, and error codes.

## Scope

### In Scope
- Charging session creation and lifecycle (PENDING → CHARGING → COMPLETED/FAILED)
- Session-to-chargepoint and session-to-vehicle association
- Marked-up unit rate capture at session start
- Energy (kWh) and amount tracking
- Error code recording (empty if successful)
- Customer session history with monthly grouping and totals
- Vendor session report generation by chargepoint + date
- Domain event publishing (SessionStartedEvent, SessionCompletedEvent, SessionFailedEvent)
- Session belongs to the month it starts

### Out of Scope
- Protocol-level OCPP state (Device Gateway handles)
- Advance session state machine edge cases (deferred to post-MVP)
- Payment processing (handled by Payment Processing)
- Reconciliation jobs (deferred)

---

## Assigned Requirements

| FR | Requirement | Priority |
|----|-------------|----------|
| FR-7 | Charging Session Lifecycle | Must |
| FR-8 | Session History & Monthly Totals | Must |
| FR-9 | Vendor Session Reports | Should |

---

## Domain Concepts

### Key Entities
| Entity | Description | Attributes |
|--------|-------------|------------|
| ChargingSession | EV charging session | id, station_id, connector_id, vehicle_id, customer_id, start_time, end_time, unit_rate_tenth_cents, total_energy_kwh, total_amount, error_code, status, created_at |
| MeterReading | Periodic energy reading | id, session_id, timestamp, energy_delivered_kwh, power_kw, meter_value |

### Key Operations
| Operation | Description | Inputs | Outputs |
|-----------|-------------|--------|---------|
| Start Session | Begin charging session | station_id, connector_id, vehicle_id, customer_id | ChargingSession |
| Record Meter | Record meter value | session_id, energy_kwh, timestamp | MeterReading |
| End Session | Complete charging session | session_id, total_energy, error_code | ChargingSession (COMPLETED/FAILED) |
| Get History | Customer session history | customer_id, month/year optional | ChargingSession[] + totals |
| Get Report | Vendor session report | station_id, date | ChargingSession[] |

---

## Dependencies

### Depends On
| Unit | Reason |
|------|--------|
| `001-identity-service` | Customer and vendor identity |
| `002-station-management` | Station/connector reference |

### Depended By
| Unit | Reason |
|------|--------|
| `004-billing-pricing` | Consumes session completed events for cost calculation |
| `005-payment-processing` | Consumes session events for payment triggering |
| `010-device-gateway` | Publishes domain events consumed by session |

---

## Technical Context

### Suggested Technology
| Component | Technology |
|-----------|------------|
| ORM | Spring Data JPA + Hibernate |
| Schema | `session` schema in PostgreSQL |
| API | REST controllers under `/api/v1/sessions/` |
| Events | SessionStartedEvent, MeterValueReceivedEvent, SessionCompletedEvent, SessionFailedEvent |

### Integration Points
| Integration | Type | Protocol |
|-------------|------|----------|
| Station lookup | Internal API | Java interface (station port) |
| Session events | Domain events | ApplicationEventPublisher |

---

## Success Criteria

### Functional
- [ ] Session can be started with station, connector, vehicle, customer
- [ ] Meter readings recorded during session
- [ ] Session ends with total energy and amount, error code if failed
- [ ] Customer sees session history grouped by month with totals
- [ ] Vendor generates session report by chargepoint + date

### Non-Functional
- [ ] Session events published reliably
- [ ] Monthly aggregation query < 200ms

### Quality
- [ ] Test coverage > 80%

---

## Bolt Suggestions

| Bolt | Type | Stories | Objective |
|------|------|---------|-----------|
| bolt-003-session-1 | DDD | S1, S2, S3 | Session lifecycle, domain model, events |
| bolt-003-session-2 | DDD | S4, S5 | History, reports, monthly totals |
