---
id: 006-billing-pricing-1
unit: 004-billing-pricing
intent: 001-ev-charging-mvp
type: ddd-construction-bolt
status: in-progress
stories:
  - 004-001-cost-calculation
  - 004-002-income-reporting
created: "2026-07-24T15:00:00Z"
started: "2026-07-26T03:55:00+12:00"
completed: null
current_stage: implement
stages_completed:
  - name: model
    completed: "2026-07-26T03:54:00+12:00"
    artifact: ddd-01-domain-model.md
  - name: design
    completed: "2026-07-26T03:55:00+12:00"
    artifact: ddd-02-technical-design.md

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
- [ ] **1. Model**: Pending — Tariff, Invoice, BillingAccount
- [ ] **2. Design**: Pending — Cost calculation, event consumption
- [ ] **3. Implement**: Pending — Source code
- [ ] **4. Test**: Pending — Test report

## Dependencies

### Requires
- 005-session-management-1 (Session events)
- 004-station-management-1 (Markup, unit price)
- 003-identity-service-3 (Auth)

### Enables
- 007-payment-processing-1 (Invoice events)
