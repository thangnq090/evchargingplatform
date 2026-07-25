---
unit: 002-station-management
intent: 001-ev-charging-mvp
phase: construction
status: complete
implemented: true
created: "2026-07-24T15:00:00Z"
updated: "2026-07-26T10:35:00Z"
---

# Story: Admin Markup Configuration

## User Story
As an **Administrator**
I want to **set and manage the markup percentage added to vendor unit prices**
So that **the platform earns revenue on charging sessions**

## Acceptance Criteria
- [ ] Given an admin, When they set vendor markup percentage, Then it's applied to that vendor's unit prices
- [ ] Given a markup update, When a new charging session starts, Then the new markup is used
- [ ] Given existing sessions, When markup changes, Then historical sessions retain original markup

## Technical Notes
- Markup stored on Vendor entity
- Applied at session start time (captured in session record)
- Markup history tracked for audit

## Dependencies
- Story 002-001 (Chargepoint CRUD)
