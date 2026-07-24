---
unit: 010-device-gateway
intent: 001-ev-charging-mvp
phase: inception
status: draft
created: "2026-07-24T15:00:00Z"
updated: "2026-07-24T15:00:00Z"
---

# Unit Brief: Device Gateway

## Purpose
Dedicated protocol boundary module for OCPP 1.6J charger communication over WebSocket. Translates OCPP frames to domain events consumed by Session Management and Station Management. Protocol abstraction layer enables OCPP 2.0.1 migration without impacting domain modules.

## Scope

### In Scope
- OCPP 1.6J WebSocket server (WSS)
- Device authentication via OCPP Authorize
- Protocol abstraction layer (OCPP → Domain Events)
- Charger heartbeat, status notification handling
- MeterValues translation → MeterValueReceivedEvent
- StartTransaction/StopTransaction → ChargingStartedEvent, ChargingStoppedEvent
- Session affinity (sticky WebSocket for stateful OCPP communication)
- Protocol boundary isolation (domain modules never see OCPP frames)

### Out of Scope
- OCPP 2.0.1 support (abstraction layer ready, but 2.0.1 deferred)
- Firmware management (metadata only in Station Mgmt; update flow deferred)
- Charger-initiated firmware updates (deferred)

---

## Assigned Requirements

| FR | Requirement | Priority |
|----|-------------|----------|
| FR-17 | OCPP 1.6J WebSocket Communication | Must |

---

## Domain Concepts

### Key Entities
| Entity | Description | Attributes |
|--------|-------------|------------|
| DeviceConnection | Active WebSocket connection | id, station_id, charger_serial, connected_at, last_heartbeat_at, protocol_version |
| OcppMessage | Raw OCPP message frame | id, connection_id, message_type (Call/Result/Error), action, payload, received_at |

### Key Operations
| Operation | Description | Inputs | Outputs |
|-----------|-------------|--------|---------|
| Accept Connection | WebSocket handshake + device auth | charger_id, credentials | Connection established |
| Handle Message | Route OCPP message to handler | OcppMessage | Domain event or OCPP response |
| Translate StartTransaction | OCPP → Domain event | StartTransaction.req | ChargingStartedEvent |
| Translate StopTransaction | OCPP → Domain event | StopTransaction.req | ChargingStoppedEvent |
| Translate MeterValues | OCPP → Domain event | MeterValues.req | MeterValueReceivedEvent |
| Handle Heartbeat | Process charger heartbeat | Heartbeat.req | HeartbeatResponse + HealthUpdatedEvent |

### Event Mapping
```
OCPP Call → Translation → Domain Event
─────────────────────────────────────
BootNotification.req       → ChargerRegisteredEvent
Heartbeat.req              → HeartbeatReceivedEvent
StatusNotification.req     → ConnectorStatusChangedEvent
Authorize.req              → ChargerAuthorizedEvent
StartTransaction.req       → ChargingStartedEvent
StopTransaction.req        → ChargingStoppedEvent
MeterValues.req            → MeterValueReceivedEvent
DiagnosticsStatusNotif.req → DiagnosticsStatusEvent (deferred)
FirmwareStatusNotif.req    → FirmwareStatusEvent (deferred)
```

---

## Dependencies

### Depends On
| Unit | Reason |
|------|--------|
| `002-station-management` | Station identity, connector status |
| `003-session-management` | Session creation/consumption |

### Depended By
| Unit | Reason |
|------|--------|
| None | Device Gateway is an inbound boundary |

---

## Technical Context

### Suggested Technology
| Component | Technology |
|-----------|------------|
| WebSocket Server | Spring WebSocket (Spring MVC stack for MVP; WebFlux/Netty path for scale) |
| OCPP Parsing | Lightweight OCPP message parser (custom or ocpp-spring-boot-starter) |
| Message Routing | In-process event bus → Domain events |
| Schema | `device_gateway` schema in PostgreSQL |
| API | WebSocket endpoint: `wss://{host}/ocpp/{charger_id}` |

### Integration Points
| Integration | Type | Protocol |
|-------------|------|----------|
| Session events | Domain events | ApplicationEventPublisher |
| Station status | Domain events | StationStatusChangedEvent |
| Station lookup | Internal API | Java interface (station port) |

### OCPP Protocol Flow
```
Charger                    Device Gateway               Session Module
   │                            │                            │
   │── WebSocket Connect ──────▶│                            │
   │── BootNotification.req ──▶│                            │
   │◀─ BootNotification.conf ──│                            │
   │── Heartbeat.req ──────────▶│                            │
   │◀─ Heartbeat.conf ─────────│                            │
   │── StartTransaction.req ──▶│── ChargingStartedEvent ──▶│
   │◀─ StartTransaction.conf ──│                            │
   │── MeterValues.req ───────▶│── MeterValueReceivedEvent ▶│
   │── StopTransaction.req ───▶│── ChargingStoppedEvent ──▶│
   │◀─ StopTransaction.conf ──│                            │
```

---

## Success Criteria

### Functional
- [ ] Charger connects via WSS with OCPP 1.6J
- [ ] StartTransaction → ChargingStartedEvent published
- [ ] StopTransaction → ChargingStoppedEvent published
- [ ] MeterValues → MeterValueReceivedEvent published
- [ ] Heartbeat tracked, timeout detected
- [ ] Protocol abstraction layer isolates domain from OCPP version

### Non-Functional
- [ ] 10K+ concurrent WebSocket connections
- [ ] Sub-second message translation latency
- [ ] Session affinity maintained (same charger → same gateway instance)

---

## Bolt Suggestions

| Bolt | Type | Stories | Objective |
|------|------|---------|-----------|
| bolt-010-gateway-1 | DDD | S1, S2, S3 | WebSocket server, OCPP message handling, protocol translation |
| bolt-010-gateway-2 | DDD | S4, S5 | Device auth, heartbeat monitoring, session affinity |
