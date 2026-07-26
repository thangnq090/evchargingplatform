---
id: 001-fulltext-search
title: PostgreSQL Full-Text Search for Sessions
status: complete
implemented: true
---

# Story: PostgreSQL Full-Text Search for Sessions

## User Story
As an **Administrator**
I want to **search charging sessions using partial matches on plates, account numbers, and error codes**
So that **I can find sessions quickly without knowing exact values**

## Acceptance Criteria
- [ ] Given admin search, When searching by registration plate partial match, Then matching sessions are returned
- [ ] Given admin search, When searching by customer account number, Then matching sessions are returned
- [ ] Given admin search, When searching by error code, Then matching sessions are returned
- [ ] Given search term "AUD", When executed, Then plates "AUD186" and "AUD994" are matched
- [ ] Given a non-admin user, When they try to search, Then 403 Forbidden is returned
- [ ] Given search results, When displayed, Then session details, customer info, and vehicle info are included

## Technical Notes
- PostgreSQL Full-Text Search using tsvector/tsquery
- Search fields: registration_plate, account_number, error_code, session_id
- GIN index on search_vector for performance
- plainto_tsquery for partial matching with 'simple' dictionary
- Admin-only RBAC enforcement

## Dependencies
- Story 003-001 (Session lifecycle)
- Story 006-001 (Vehicle registration)
