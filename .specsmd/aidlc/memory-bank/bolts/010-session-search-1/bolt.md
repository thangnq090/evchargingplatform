---
id: 010-session-search-1
unit: 008-session-search
intent: 001-ev-charging-mvp
type: simple-construction-bolt
status: planned
stories:
  - 008-001-fulltext-search
created: "2026-07-24T15:00:00Z"
started: null
completed: null
current_stage: null
stages_completed: []

requires_bolts:
  - 005-session-management-1
  - 008-vehicle-management-1
enables_bolts: []
requires_units: []
blocks: false

complexity:
  avg_complexity: 2
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
---

# Bolt: 010-session-search-1

## Overview
PostgreSQL Full-Text Search for session admin search. GIN index on tsvector, search endpoint with partial matching, admin-only RBAC.

## Stories Included
- **008-001-fulltext-search**: PostgreSQL FTS for sessions (Must)

## Bolt Type
**Type**: Simple Construction Bolt

## Stages
- [ ] **1. Plan**: Pending
- [ ] **2. Implement**: Pending
- [ ] **3. Test**: Pending

## Dependencies

### Requires
- 005-session-management-1 (Session data to index)
- 008-vehicle-management-1 (Vehicle data to index)
