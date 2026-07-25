# Story: JWT Authentication with HMAC-SHA256 Signing

## User Story
As a **Security Engineer**
I want **JWT access tokens signed with HMAC-SHA256 secret key and validated by services**
So that **all API calls are authenticated and tamper-proof**

## Acceptance Criteria
- [ ] Given an authentication request, When successful, Then JWT is signed with HMAC-SHA256 secret key
- [ ] Given a JWT token, When service/gateway validates it, Then HMAC signature verification succeeds
- [ ] Given a token with invalid signature, When validated, Then 401 is returned
- [ ] Given a token with invalid issuer, When validated, Then 401 is returned
- [ ] Given a token with invalid audience, When validated, Then 401 is returned
- [ ] Given an expired token, When validated, Then 401 is returned

## Technical Notes
- HMAC-SHA256 symmetric signing using configured secret key
- Shared secret across services (Bolt 1 standard)
- Claims: sub, iat, exp, iss, aud, roles, vendor_id

## Dependencies
- Story 001-001 (Admin registration)
