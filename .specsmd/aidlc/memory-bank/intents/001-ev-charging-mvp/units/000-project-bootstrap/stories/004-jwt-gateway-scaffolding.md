# Story: Spring Cloud Gateway and JWT Infrastructure

## User Story
As a **Developer**
I want to **set up Spring Cloud Gateway with JWT RS256 validation and JWKS endpoint**
So that **all API calls are authenticated from the start across all modules**

## Acceptance Criteria
- [ ] Given Gateway module, When configured, Then it routes to module REST controllers
- [ ] Given JWT configuration, When RS256 public key is provided, Then tokens are validated
- [ ] Given JWKS endpoint, When called, Then public key is returned for signature verification
- [ ] Given unauthenticated request, When received, Then 401 is returned
- [ ] Given RS256 key pair script, When run, Then private and public keys are generated

## Technical Notes
- Spring Cloud Gateway + Spring Security OAuth2 Resource Server
- JWKS endpoint for key distribution
- OpenSSL script for RS256 key pair generation

## Dependencies
- Story 000-001 (Backend scaffolding)
