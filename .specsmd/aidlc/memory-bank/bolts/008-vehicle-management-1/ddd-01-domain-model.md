---
stage: model
bolt: 008-vehicle-management-1
created: "2026-07-26T10:43:23Z"
---

## Static Model: Vehicle Management

### Entities

- **Vehicle**: Represents a registered vehicle on the EV charging platform.
  - `id` (UUID) — surrogate PK
  - `registration_plate` (String, max 20) — unique identifier per vendor scope when ACTIVE; re-registrable after DE_LISTED
  - `rfid_number` (String, max 50, nullable) — optional RFID tag; unique when non-null
  - `current_owner_id` (UUID, FK → identity.customers.id) — current owning customer
  - `vendor_id` (UUID) — vendor scope (RLS enforcement per ADR-017)
  - `status` (VehicleStatus enum: ACTIVE | DE_LISTED)
  - `created_at` (OffsetDateTime)
  - `delisted_at` (OffsetDateTime, nullable) — set when DE_LISTED

- **OwnershipRecord**: Tracks vehicle ownership history across de-list/re-register cycles.
  - `id` (UUID) — surrogate PK
  - `vehicle_id` (UUID, FK → vehicle.vehicles.id)
  - `customer_id` (UUID, FK → identity.customers.id)
  - `start_date` (OffsetDateTime)
  - `end_date` (OffsetDateTime, nullable) — null = currently active ownership

---

### Value Objects

- **RegistrationPlate**: Immutable wrapper around a normalised plate string.
  - `value` (String) — uppercased, trimmed, max 20 chars
  - Constraint: must match `[A-Z0-9\-]{1,20}`
  - Equality by value (case-insensitive input, stored uppercase)

- **RfidNumber**: Immutable wrapper around an RFID identifier.
  - `value` (String) — hex or alphanumeric, max 50 chars
  - Equality by value

- **VehicleStatus**: Enum value object
  - `ACTIVE` — in service, appears in customer vehicle list
  - `DE_LISTED` — soft-deleted, invisible to customer queries, plate eligible for re-registration

---

### Aggregates

- **Vehicle** (Aggregate Root)
  - **Members**: `Vehicle`, associated `OwnershipRecord` (one active at a time)
  - **Invariants**:
    1. A `registration_plate` within a `vendor_id` scope must be unique among ACTIVE vehicles.
    2. An `rfid_number` must be globally unique when non-null.
    3. Only one `OwnershipRecord` may have `end_date = null` at any time for a given vehicle.
    4. A DE_LISTED vehicle cannot be modified except through a new `register` command (which creates a fresh Vehicle record, not reactivates).
    5. RFID can only be associated while vehicle is ACTIVE.

---

### Domain Events

- **VehicleRegistered**
  - Trigger: Customer successfully registers a new vehicle
  - Payload: `vehicleId`, `customerId`, `vendorId`, `registrationPlate`, `rfidNumber` (nullable), `registeredAt`
  - Consumers: Session Management (to resolve vehicle on RFID auto-identify), Session Search (for indexing)

- **RfidAssociated**
  - Trigger: RFID is linked to a vehicle (at registration time or during manual session association)
  - Payload: `vehicleId`, `rfidNumber`, `associatedAt`
  - Consumers: Session Management (cache refresh)

- **VehicleDelisted**
  - Trigger: Customer de-lists a vehicle
  - Payload: `vehicleId`, `customerId`, `registrationPlate`, `delistedAt`
  - Consumers: Session Search (remove from active index), Session Management (invalidate RFID lookup)

---

### Domain Services

- **VehicleRegistrationService**
  - Operations:
    - `register(customerId, vendorId, plate, rfid?)` → Vehicle
      - Validates plate uniqueness (ACTIVE scope + vendor)
      - Validates RFID uniqueness (if provided)
      - Creates Vehicle + initial OwnershipRecord
      - Publishes `VehicleRegistered`
    - `associateRfid(vehicleId, rfid)` → Vehicle
      - Validates vehicle is ACTIVE
      - Validates RFID uniqueness
      - Updates Vehicle, publishes `RfidAssociated`
  - Dependencies: `VehicleRepository`, `OwnershipRecordRepository`, `DomainEventPublisher`

- **VehicleDelistingService**
  - Operations:
    - `delist(vehicleId, customerId)` → Vehicle
      - Validates ownership (customerId = current_owner_id)
      - Sets status = DE_LISTED, closes active OwnershipRecord
      - Publishes `VehicleDelisted`
  - Dependencies: `VehicleRepository`, `OwnershipRecordRepository`, `DomainEventPublisher`

---

### Repository Interfaces

- **VehicleRepository**
  - Entity: `Vehicle`
  - Methods:
    - `findById(id: UUID): Optional<Vehicle>`
    - `findByPlateAndVendorAndStatus(plate: RegistrationPlate, vendorId: UUID, status: VehicleStatus): Optional<Vehicle>`
    - `findByRfid(rfid: RfidNumber): Optional<Vehicle>`
    - `findByOwnerAndVendor(customerId: UUID, vendorId: UUID, status: VehicleStatus, pageable: Pageable): Page<Vehicle>`
    - `existsByPlateAndVendorAndStatus(plate: RegistrationPlate, vendorId: UUID, status: VehicleStatus): boolean`
    - `existsByRfid(rfid: RfidNumber): boolean`
    - `save(vehicle: Vehicle): Vehicle`

- **OwnershipRecordRepository**
  - Entity: `OwnershipRecord`
  - Methods:
    - `findActiveByVehicleId(vehicleId: UUID): Optional<OwnershipRecord>`
    - `findAllByVehicleId(vehicleId: UUID): List<OwnershipRecord>`
    - `save(record: OwnershipRecord): OwnershipRecord`

---

### Ubiquitous Language

| Term | Definition |
|------|-----------|
| **Vehicle** | A registered automobile (car, van, etc.) participating in the EV charging platform, uniquely identified by a registration plate within a vendor scope |
| **Registration Plate** | The official alphanumeric identifier of a vehicle (e.g., "ABC-1234"); case-insensitive, stored uppercase |
| **RFID Number** | A unique radio-frequency identifier tag physically attached to or stored in the vehicle used for automatic charger identification |
| **RFID Association** | The act of linking an RFID number to a vehicle, enabling auto-identification at charge points |
| **Active Vehicle** | A vehicle with status ACTIVE — visible in customer's vehicle list, eligible to start charging sessions |
| **De-listed Vehicle** | A vehicle with status DE_LISTED — soft-deleted, hidden from active lists, historical sessions preserved; its plate is eligible for re-registration by any customer |
| **Re-registration** | Registering a previously de-listed plate under a new or the same customer, resulting in a brand-new Vehicle record |
| **Ownership Record** | An immutable historical record linking a customer to a vehicle for a specific time period |
| **Current Owner** | The customer holding an active OwnershipRecord for the vehicle (`end_date = null`) |
| **Vendor Scope** | The boundary within which plate uniqueness is enforced — a plate can be registered once per vendor at any given time |
