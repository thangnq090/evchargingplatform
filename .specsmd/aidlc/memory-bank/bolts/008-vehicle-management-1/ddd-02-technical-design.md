---
stage: design
bolt: 008-vehicle-management-1
created: "2026-07-26T10:47:46Z"
---

## Technical Design: Vehicle Management

---

### Architecture Pattern

**Hexagonal Architecture (Ports & Adapters)** within the Spring Modulith modular monolith (ADR-003).

- The `vehicle` module is a self-contained bounded context
- Domain and application layers have **zero Spring dependencies** (pure Java)
- Infrastructure adapters implement ports defined by the domain
- Spring Modulith enforces module boundaries at build time

---

### Layer Structure

```text
┌────────────────────────────────────────────────────────┐
│  API Layer  (com.evcharging.vehicle.api)               │
│  VehicleController, AdminVehicleController             │
│  RegisterVehicleRequest, VehicleResponse, ...          │
├────────────────────────────────────────────────────────┤
│  Application Layer  (com.evcharging.vehicle.application)│
│  VehicleApplicationService                             │
│  RegisterVehicleCommand, DelistVehicleCommand          │
│  AssociateRfidCommand                                  │
├────────────────────────────────────────────────────────┤
│  Domain Layer  (com.evcharging.vehicle.domain)         │
│  Vehicle, OwnershipRecord                              │
│  RegistrationPlate, RfidNumber, VehicleStatus          │
│  VehicleRegistrationService, VehicleDelistingService   │
│  VehicleRepository (port), OwnershipRecordRepository   │
│  VehicleRegisteredEvent, VehicleDelistedEvent, ...     │
├────────────────────────────────────────────────────────┤
│  Infrastructure Layer  (com.evcharging.vehicle.infra)  │
│  VehicleJpaRepository (adapter)                        │
│  VehicleEntity, OwnershipRecordEntity (JPA)            │
│  VehicleMapper                                         │
│  db/migration/vehicle-module/V401__create_vehicle_schema│
└────────────────────────────────────────────────────────┘
```

---

### Package Structure

```text
com.evcharging.vehicle/
├── VehicleModule.java                           ← Spring Modulith descriptor
├── api/
│   ├── controller/
│   │   ├── VehicleController.java               ← Customer-facing REST
│   │   └── AdminVehicleController.java          ← Admin-facing REST
│   └── dto/
│       ├── RegisterVehicleRequest.java
│       ├── AssociateRfidRequest.java
│       ├── VehicleResponse.java
│       └── OwnershipRecordResponse.java
├── application/
│   ├── service/
│   │   └── VehicleApplicationService.java
│   └── events/
│       └── package-info.java                    ← Spring Modulith event listener
├── domain/
│   ├── model/
│   │   ├── Vehicle.java
│   │   ├── OwnershipRecord.java
│   │   ├── VehicleStatus.java
│   │   ├── RegistrationPlate.java
│   │   └── RfidNumber.java
│   ├── event/
│   │   ├── VehicleRegisteredEvent.java
│   │   ├── RfidAssociatedEvent.java
│   │   └── VehicleDelistedEvent.java
│   ├── repository/
│   │   ├── VehicleRepository.java               ← Port (interface)
│   │   └── OwnershipRecordRepository.java       ← Port (interface)
│   └── service/
│       ├── VehicleRegistrationService.java
│       └── VehicleDelistingService.java
└── infrastructure/
    ├── adapter/
    │   ├── VehicleJpaAdapter.java               ← Implements VehicleRepository
    │   ├── OwnershipRecordJpaAdapter.java
    │   └── VehicleMapper.java
    └── persistence/
        ├── VehicleEntity.java
        ├── OwnershipRecordEntity.java
        ├── VehicleJpaRepository.java            ← Spring Data JPA
        └── OwnershipRecordJpaRepository.java
```

---

### API Design

**Base path**: `/api/v1/vehicles`  
**Module tag**: `vehicle`

#### Customer-Facing Endpoints

| Method | Path | Description | Auth | Status Codes |
|--------|------|-------------|------|-------------|
| `POST` | `/api/v1/vehicles` | Register a vehicle | `CUSTOMER` | 201, 400, 409, 422 |
| `GET` | `/api/v1/vehicles` | List my vehicles (ACTIVE) | `CUSTOMER` | 200 |
| `GET` | `/api/v1/vehicles/{vehicleId}` | Get vehicle detail | `CUSTOMER` | 200, 403, 404 |
| `PATCH` | `/api/v1/vehicles/{vehicleId}/rfid` | Associate RFID | `CUSTOMER` | 200, 400, 403, 409 |
| `DELETE` | `/api/v1/vehicles/{vehicleId}` | De-list vehicle | `CUSTOMER` | 204, 403, 404, 422 |
| `GET` | `/api/v1/vehicles/lookup/plate` | Find by plate (partial) | `CUSTOMER` | 200 |
| `GET` | `/api/v1/vehicles/lookup/rfid/{rfid}` | Find by RFID | `CUSTOMER`, `INTERNAL` | 200, 404 |

