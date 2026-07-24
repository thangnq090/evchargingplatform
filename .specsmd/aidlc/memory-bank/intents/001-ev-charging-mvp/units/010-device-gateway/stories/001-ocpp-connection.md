# Story: OCPP 1.6J WebSocket Connection and Device Authentication

## User Story
As a **Charging Station**
I want to **connect to the platform via OCPP 1.6J WebSocket and authenticate**
So that **I can receive charging commands and report status**

## Acceptance Criteria
- [ ] Given a charger, When it connects via WSS, Then WebSocket handshake succeeds
- [ ] Given a connection, When BootNotification is sent, Then the charger is registered and configuration is returned
- [ ] Given an unauthenticated charger, When it sends commands, Then they are rejected
- [ ] Given a charger, When disconnected, Then the connection is cleaned up

## Technical Notes
- OCPP 1.6J over WSS (WebSocket Secure)
- Device auth via OCPP Authorize + X.509 mTLS
- Session affinity: charger always routes to same gateway instance (sticky WS)

## Dependencies
- Story 002-001 (Chargepoint CRUD — station identity)
