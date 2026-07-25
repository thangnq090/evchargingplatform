---
stage: model
bolt: 005-session-management-1
created: 2026-07-25T14:42:00Z
---

## Static Model: session-management

### Entities

- **ChargingSession**: Represents an EV charging session from initiation to completion or failure.
  - Properties:
    - `id` (`SessionId`): Strongly-typed identifier.
    - `stationId` (`StationId`): The charging station where the session takes place.
    - `connectorId` (`Integer`): The connector number on the station.
    - `customerId` (`UserId`): The customer charging their vehicle.
    - `vehicleId` (`VehicleId`): Optional reference to the vehicle being charged.
    - `status` (`SessionStatus`): `PENDING` (requested), `CHARGING` (active), `COMPLETED` (ended successfully), or `FAILED` (ended with error).
    - `startTime` (`Instant`): Timestamp when charging starts.
    - `endTime` (`Instant`): Timestamp when the session ends (nullable).
    - `unitRate` (`Money`): Marked-up rate per kWh captured at session start.
    - `totalEnergyKwh` (`BigDecimal`): Cumulative energy delivered during the session.
    - `totalAmount` (`Money`): Total cost of the session (energy * rate).
    - `errorCode` (`String`): Error code if status is `FAILED` (nullable).
    - `createdAt` (`Instant`): Record creation timestamp.
  - Business Rules:
    - Status transitions must follow: `PENDING` → `CHARGING` → `COMPLETED` / `FAILED`.
    - Marked-up rate must be captured at session start and remain fixed for the duration.
    - Total energy can only increase.
    - Total amount must be calculated as `totalEnergyKwh * unitRate`.
    - Session belongs to the calendar month of its `startTime` for history grouping.

- **MeterReading**: A periodic measurement of energy and power sent by the charger.
  - Properties:
    - `id` (`MeterReadingId`): Strongly-typed identifier.
    - `sessionId` (`SessionId`): The session this reading belongs to.
    - `timestamp` (`Instant`): Time of measurement.
    - `energyDeliveredKwh` (`BigDecimal`): Total cumulative energy delivered in kWh.
    - `powerKw` (`BigDecimal`): Current power rate in kW.
  - Business Rules:
    - Must belong to an active (`CHARGING`) session.
    - The timestamp must be between the session's start time and end time.

### Value Objects

- **SessionId**: Type-safe UUID for a charging session.
- **MeterReadingId**: Type-safe UUID for a meter reading.
- **SessionStatus**: Enum with states: `PENDING`, `CHARGING`, `COMPLETED`, `FAILED`.

### Aggregates

- **ChargingSessionAggregateRoot**:
  - Root: `ChargingSession`
  - Members: `MeterReading` (linked but stored in a separate table for performance scaling, managed under the aggregate's lifecycle).
  - Invariants:
    - Meter reading timestamps must be sequential.
    - Energy readings must be non-decreasing.

### Domain Events

- **SessionStartedEvent**: Published when a session transitions to `CHARGING`.
  - Payload: `sessionId`, `stationId`, `connectorId`, `customerId`, `unitRate`, `startTime`.
- **MeterReadingRecordedEvent**: Published when a new meter reading is recorded.
  - Payload: `sessionId`, `timestamp`, `energyDeliveredKwh`, `powerKw`.
- **SessionCompletedEvent**: Published when a session successfully completes.
  - Payload: `sessionId`, `endTime`, `totalEnergyKwh`, `totalAmount`.
- **SessionFailedEvent**: Published when a session fails.
  - Payload: `sessionId`, `endTime`, `errorCode`.

### Domain Services

- **SessionPricingService**: Computes the charging cost based on the captured unit rate and energy delivered.
- **SessionLifecycleService**: Validates and transitions session states, ensuring station availability rules are satisfied before starting a session.

### Repository Interfaces

- **ChargingSessionRepository**:
  - `save(ChargingSession session)`: Persist or update a session.
  - `findById(SessionId id)`: Find a session by ID.
  - `findByCustomerIdAndStartTimeBetween(UserId customerId, Instant start, Instant end)`: Retrieve customer sessions within a date range (for monthly history/totals).
  - `findByStationIdAndStartTimeBetween(StationId stationId, Instant start, Instant end)`: Retrieve sessions for a vendor report.

- **MeterReadingRepository**:
  - `save(MeterReading reading)`: Persist a reading.
  - `findBySessionId(SessionId sessionId)`: Retrieve readings for a session.

### Ubiquitous Language

- **Charging Session**: A single continuous charging transaction for a customer at a station connector.
- **Meter Reading**: Telemetry data sent periodically containing energy and power measurements.
- **Marked-up Rate**: The final customer cost per kWh, including any admin markup.
- **Active Session**: A session with status `CHARGING`.
