---
id: 012-device-gateway-1
unit: 010-device-gateway
intent: 001-ev-charging-mvp
type: ddd-construction-bolt
status: planned
stories:
  - 010-001-ocpp-connection
  - 010-002-ocpp-message-routing
created: "2026-07-24T15:00:00Z"
started: null
completed: null
current_stage: null
stages_completed: []

requires_bolts:
  - 004-station-management-1
  - 005-session-management-1
enables_bolts: []
requires_units: []
blocks: false

complexity:
  avg_complexity: 3
  avg_uncertainty: 2
  max_dependencies: 2
  testing_scope: 2
---

# Bolt: 012-device-gateway-1

## Overview
OCPP 1.6J WebSocket server, device authentication (X.509 + OCPP Authorize), and message routing from OCPP frames to domain events.

## Stories Included
- **010-001-ocpp-connection**: OCPP WebSocket connection and auth (Must)
- **010-002-ocpp-message-routing**: OCPP message routing and event translation (Must)

## Bolt Type
**Type**: DDD Construction Bolt

## Stages
- [ ] **1. Model**: Pending — DeviceConnection, OcppMessage, protocol abstraction
- [ ] **2. Design**: Pending — WebSocket handling, event mapping
- [ ] **3. Implement**: Pending — Source code
- [ ] **4. Test**: Pending — Test report

## Dependencies

### Requires
- 004-station-management-1 (Station identity)
- 005-session-management-1 (Session lifecycle events)