#### Admin-Facing Endpoints

| Method | Path | Description | Auth | Status Codes |
|--------|------|-------------|------|-------------|
| `GET` | `/api/v1/admin/vehicles` | List all vehicles | `ADMIN` | 200 |
| `GET` | `/api/v1/admin/vehicles/{vehicleId}` | Get any vehicle | `ADMIN` | 200, 404 |
| `GET` | `/api/v1/admin/vehicles/{vehicleId}/ownership` | Full ownership history | `ADMIN` | 200 |

#### Request / Response Schemas

**POST /api/v1/vehicles**
```json
// Request
{
  "registrationPlate": "ABC-1234",
  "rfidNumber": "04A3B5C2D1E0"   // optional
}

// 201 Response
{
  "data": {
    "id": "veh_abc123",
    "registrationPlate": "ABC-1234",
    "rfidNumber": "04A3B5C2D1E0",
    "status": "ACTIVE",
    "ownerId": "cus_xyz789",
    "createdAt": "2026-07-26T10:47:46Z"
  },
  "meta": { "timestamp": "2026-07-26T10:47:46Z", "version": "v1" }
}
```

**PATCH /api/v1/vehicles/{vehicleId}/rfid**
```json
// Request
{
  "rfidNumber": "04A3B5C2D1E0"
}
// 200 Response: updated VehicleResponse
```

**GET /api/v1/vehicles/lookup/plate?q=ABC&limit=20&cursor=...**
```json
// Response (collection)
{
  "data": [
    { "id": "veh_abc123", "registrationPlate": "ABC-1234", "status": "ACTIVE", "ownerId": "..." }
  ],
  "meta": {
    "timestamp": "...",
    "pagination": { "cursor": "...", "limit": 20, "hasMore": false }
  }
}
```

#### Domain Error Codes

| Error Code | HTTP | Trigger |
|-----------|------|---------|
| `VEHICLE_PLATE_CONFLICT` | 409 | Plate already ACTIVE for this vendor |
| `VEHICLE_RFID_CONFLICT` | 409 | RFID already assigned globally |
| `VEHICLE_NOT_FOUND` | 404 | No vehicle with given ID |
| `VEHICLE_NOT_OWNED` | 403 | Customer does not own this vehicle |
| `VEHICLE_ALREADY_DELISTED` | 422 | Cannot delist a DE_LISTED vehicle |
| `VEHICLE_RFID_ALREADY_SET` | 409 | RFID already associated (use admin endpoint to change) |

---

### Data Model

**Schema**: `vehicle` (per ADR-004 — schema-per-module, no cross-schema FK joins)

#### Table: `vehicle.vehicles`

```sql
CREATE TABLE vehicle.vehicles (
    id                 UUID         PRIMARY KEY,
    registration_plate VARCHAR(20)  NOT NULL,
    rfid_number        VARCHAR(50)  UNIQUE,
    current_owner_id   UUID         NOT NULL,   -- ref to identity.customers (no FK — cross-module)
    vendor_id          UUID         NOT NULL,
    status             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                           CHECK (status IN ('ACTIVE', 'DE_LISTED')),
    created_at         TIMESTAMPTZ  NOT NULL,
    delisted_at        TIMESTAMPTZ,
    version            INTEGER      NOT NULL DEFAULT 0
);

-- Plate uniqueness enforced at application layer (not DB UNIQUE) to allow re-registration of DE_LISTED plates
-- Partial unique index for ACTIVE constraint:
CREATE UNIQUE INDEX uidx_vehicles_plate_vendor_active
    ON vehicle.vehicles (registration_plate, vendor_id)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_vehicles_owner_vendor
    ON vehicle.vehicles (current_owner_id, vendor_id, status);

CREATE INDEX idx_vehicles_rfid
    ON vehicle.vehicles (rfid_number)
    WHERE rfid_number IS NOT NULL;

CREATE INDEX idx_vehicles_plate_ilike
    ON vehicle.vehicles USING gin (registration_plate gin_trgm_ops);
```

#### Table: `vehicle.ownership_records`

