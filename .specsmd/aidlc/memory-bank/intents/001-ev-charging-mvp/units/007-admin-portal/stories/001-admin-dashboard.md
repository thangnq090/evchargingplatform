# Story: Admin Dashboard API Endpoints

## User Story
As an **Administrator**
I want to **view vendors, chargepoints, income, and manage settings through aggregated API endpoints**
So that **I can operate the platform from a single dashboard**

## Acceptance Criteria
- [ ] Given admin user, When they query dashboard, Then vendors list with chargepoints and income is returned
- [ ] Given admin user, When they filter income by date range + vendor, Then filtered results are returned
- [ ] Given admin user, When they set vendor markup, Then markup is updated
- [ ] Given admin user, When they reset user credentials, Then temporary password is generated

## Technical Notes
- Application/UI layer only — no business logic ownership
- Data aggregated from Station, Billing, Session, Identity modules
- Vendor scoping via JWT vendor_id claim

## Dependencies
- All other units (data aggregation)
