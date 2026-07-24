# Story: Docker Build and CI Pipeline Setup

## User Story
As a **DevOps Engineer**
I want to **set up Docker multi-stage builds and GitHub Actions CI pipeline**
So that **the project can be built, tested, scanned, and containerized automatically**

## Acceptance Criteria
- [ ] Given multi-stage Dockerfile, When built, Then distroless production image is created (~100MB)
- [ ] Given docker-compose, When started, Then backend + PostgreSQL + Redis run locally
- [ ] Given GitHub Actions workflow, When PR is created, Then it runs build, test, scan (Trivy), and build image
- [ ] Given Spring Boot, When Docker container starts, Then health endpoint returns UP

## Technical Notes
- Multi-stage: Maven build → distroless Java 21 runtime
- Docker Compose with: app, postgres (with PostGIS), redis
- CI: `.github/workflows/ci.yaml`

## Dependencies
- Story 000-001 (Backend scaffolding — needs pom.xml)
