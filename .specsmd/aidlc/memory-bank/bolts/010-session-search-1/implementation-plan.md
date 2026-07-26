---
stage: plan
bolt: 010-session-search-1
unit: 008-session-search
intent: 001-ev-charging-mvp
created: 2026-07-26T16:01:03Z
---

## Implementation Plan: Session Full-Text Search (010-session-search-1)

### Objective
Implement PostgreSQL Full-Text Search (FTS) for charging sessions, enabling admins to search sessions via partial matches on registration plates, customer account numbers, session IDs, and error codes. Expose an admin-only REST endpoint with RBAC enforcement (`ROLE_ADMIN`).

### Deliverables
1. **Flyway DB Migration (`V202__add_session_search_vector.sql` in `session-module`)**:
   - Add a GIN index and/or query join logic with PostgreSQL full-text search capability.
   - Join `session.charging_sessions` with `identity.users` (for `account_number`) and `vehicle.vehicles` (for `registration_plate`) or build an effective tsvector / trigram query across tables.
2. **Session Search Repository & Query**:
   - Native SQL query in `ChargingSessionRepository` or custom Search Repository using `to_tsvector` / `to_tsquery` / `plainto_tsquery` / `ILIKE` partial matching across `session_id`, `registration_plate`, `account_number`, and `error_code`.
3. **Session Search DTOs & Service**:
   - `SessionSearchResponse` DTO containing session details, customer account number, vehicle registration plate, and error code.
   - `SessionSearchService` executing full-text search queries.
4. **Admin Search REST Endpoint**:
   - `GET /api/v1/admin/sessions/search?q={query}` in `SessionAdminController` (or `SessionSearchController`).
   - Protected with `@PreAuthorize("hasRole('ADMIN')")`.

### Dependencies
- `005-session-management-1`: Charging session entities and schema (`session.charging_sessions`).
- `008-vehicle-management-1`: Vehicle entities and schema (`vehicle.vehicles`).
- `001-identity-service-1`: Customer user entity and schema (`identity.users`).

### Technical Approach
1. **Database Search Query**:
   - Write SQL joining `session.charging_sessions s` LEFT JOIN `vehicle.vehicles v ON s.vehicle_id = v.id` LEFT JOIN `identity.users u ON s.customer_id = u.id`.
   - Match search terms against `s.id::text`, `v.registration_plate`, `u.account_number`, and `s.error_code` using PostgreSQL `to_tsvector('simple', ...)` or partial matching with `plainto_tsquery('simple', :query)` and `ILIKE '%' || :query || '%'`.
2. **REST API & RBAC**:
   - Endpoint path: `GET /api/v1/admin/sessions/search?q={term}`.
   - Returns paginated or list of `SessionSearchResponse` objects.
   - Non-admin access returns HTTP 403 Forbidden.

### Acceptance Criteria
- [ ] Given admin search term "AUD", partial matching returns sessions linked to vehicles with plates like "AUD186" and "AUD994".
- [ ] Given admin search term matching customer account number, matching sessions are returned.
- [ ] Given admin search term matching session ID or error code, matching sessions are returned.
- [ ] Given a non-admin user request, HTTP 403 Forbidden is returned.
- [ ] Search response includes session details, customer account info, and vehicle registration info.
