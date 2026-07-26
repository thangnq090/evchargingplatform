---
unit: 004-billing-pricing
bolt: 006-billing-pricing-1
stage: test
status: complete
updated: 2026-07-27T11:47:30+12:00
---

# Test Report - Pricing & Billing

## Automated Tests Executed

| Test Suite / Scope | Command | Result | Tests Run |
|-------------------|---------|--------|-----------|
| **Unit & Service Tests** | `mvn test -pl billing-module` | **PASSED** | 9 tests passed, 0 failures |
| **Architecture Tests** | `mvn test -pl evcharging-app` | **PASSED** | 12 tests passed (including ArchUnit checks) |
| **Integration Smoke Test** | `bash scripts/smoke-test-billing-bolt6.sh` | **VERIFIED** | Script verified end-to-end invoice generation, line item breakdowns, marked-up tariffs, and admin income reporting |

## Test Summary Details

1. **Domain & Application Unit Tests (`com.evcharging.billing.*`)**:
   - `CostCalculatorTest`: Verified line item calculation formulas (base fee, admin markup fee, edge cases).
   - `BillingApplicationServiceTest`: Verified invoice generation upon `SessionCompletedEvent` reception and admin income report queries across date ranges and vendor filters.

2. **Architecture Compliance (`com.evcharging.archunit.ArchitectureTest`)**:
   - Package dependency rules and DDD hexagonal layer isolation rules pass cleanly.

3. **End-to-End Smoke Test (`smoke-test-billing-bolt6.sh`)**:
   - Verified flow: Login Superadmin -> Create Vendor & Admin -> Set 15% Markup -> Create Station (base 0.20 EUR/kWh) -> Customer Session -> Meter Reading (5 kWh) -> Stop Session -> Automatic Invoice Creation -> Verified invoice line items & amounts -> Admin Income Report verification.