```sql
CREATE TABLE vehicle.ownership_records (
    id           UUID        PRIMARY KEY,
    vehicle_id   UUID        NOT NULL REFERENCES vehicle.vehicles(id),
    customer_id  UUID        NOT NULL,
    start_date   TIMESTAMPTZ NOT NULL,
    end_date     TIMESTAMPTZ               -- NULL = active ownership
);

CREATE INDEX idx_ownership_vehicle_id ON vehicle.ownership_records (vehicle_id);
CREATE INDEX idx_ownership_customer   ON vehicle.ownership_records (customer_id, end_date NULLS FIRST);
```

#### Flyway Migration

- **File**: `V401__create_vehicle_schema.sql`
- **Path**: `src/main/resources/db/migration/vehicle-module/V401__create_vehicle_schema.sql`
- **Number basis**: identity=V1-V8, station=V101-V104, session=V201, billing=V301, vehicle=**V401**

---

### Security Design

| Concern | Approach |
|---------|----------|
| **Authentication** | JWT Bearer validated at Gateway (ADR-007); token claims propagated to module via `SecurityContext` |
| **Customer ownership** | Application service reads `customerId` from JWT `sub` claim; validates `vehicle.current_owner_id == customerId` before mutations |
| **Vendor scoping** | `vendorId` extracted from JWT claim; injected into all queries — no cross-vendor data leakage |
| **RFID lookup** | Exposed to `CUSTOMER` and internal `INTERNAL` scope (session module resolves RFID during session start) |
| **Admin bypass** | Admin endpoints annotated `@PreAuthorize("hasRole('ADMIN')")` — bypass vendor scope |
| **Soft-delete privacy** | DE_LISTED vehicles excluded from customer list queries (`WHERE status = 'ACTIVE'`) |
| **RLS readiness** | `vendor_id` column present on all tables; Row-Level Security policies can be applied per ADR-017 |

---

### Domain Events Design

Events published via `ApplicationEventPublisher` (Spring Modulith in-process, transactional — ADR-005).

| Event Class | Fields | Consumers |
|-------------|--------|-----------|
| `VehicleRegisteredEvent` | `vehicleId`, `customerId`, `vendorId`, `registrationPlate`, `rfidNumber`, `registeredAt` | `session` (RFID cache), `search` (index) |
| `RfidAssociatedEvent` | `vehicleId`, `rfidNumber`, `associatedAt` | `session` (RFID cache refresh) |
| `VehicleDelistedEvent` | `vehicleId`, `customerId`, `registrationPlate`, `delistedAt` | `search` (remove from index) |

Events are published **within the same transaction** that mutates vehicle state — guaranteed consistency without distributed sagas.

---

### NFR Implementation

| Requirement | Design Approach |
|-------------|----------------|
| **Plate lookup performance** | `pg_trgm` GIN index on `registration_plate` for `ILIKE` partial match; default cursor pagination (limit 20) |
| **RFID lookup latency** | Indexed `rfid_number` column; session module may cache RFID→vehicleId in Redis for hot-path resolution |
| **Optimistic locking** | `version` column on `vehicles` table; JPA `@Version` annotation — prevents concurrent modification |
| **Audit trail** | `ownership_records` table provides full ownership history; `created_at` + `delisted_at` timestamps on vehicle |
| **Observability** | MDC context: `vehicleId` added on vehicle-scoped operations; structured INFO log on register/delist/RFID associate |
| **Idempotency** | `Idempotency-Key` header supported on POST/PATCH; stored in-memory map (MVP) → Redis (production) |

---

### Integration Points

| Module | Direction | Mechanism | Data |
|--------|-----------|-----------|------|
| `identity` | Inbound reference | No direct call — `customerId` from JWT claim | Customer ownership validation |
| `session` | Outbound event | `VehicleRegisteredEvent`, `RfidAssociatedEvent`, `VehicleDelistedEvent` | Vehicle registered/RFID/delisted |
| `session` | Inbound query | `GET /api/v1/vehicles/lookup/rfid/{rfid}` (HTTP or internal port) | RFID → vehicle resolution |
| `session-search` | Outbound event | `VehicleRegisteredEvent`, `VehicleDelistedEvent` | Indexing vehicle for search |

---

### Verification Plan (Pre-Implementation)

- [ ] `vehicle` schema + tables created by Flyway migration V401
- [ ] Partial unique index enforces ACTIVE plate uniqueness per vendor
- [ ] `rfid_number` globally unique index present
- [ ] Module compiles with zero cross-module imports (Spring Modulith check)
- [ ] API base path `/api/v1/vehicles` routes correctly
- [ ] JWT `sub` → `customerId`, `vendorId` claim extraction confirmed
