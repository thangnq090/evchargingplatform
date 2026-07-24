---
id: 000-project-bootstrap-1
unit: 000-project-bootstrap
intent: 001-ev-charging-mvp
type: simple-construction-bolt
status: planned
stories:
  - 000-001-backend-scaffolding
  - 000-002-frontend-scaffolding
  - 000-003-ci-docker-scaffolding
  - 000-004-jwt-gateway-scaffolding
created: "2026-07-24T15:00:00Z"
started: null
completed: null
current_stage: null
stages_completed: []

requires_bolts: []
enables_bolts:
  - 001-identity-service-1
  - 002-identity-service-2
  - 003-identity-service-3
  - 004-station-management-1
requires_units: []
blocks: false

complexity:
  avg_complexity: 2
  avg_uncertainty: 1
  max_dependencies: 0
  testing_scope: 2
---

# Bolt: 000-project-bootstrap-1

## Overview
Project scaffolding and setup — Maven multi-module backend, React+Vite frontend, Docker build, CI pipeline, Spring Cloud Gateway with JWT validation. This bolt must execute first as all other units depend on the project structure.

## Stories Included
- **000-001-backend-scaffolding**: Backend project structure + module boundaries (Must)
- **000-002-frontend-scaffolding**: Frontend project structure (Must)
- **000-003-ci-docker-scaffolding**: Docker build + CI pipeline (Must)
- **000-004-jwt-gateway-scaffolding**: Gateway + JWT infrastructure (Must)

## Bolt Type
**Type**: Simple Construction Bolt
**Stages**: Plan → Implement → Test

## Stages
- [ ] **1. Plan**: Pending
- [ ] **2. Implement**: Pending
- [ ] **3. Test**: Pending

## Dependencies

### Requires
- None (foundational bolt)

### Enables
- 001-identity-service-1 (requires project structure)
- All subsequent bolts
