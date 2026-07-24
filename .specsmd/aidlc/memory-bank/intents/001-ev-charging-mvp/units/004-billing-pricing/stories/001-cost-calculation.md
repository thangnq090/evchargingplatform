# Story: Cost Calculation with Marked-up Rates

## User Story
As a **Customer**
I want to **be charged the correct amount based on energy used and the marked-up unit rate**
So that **I pay a fair price for charging**

## Acceptance Criteria
- [ ] Given a completed session, When cost is calculated, Then amount = energy_kwh × (vendor_unit_price + admin_markup)
- [ ] Given session at start time, When cost is calculated, Then the unit rate active at session start is used
- [ ] Given multiple sessions, When invoiced, Then each session shows line items with unit price and markup breakdown

## Dependencies
- Story 003-001 (Session lifecycle)
- Story 002-002 (Markup configuration)
