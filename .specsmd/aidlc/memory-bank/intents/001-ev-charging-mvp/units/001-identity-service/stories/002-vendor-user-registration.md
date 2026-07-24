# Story: Vendor User Registration and Invitation

## User Story
As an **Administrator**
I want to **create a vendor account and invite the original vendor user**
So that **vendors can manage their chargepoints and activity**

## Acceptance Criteria
- [ ] Given an admin, When they create a vendor with user details, Then vendor account is created and invitation is issued
- [ ] Given a created vendor, When the invited user accepts, Then they can log in as VENDOR_ADMIN
- [ ] Given a VENDOR_ADMIN, When they add vendor users, Then users get VENDOR_USER role
- [ ] Given any vendor user, When they log in, Then JWT contains vendor_id claim

## Technical Notes
- Vendor user roles: VENDOR_ADMIN (full vendor access), VENDOR_USER (limited per permissions)
- Invitation via temporary token
- Vendor_id included in JWT claims for data scoping

## Dependencies
- Story 001-001 (Admin registration)
