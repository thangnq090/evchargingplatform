# Story: RBAC and Credential Management

## User Story
As an **Administrator**
I want to **manage user roles and reset credentials**
So that **users have appropriate access and can recover from lockouts**

## Acceptance Criteria
- [ ] Given an admin, When they reset a user's password, Then a temporary password is generated
- [ ] Given a user with temporary password, When they log in, Then they are prompted to change password
- [ ] Given role assignment, When user is assigned VENDOR_ADMIN, Then they can manage vendor resources and users
- [ ] Given role assignment, When user is assigned VENDOR_USER, Then they have limited access per permissions
- [ ] Given an API endpoint, When accessed without required role, Then 403 Forbidden is returned

## Technical Notes
- Roles: ADMIN, VENDOR_ADMIN, VENDOR_USER, CUSTOMER
- Permissions: fine-grained (station:write, session:read, billing:read)
- Password reset generates temporary password with forced change

## Dependencies
- Story 001-004 (JWT authentication)
