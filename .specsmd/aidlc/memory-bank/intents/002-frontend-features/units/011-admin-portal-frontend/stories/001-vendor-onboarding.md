# Story: Admin Vendor Onboarding

## Story ID
`001-vendor-onboarding`

## Unit
`011-admin-portal-frontend`

## Requirement Mapping
- **FR-FE-1**: Platform Admin can view all registered vendors, onboard/invite new vendors, and create initial Vendor Admin user accounts.

## Acceptance Criteria
1. Admin can view a paginated list of all onboarded vendors with status, contact email, and chargepoint counts.
2. Admin can open an "Onboard Vendor" modal to enter Vendor Name, Business Registration/Tax ID, Contact Email, and Initial Admin Email.
3. Submitting the onboarding form sends a request to the backend identity/vendor service and displays a success toast with invitation status.
4. Input validation ensures valid email formats and required business fields.
