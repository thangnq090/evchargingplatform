---
id: 006-billing-pricing-1
unit: 004-billing-pricing
intent: 001-ev-charging-mvp
type: ddd-construction-bolt
status: complete
stories:
  - 004-001-cost-calculation
  - 004-002-income-reporting
created: "2026-07-24T15:00:00Z"
started: "2026-07-26T03:55:00+12:00"
completed: "2026-07-27T11:47:30+12:00"
current_stage: test
stages_completed:
  - name: model
    completed: "2026-07-26T03:54:00+12:00"
    artifact: ddd-01-domain-model.md
  - name: design
    completed: "2026-07-26T03:55:00+12:00"
    artifact: ddd-02-technical-design.md
  - name: implement
    completed: "2026-07-26T04:00:00+12:00"
    artifact: src/
  - name: test
    completed: "2026-07-27T11:47:30+12:00"
    artifact: ddd-03-test-report.md

requires_bolts:
  - 005-session-management-1
  - 004-station-management-1
  - 003-identity-service-3
enables_bolts:
  - 007-payment-processing-1
requires_units: []
blocks: false

complexity:
  avg_complexity: 2
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
---

# Bolt: 006-billing-pricing-1

## Overview
Cost calculation with marked-up rates, invoice generation, and income reporting for admin and vendor dashboards.

## Stories Included
- **004-001-cost-calculation**: Cost calculation with marked-up rates (Must)
- **004-002-income-reporting**: Admin income reporting (Must)

## Bolt Type
**Type**: DDD Construction Bolt

## Stages
- [x] **1. Model**: Completed — Tariff, Invoice, BillingAccount
- [x] **2. Design**: Completed — Cost calculation, event consumption
- [x] **3. Implement**: Completed — Source code
- [x] **4. Test**: Completed — Test report


## Dependencies

### Requires
- 005-session-management-1 (Session events)
- 004-station-management-1 (Markup, unit price)
- 003-identity-service-3 (Auth)

### Enables
- 007-payment-processing-1 (Invoice events)
