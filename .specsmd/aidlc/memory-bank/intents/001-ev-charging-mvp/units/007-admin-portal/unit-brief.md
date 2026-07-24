---
unit: 007-admin-portal
intent: 001-ev-charging-mvp
phase: inception
status: draft
created: "2026-07-24T15:00:00Z"
updated: "2026-07-24T15:00:00Z"
---

# Unit Brief: Admin & Vendor Portal

## Purpose
Application/UI layer that aggregates data from Station, Billing, Session, Identity, and Vehicle modules. Owns no business logic or persistent data — purely aggregation and display. Provides Admin Dashboard and Vendor Dashboard views.

## Scope

### In Scope
- Admin Dashboard: view vendors + chargepoints, system-wide income (date range + vendor filter), set vendor markup, reset user credentials
- Vendor Dashboard: list own chargepoints, add/update/remove, view income (current month + breakdowns), generate session reports by chargepoint + date
- Data aggregation from existing modules via REST API composition
- Filtering by vendor_id from JWT for vendor users
- No business logic or persistent data

### Out of Scope
- UI rendering framework (REST API first; frontend later)
- Real-time dashboard updates (SSE deferred)
- Any domain logic ownership

---

## Assigned Requirements

| FR | Requirement | Priority |
|----|-------------|----------|
| FR-19 | Admin Dashboard | Should |
| FR-20 | Vendor Dashboard | Should |

---

## Dependencies

### Depends On
| Unit | Reason |
|------|--------|
| `001-identity-service` | Admin/vendor identity, credential reset |
| `002-station-management` | Chargepoint data, markup config |
| `003-session-management` | Session data, reports |
| `004-billing-pricing` | Income and activity data |
| `005-payment-processing` | Payment status |

---

## Bolt Suggestions

| Bolt | Type | Stories | Objective |
|------|------|---------|-----------|
| bolt-007-portal-1 | Simple | S1, S2 | Admin Dashboard API endpoints |
| bolt-007-portal-2 | Simple | S3, S4 | Vendor Dashboard API endpoints |
