---
unit: 004-billing-pricing
intent: 001-ev-charging-mvp
phase: inception
status: stories-defined
created: "2026-07-24T15:00:00Z"
updated: "2026-07-25T15:00:00Z"
---

# Unit Brief: Pricing & Billing

## Purpose
Manage tariff calculation, cost calculation using marked-up unit rates, invoice generation, and income reporting for admin and vendor dashboards.

## Scope

### In Scope
- Tariff calculation (energy × marked-up unit rate + fees)
- Markup application (admin markup + vendor unit price)
- Invoice generation per session
- Admin income reporting (by date range, optionally by vendor)
- Vendor income insights (current month, breakdowns by chargepoint over days/weeks/months)
- Domain events for cost calculation and invoice generation

### Out of Scope
- Payment capture (handled by Payment Processing)
- Tax/VAT calculation (simplified for MVP)
- Dynamic pricing or time-based tariffs (deferred)

---

## Assigned Requirements

| FR | Requirement | Priority |
|----|-------------|----------|
| FR-12 | Income Reporting (Admin) | Must |
| FR-13 | Vendor Income & Activity Insights | Should |

---

## Domain Concepts

### Key Entities
| Entity | Description | Attributes |
|--------|-------------|------------|
| Tariff | Pricing rule | id, name, energy_rate_tenth_cents, session_fee, idle_fee, valid_from, valid_to |
| Invoice | Session invoice | id, session_id, customer_id, vendor_id, line_items[], total_amount, currency, created_at |
| BillingAccount | Customer billing account | id, customer_id, balance, total_spent, last_billed_at |
| IncomeReport | Aggregated income view | vendor_id, period, total_revenue, session_count, avg_per_session |

### Key Operations
| Operation | Description | Inputs | Outputs |
|-----------|-------------|--------|---------|
| CalculateCost | Compute session cost | session_id, energy_kwh, unit_rate | Cost (line items, total) |
| GenerateInvoice | Create session invoice | session_id | Invoice |
| GetAdminIncome | Income by date range, vendor filter | start_date, end_date, vendor_id(optional) | IncomeReport[] |
| GetVendorInsights | Vendor activity breakdown | vendor_id, period (day/week/month) | ActivityReport |
| SetMarkup | Admin vendor markup | vendor_id, markup_percentage | Vendor |

---

## Dependencies

### Depends On
| Unit | Reason |
|------|--------|
| `001-identity-service` | Admin/vendor/customer identity |
| `002-station-management` | Vendor markup, chargepoint pricing |
| `003-session-management` | Session completion events |

### Depended By
| Unit | Reason |
|------|--------|
| `005-payment-processing` | Consumes invoicing for payment capture |
| `007-admin-portal` | Aggregates income data |

---

## Success Criteria

### Functional
- [ ] Session cost calculated from energy × marked-up unit rate
- [ ] Invoice generated per session
- [ ] Admin income report by date range + vendor filter
- [ ] Vendor sees current month income + breakdowns

### Quality
- [ ] Test coverage > 80%
- [ ] Money handled via JSR 354 Moneta (no raw BigDecimal)

---

## Bolt Suggestions

| Bolt | Type | Stories | Objective |
|------|------|---------|-----------|
| bolt-004-billing-1 | DDD | S1, S2 | Tariff/cost model, invoice generation |
| bolt-004-billing-2 | DDD | S3, S4 | Income reports, vendor insights |
