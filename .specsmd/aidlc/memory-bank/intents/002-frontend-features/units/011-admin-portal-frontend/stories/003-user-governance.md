# Story: Account Governance & Credential Resets

## Story ID
`003-user-governance`

## Unit
`011-admin-portal-frontend`

## Requirement Mapping
- **FR-FE-3**: Platform Admin can view all platform users across roles, lock/unlock accounts, and initiate credential resets.

## Acceptance Criteria
1. Admin can view a searchable, filterable table of all system users (ADMIN, VENDOR_ADMIN, VENDOR_USER, CUSTOMER).
2. Admin can toggle account status (Active vs Suspended/Locked) with confirmation guard.
3. Admin can click "Trigger Reset Password" for any user, which generates a secure temporary reset token / sends reset notification.
4. Role badges clearly distinguish user permissions and tenant scope.
