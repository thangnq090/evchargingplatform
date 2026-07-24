# Story: Customer Registration

## User Story
As a **Customer**
I want to **register with my name, email, and phone number**
So that **I can start charging sessions and view my history**

## Acceptance Criteria
- [ ] Given a new customer, When they register with name, email, phone, Then account is created with auto-generated account number
- [ ] Given a registered customer, When they log in, Then JWT with CUSTOMER role is returned
- [ ] Given customer registration, When email is duplicate, Then 409 Conflict is returned

## Technical Notes
- Account number format: `ACC-{UUID-prefix}` or sequential format
- Customer role scopes: session:start, session:read, vehicle:manage, billing:read

## Dependencies
- Story 001-001 (Admin registration - auth infrastructure)
