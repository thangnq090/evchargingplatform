---
stage: design
bolt: 005-session-management-1
created: 2026-07-25T14:44:10Z
---

## Technical Design: session-management

### Architecture Pattern

Hexagonal Architecture (Ports and Adapters) combined with Domain-Driven Design (DDD) principles. This isolates the core domain logic from infrastructure details (like HTTP REST controllers and Spring Data JPA repositories), aligning with the project's Modular Monolith style (ADR-003).

### Layer Structure

```text
com.evcharging.session
├── domain/                      # Pure Java domain logic (no framework coupling)
│   ├── model/                  # Aggregates, Entities, Value Objects, Enums
│   ├── event/                  # Domain Event definitions
│   ├── repository/             # Repository Ports (interfaces)
│   └── service/                # Domain Services
├── application/                 # Use Cases / Application Services
│   ├── service/                # Application Services orchestrating use cases
│   ├── dto/                    # Request/Response objects
│   └── port/                   # Input/Output Ports (for cross-module dependency)
├── infrastructure/              # Framework adapters and concrete implementations
│   ├── adapter/                # Integration adapters (other modules, third party)
│   ├── persistence/            # JPA Entities, Spring Data repositories
│   └── config/                 # Bean declarations, Spring configuration
└── api/                         # Presentation Layer
    ├── controller/             # Spring MVC REST Controllers
    └── dto/                    # API Request/Response DTOs
```

### API Design

All endpoints reside under `/api/v1/sessions/`.

- **Start Session**
  - **Endpoint**: `POST /api/v1/sessions`
  - **Request**:
    ```json
    {
      "stationId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "connectorId": 1,
      "vehicleId": "5cb85f64-5717-4562-b3fc-2c963f66afa6",
      "customerId": "8da85f64-5717-4562-b3fc-2c963f66afa6"
    }
    ```
  - **Response**: `201 Created`
    ```json
    {
      "id": "1fa85f64-5717-4562-b3fc-2c963f66afa6",
      "stationId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "connectorId": 1,
      "customerId": "8da85f64-5717-4562-b3fc-2c963f66afa6",
      "vehicleId": "5cb85f64-5717-4562-b3fc-2c963f66afa6",
      "status": "CHARGING",
      "startTime": "2026-07-25T14:44:10Z",
      "unitRate": {
        "amount": 0.3500,
        "currency": "EUR"
      },
      "totalEnergyKwh": 0.0,
      "totalAmount": {
        "amount": 0.0000,
        "currency": "EUR"
      }
    }
    ```

- **Stop Session**
  - **Endpoint**: `POST /api/v1/sessions/{id}/stop`
  - **Request**: Empty body (or optional error code/reason)
  - **Response**: `200 OK`
    ```json
    {
      "id": "1fa85f64-5717-4562-b3fc-2c963f66afa6",
      "status": "COMPLETED",
      "endTime": "2026-07-25T15:14:10Z",
      "totalEnergyKwh": 15.65,
      "totalAmount": {
        "amount": 5.4775,
        "currency": "EUR"
      }
    }
    ```

- **Record Meter Reading**
  - **Endpoint**: `POST /api/v1/sessions/{id}/meter-readings`
  - **Request**:
    ```json
    {
      "timestamp": "2026-07-25T14:50:00Z",
      "energyDeliveredKwh": 5.25,
      "powerKw": 22.0
    }
    ```
  - **Response**: `202 Accepted`

- **Get Customer Session History**
  - **Endpoint**: `GET /api/v1/sessions/history`
  - **Query Parameters**: `customerId` (UUID), `yearMonth` (optional, format: `YYYY-MM`)
  - **Response**: `200 OK`
    ```json
    {
      "history": [
        {
          "month": "2026-07",
          "totals": {
            "totalSessions": 1,
            "totalEnergyKwh": 15.65,
            "totalAmount": {
              "amount": 5.48,
              "currency": "EUR"
            }
          },
          "sessions": [
            {
              "id": "1fa85f64-5717-4562-b3fc-2c963f66afa6",
              "stationId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
              "connectorId": 1,
              "startTime": "2026-07-25T14:44:10Z",
              "endTime": "2026-07-25T15:14:10Z",
              "status": "COMPLETED",
              "totalEnergyKwh": 15.65,
              "totalAmount": {
                "amount": 5.48,
                "currency": "EUR"
              }
            }
          ]
        }
      ]
    }
    ```

- **Get Vendor Session Report**
  - **Endpoint**: `GET /api/v1/sessions/report`
  - **Query Parameters**: `stationId` (UUID), `date` (format: `YYYY-MM-DD`)
  - **Response**: `200 OK` (list of sessions at the chargepoint on that day)

### Data Model

Flyway migration file `V102__create_session_schema.sql` under the `session` schema.

```sql
CREATE SCHEMA IF NOT EXISTS session;

CREATE TABLE session.charging_sessions (
    id UUID PRIMARY KEY,
    station_id UUID NOT NULL,
    connector_id INTEGER NOT NULL,
    customer_id UUID NOT NULL,
    vehicle_id UUID,
    status VARCHAR(20) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    unit_rate_amount NUMERIC(19,4) NOT NULL,
    unit_rate_currency VARCHAR(3) NOT NULL,
    total_energy_kwh NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    total_amount_amount NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    total_amount_currency VARCHAR(3) NOT NULL,
    error_code VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE session.meter_readings (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES session.charging_sessions(id) ON DELETE CASCADE,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    energy_delivered_kwh NUMERIC(19,4) NOT NULL,
    power_kw NUMERIC(19,4) NOT NULL
);

-- Indexes for performance (NFRs)
CREATE INDEX idx_sessions_customer_time ON session.charging_sessions(customer_id, start_time DESC);
CREATE INDEX idx_sessions_station_time ON session.charging_sessions(station_id, start_time DESC);
CREATE INDEX idx_readings_session_time ON session.meter_readings(session_id, timestamp ASC);
```

### Security Design

- **Authentication**: JWT-based authentication relaid via the Spring Cloud Gateway.
- **Authorization**:
  - `POST /api/v1/sessions` requires role `CUSTOMER` (for starting their own sessions) or `ADMIN`.
  - `POST /api/v1/sessions/{id}/stop` requires role `CUSTOMER` (must be the session owner), `VENDOR` (owning the station), or `ADMIN`.
  - `POST /api/v1/sessions/{id}/meter-readings` requires service/system authority (relayed from Device Gateway).
  - `GET /api/v1/sessions/history` requires role `CUSTOMER` (must match `customerId` in query parameter, or be `ADMIN`).
  - `GET /api/v1/sessions/report` requires role `VENDOR` (owning the station) or `ADMIN`.
- **Tenancy Validation**: Row-Level Security (RLS) or application-level filters scoped to the authenticated user's tenant ID or vendor ID.

### NFR Implementation

- **Performance**:
  - Queries for customer session history and monthly aggregations use the compound index `(customer_id, start_time DESC)` to ensure sub-200ms latency even for large datasets.
- **Concurrency**:
  - Optimistic locking (`version` column) on `charging_sessions` prevents concurrent overwrite issues when processing late/out-of-order meter readings.
