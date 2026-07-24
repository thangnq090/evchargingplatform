---
intent: 001-ev-charging-mvp
phase: inception
status: defined
updated: "2026-07-24T15:00:00Z"
---

# Unit Decomposition: EV Charging Platform MVP

## Overview

11 units identified from 20 functional requirements + 1 project bootstrap unit, organized by module ownership. Units follow modular monolith architecture with clear dependency direction and domain event communication.

## Requirement-to-Unit Mapping

| FR | Title | Unit | Priority |
|----|-------|------|----------|
| — | Project Scaffolding (no FR) | `000-project-bootstrap` | Must |
| FR-1 | Admin User Registration | `001-identity-service` | Must |
| FR-2 | Vendor User Management | `001-identity-service` | Must |
| FR-3 | Customer Registration | `001-identity-service` | Must |
| FR-4 | Authentication & Authorization | `001-identity-service` | Must |
| FR-5 | Chargepoint Management | `002-station-management` | Must |
| FR-6 | Admin Markup Configuration | `002-station-management` | Must |
| FR-7 | Charging Session Lifecycle | `003-session-management` | Must |
| FR-8 | Session History & Monthly Totals | `003-session-management` | Must |
| FR-9 | Vendor Session Reports | `003-session-management` | Should |
| FR-10 | Vehicle Registration | `006-vehicle-management` | Must |
| FR-11 | Vehicle De-listing | `006-vehicle-management` | Must |
| FR-12 | Income Reporting (Admin) | `004-billing-pricing` | Must |
| FR-13 | Vendor Income & Activity Insights | `004-billing-pricing` | Should |
| FR-14 | Payment Orchestration | `005-payment-processing` | Must |
| FR-15 | Payment/Session Decoupling | `005-payment-processing` | Must |
| FR-16 | Full-Text Search | `008-session-search` | Must |
| FR-17 | OCPP 1.6J WebSocket Communication | `010-device-gateway` | Must |
| FR-18 | Console Log Notifications | `009-notification` | Could |
| FR-19 | Admin Dashboard | `007-admin-portal` | Should |
| FR-20 | Vendor Dashboard | `007-admin-portal` | Should |

## Units Summary

| # | Unit | Module | Dependencies | Bolt Type | Priority |
|---|------|--------|--------------|-----------|----------|
| 0 | `000-project-bootstrap` | — (Setup) | None | Simple | Must |
| 1 | `001-identity-service` | Identity & Access | `000-project-bootstrap` | DDD | Must |
| 2 | `002-station-management` | Station Management | `000-project-bootstrap`, `001-identity-service` | DDD | Must |
| 3 | `003-session-management` | Session Management | `002-station-management`, `001-identity-service` | DDD | Must |
| 4 | `004-billing-pricing` | Pricing & Billing | `001-identity-service`, `002-station-management` | DDD | Must |
| 5 | `005-payment-processing` | Payment Processing | `004-billing-pricing`, `003-session-management` | DDD | Must |
| 6 | `006-vehicle-management` | Vehicle Management | `001-identity-service` | DDD | Must |
| 7 | `007-admin-portal` | Application/UI | All backend units | Simple | Should |
| 8 | `008-session-search` | Session Management | `003-session-management`, `006-vehicle-management` | Simple | Must |
| 9 | `009-notification` | Notification | `003-session-management`, `005-payment-processing` | Simple | Could |
| 10 | `010-device-gateway` | Device Gateway | `002-station-management`, `003-session-management` | DDD | Must |

## Dependency Graph

```
                         ┌──────────────────────────────┐
                         │  001-identity-service        │
                         │  (no dependencies)           │
                         └──────────┬───────────────────┘
                                    │
                    ┌───────────────┼───────────────────┐
                    ▼               ▼                   ▼
        ┌──────────────────┐ ┌────────────┐ ┌──────────────────┐
        │ 002-station-mgmt │ │ 006-vehicle│ │ 008-session-search│
        │ depends: 001     │ │ depends:001│ │ depends: 003, 006│
        └────────┬─────────┘ └────────────┘ └──────────────────┘
                 │
                 ▼
        ┌──────────────────┐
        │ 003-session-mgmt │
        │ depends: 001, 002│
        └────────┬─────────┘
                 │
          ┌──────┴──────┐
          ▼              ▼
  ┌──────────────┐ ┌──────────────┐
  │ 004-billing  │ │ 010-device   │
  │ depends: 001 │ │ gateway      │
  │ 002, 003     │ │ depends: 002 │
  └──────┬───────┘ │ 003          │
         │         └──────────────┘
         ▼
  ┌──────────────────┐
  │ 005-payment      │
  │ depends: 004, 003│
  └──────────────────┘

  ┌──────────────────┐ ┌──────────────────┐
  │ 007-admin-portal │ │ 009-notification │
  │ depends: all     │ │ depends: 003, 005│
  └──────────────────┘ └──────────────────┘
```

## Unit Briefs Created

- ✅ `units/000-project-bootstrap/unit-brief.md`
- ✅ `units/001-identity-service/unit-brief.md`
- ✅ `units/002-station-management/unit-brief.md`
- ✅ `units/003-session-management/unit-brief.md`
- ✅ `units/004-billing-pricing/unit-brief.md`
- ✅ `units/005-payment-processing/unit-brief.md`
- ✅ `units/006-vehicle-management/unit-brief.md`
- ✅ `units/007-admin-portal/unit-brief.md`
- ✅ `units/008-session-search/unit-brief.md`
- ✅ `units/009-notification/unit-brief.md`
- ✅ `units/010-device-gateway/unit-brief.md`

## Construction Order

Based on dependency graph, recommended execution order:

0. **000-project-bootstrap** — Scaffolding: project structure, build, CI, Docker, Gateway (no dependencies)
1. **001-identity-service** — Foundation: users, auth, RBAC (needs bootstrap)
2. **006-vehicle-management** — Parallel: vehicle entities (needs bootstrap + 001)
3. **002-station-management** — Parallel: chargepoints (needs bootstrap + 001)
4. **003-session-management** — Core: sessions, metering (needs 001, 002)
5. **004-billing-pricing** — Pricing: tariffs, markup, invoicing (needs 001, 003)
6. **010-device-gateway** — OCPP protocol handling (needs 002, 003)
7. **005-payment-processing** — Payment: orchestrator, MockPayment (needs 003, 004)
8. **008-session-search** — Search: PostgreSQL FTS indexing (needs 003, 006)
9. **009-notification** — Console notifications (needs 003, 005)
10. **007-admin-portal** — Portal UI layer (needs all)
