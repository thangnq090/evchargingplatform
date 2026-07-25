---
unit: 008-session-search
intent: 001-ev-charging-mvp
phase: inception
status: stories-defined
created: "2026-07-24T15:00:00Z"
updated: "2026-07-25T15:00:00Z"
---

# Unit Brief: Session Full-Text Search

## Purpose
Provide full-text search for charging sessions using PostgreSQL Full-Text Search (to_tsvector / to_tsquery). Supports partial matches across registration plates, customer account numbers, and error codes. Accessible only to ADMIN role. Migration path to OpenSearch/Elasticsearch when search volume or complexity justifies it.

## Scope

### In Scope
- Full-text search endpoint for admin
- Searchable fields: registration plate, customer account number, error code, session ID
- Partial matches ("AUD" matches "AUD186" and "AUD994")
- PostgreSQL Full-Text Search (tsvector/tsquery)
- Results include: session details, customer info, vehicle info
- Admin-only access (RBAC enforcement)

### Out of Scope
- End-user search (admin only for MVP)
- External search engine (OpenSearch deferred)
- Advanced search features (faceted, fuzzy beyond PG FTS)
- Real-time search index updates

---

## Assigned Requirements

| FR | Requirement | Priority |
|----|-------------|----------|
| FR-16 | Full-Text Search (admin, partial match, session/customer/vehicle info) | Must |

---

## Dependencies

### Depends On
| Unit | Reason |
|------|--------|
| `003-session-management` | Session data to index |
| `006-vehicle-management` | Vehicle data (plate, RFID) for search |

### Depended By
| Unit | Reason |
|------|--------|
| `007-admin-portal` | Search UI |

---

## Technical Context

### Search Index Strategy (PostgreSQL FTS)
```sql
-- Create tsvector index combining searchable fields
ALTER TABLE sessions ADD COLUMN search_vector tsvector
  GENERATED ALWAYS AS (
    setweight(to_tsvector('english', coalesce(session_id,'')), 'A') ||
    setweight(to_tsvector('simple', coalesce(registration_plate,'')), 'B') ||
    setweight(to_tsvector('simple', coalesce(account_number,'')), 'B') ||
    setweight(to_tsvector('simple', coalesce(error_code,'')), 'C')
  ) STORED;

CREATE INDEX idx_sessions_search ON sessions USING GIN(search_vector);

-- Query
SELECT * FROM sessions
WHERE search_vector @@ plainto_tsquery('simple', :search_term)
ORDER BY ts_rank(search_vector, plainto_tsquery('simple', :search_term)) DESC;
```

### Integration Points
| Integration | Type | Protocol |
|-------------|------|----------|
| Session data | READ-ONLY | SQL query (same DB, `session` schema) |
| Vehicle data | READ-ONLY | SQL query (same DB, `vehicle` schema) |

### Migration Path to OpenSearch
- Add OpenSearch client dependency
- Create index mapping matching search fields
- Add event listener to sync session/vehicle events to OpenSearch
- Add feature flag to switch between PG FTS and OpenSearch
- No domain changes required (search is read-only)

---

## Bolt Suggestions

| Bolt | Type | Stories | Objective |
|------|------|---------|-----------|
| bolt-008-search-1 | Simple | S1 | PostgreSQL FTS index, search endpoint, RBAC |
