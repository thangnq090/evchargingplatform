# Story: JWT Authentication with RS256 Signing

## User Story
As a **Security Engineer**
I want to **JWT access tokens signed with RS256 and validated by the Gateway**
So that **all API calls are authenticated and tamper-proof**

## Acceptance Criteria
- [ ] Given an authentication request, When successful, Then JWT is signed with RS256 private key
- [ ] Given a JWT token, When Gateway validates it, Then public key verification succeeds
- [ ] Given a token with invalid signature, When Gateway validates, Then 401 is returned
- [ ] Given a token with invalid issuer, When Gateway validates, Then 401 is returned
- [ ] Given a token with invalid audience, When Gateway validates, Then 401 is returned
- [ ] Given an expired token, When Gateway validates, Then 401 is returned

## Technical Notes
- RS256 (RSA SHA-256) asymmetric signing
- Auth Service holds private key; Gateway + Resource Servers use public key
- JWKS endpoint for key distribution
- Claims: sub, iat, exp, iss, aud, roles, vendor_id

## Dependencies
- Story 001-001 (Admin registration)
