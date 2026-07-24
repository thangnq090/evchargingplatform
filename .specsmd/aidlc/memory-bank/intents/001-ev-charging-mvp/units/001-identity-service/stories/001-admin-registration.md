# Story: Admin Registration and Login

## User Story
As an **Administrator**
I want to **register with email and password and log in**
So that **I can access the platform to manage vendors and settings**

## Acceptance Criteria
- [ ] Given a new admin, When they register with name, email, password, Then an admin account is created
- [ ] Given a registered admin, When they log in with correct credentials, Then JWT access + refresh tokens are returned
- [ ] Given a registered admin, When they log in with wrong password, Then 401 Unauthorized is returned
- [ ] Given an admin with expired token, When they make an API call, Then 401 is returned
- [ ] Given a valid refresh token, When token is refreshed, Then a new access token + new refresh token are returned

## Technical Notes
- JWT signed with RS256 using key pair
- Access token TTL: 15 minutes
- Refresh token TTL: 7 days with rotation

## Dependencies
- None (foundational story)
