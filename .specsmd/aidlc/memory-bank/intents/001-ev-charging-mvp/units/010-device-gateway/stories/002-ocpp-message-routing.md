# Story: OCPP Message Routing and Event Translation

## User Story
As a **Charging Station**
I want to **send StartTransaction, StopTransaction, and MeterValues messages**
So that **charging sessions are managed and energy usage is recorded**

## Acceptance Criteria
- [ ] Given a StartTransaction.req from a charger, When received, Then a ChargingStartedEvent is published to Session module
- [ ] Given a StopTransaction.req, When received, Then a ChargingStoppedEvent is published with total energy
- [ ] Given a MeterValues.req, When received, Then a MeterValueReceivedEvent is published with reading data
- [ ] Given a StatusNotification.req, When received, Then a ConnectorStatusChangedEvent is published
- [ ] Given an OCPP 2.0.1 message (in future), When the protocol adapter is upgraded, Then domain events remain unchanged

## Technical Notes
- Protocol abstraction layer maps OCPP → Domain events
- OCPP Call messages → Domain events published via ApplicationEventPublisher
- Domain modules never handle raw OCPP frames
- Migration path: new adapter for OCPP 2.0.1 without changing domain

## Dependencies
- Story 010-001 (OCPP connection)
