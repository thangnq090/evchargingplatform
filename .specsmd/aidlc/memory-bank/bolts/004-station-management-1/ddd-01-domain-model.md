---
unit: 002-station-management
bolt: 004-station-management-1
stage: model
status: complete
updated: "2026-07-25T10:25:22Z"
---

# Static Model - Station Management (Bolt 1)

## Bounded Context

The Station Management Bounded Context is responsible for managing the lifecycle of charging stations (chargepoints), their geospatial location data, vendor ownership, and admin-configurable markup percentages. This context handles chargepoint CRUD operations, availability status management, and vendor markup configuration for billing calculations. For Bolt 1, the context focuses on core chargepoint management with PostGIS location and vendor markup settings.

---

## Domain Entities

| Entity | Properties | Business Rules |
|--------|------------|----------------|
| **Station** | `id`: StationId (UUID)<br>`vendorId`: VendorId<br>`name`: String<br>`groupLabel`: String (nullable)<br>`unitPriceTenthCents`: Integer<br>`status`: StationStatus<br>`location`: Location (GEOGRAPHY Point)<br>`createdAt`: Instant<br>`updatedAt`: Instant<br>`deletedAt`: Instant (nullable) | - Station ID is user-assigned (displayed on physical unit for support).<br>- Name must be non-blank and unique within vendor scope.<br>- Unit price stored as integer (tenths of cents) to avoid floating point precision issues.<br>- Soft-delete via `deletedAt` timestamp preserves historical session data.<br>- Status transitions: AVAILABLE → UNAVAILABLE/MAINTENANCE and back.<br>- Location must be valid WGS 84 coordinates. |
| **Vendor** | `id`: VendorId (UUID)<br>`name`: String<br>`accountNumber`: String<br>`markupPercentage`: Integer<br>`createdAt`: Instant<br>`updatedAt`: Instant | - Vendor name must be unique.<br>- Markup percentage stored as integer (e.g., 1500 = 15.00%).<br>- Markup is applied to vendor's unit prices for session billing.<br>- Markup changes do NOT affect existing sessions (captured at session start). |
| **Connector** | `id`: ConnectorId (UUID)<br>`stationId`: StationId<br>`type`: ConnectorType<br>`maxPowerKw`: Integer<br>`status`: ConnectorStatus<br>`createdAt`: Instant | - A station has 1-N connectors.<br>- Connector type: CCS, CHAdeMO, TYPE_2.<br>- Connector status independent of station status. |

---

## Value Objects

| Value Object | Properties | Constraints |
|--------------|------------|-------------|
| **StationId** | `value`: UUID | - Unique identifier for station.<br>- User-assigned, not auto-generated.<br>- Displayed on physical unit for support calls. |
| **VendorId** | `value`: UUID | - References identity module's Vendor aggregate. |
| **Location** | `latitude`: BigDecimal<br>`longitude`: BigDecimal | - WGS 84 coordinate system.<br>- Latitude: -90 to 90.<br>- Longitude: -180 to 180.<br>- Stored as PostGIS `GEOGRAPHY(Point, 4326)` for spatial queries. |
| **Money** | `tenthCents`: Integer | - Integer representation avoids floating point errors.<br>- 1 tenth-cent = 0.001 USD.<br>- All monetary calculations use this value object. |
| **MarkupPercentage** | `basisPoints`: Integer | - Stored as basis points (1 BP = 0.01%).<br>- Example: 1500 basis points = 15.00%.<br>- Range: 0 to 10000 (0% to 100%). |

---

## Aggregates

| Aggregate Root | Members | Invariants |
|----------------|---------|------------|
| **Station** | Station (Root), Connector (0-N) | - Station must have exactly one vendor.<br>- Station must have at least one connector to be operational.<br>- Station cannot be hard-deleted if sessions exist (enforced by soft-delete).<br>- Unit price must be non-negative. |
| **Vendor** | Vendor (Root) | - Vendor name must be unique.<br>- Markup percentage must be 0-10000 basis points.<br>- Vendor is owned by identity module; station module holds reference. |

**Note**: Vendor aggregate is defined in identity module. Station module holds `VendorId` reference and can read vendor data, but cannot modify vendor entity. Markup configuration is a shared responsibility: identity module owns Vendor aggregate, station module provides admin interface for markup updates.

---

## Domain Events

| Event | Trigger | Payload |
|-------|---------|---------|
| **StationCreatedEvent** | Vendor creates a new chargepoint. | `stationId`: UUID, `vendorId`: UUID, `name`: String, `location`: Location, `unitPrice`: Integer, `timestamp`: Instant |
| **StationUpdatedEvent** | Station fields are modified. | `stationId`: UUID, `vendorId`: UUID, `changes`: Map<String, Object>, `timestamp`: Instant |
| **StationDeletedEvent** | Station is soft-deleted. | `stationId`: UUID, `vendorId`: UUID, `timestamp`: Instant |
| **StationStatusChangedEvent** | Station availability status changes. | `stationId`: UUID, `vendorId`: UUID, `oldStatus`: StationStatus, `newStatus`: StationStatus, `timestamp`: Instant |
| **VendorMarkupChangedEvent** | Admin updates vendor markup. | `vendorId`: UUID, `oldMarkup`: Integer, `newMarkup`: Integer, `changedBy`: UUID (admin), `timestamp`: Instant |

---

## Domain Services

| Service | Operations | Dependencies |
|---------|------------|--------------|
| **StationDomainService** | - `createStation(vendorId, name, groupLabel, unitPrice, location, connectors)`: Creates new station with connectors.<br>- `updateStation(stationId, fields)`: Updates station fields with validation.<br>- `deleteStation(stationId)`: Soft-deletes station.<br>- `changeStatus(stationId, newStatus)`: Updates availability status. | `StationRepository`<br>`VendorRepository` (read-only) |
| **MarkupDomainService** | - `setVendorMarkup(vendorId, markupBasisPoints, adminId)`: Sets vendor markup percentage.<br>- `getEffectivePrice(stationId)`: Returns unit price with markup applied. | `VendorRepository`<br>`StationRepository` |

---

## Repository Interfaces

| Repository | Entity | Methods |
|------------|--------|---------|
| **StationRepository** | Station | `save(Station): Station`<br>`findById(StationId): Optional<Station>`<br>`findByVendorId(VendorId): List<Station>`<br>`findByVendorIdAndStatus(VendorId, StationStatus): List<Station>`<br>`findByVendorIdAndDeletedAtIsNull(VendorId): List<Station>`<br>`findNearby(Location, radiusKm): List<Station>`<br>`existsByVendorIdAndName(VendorId, String): boolean` |
| **ConnectorRepository** | Connector | `save(Connector): Connector`<br>`findByStationId(StationId): List<Connector>`<br>`findById(ConnectorId): Optional<Connector>` |
| **VendorRepository** | Vendor | `findById(VendorId): Optional<Vendor>`<br>`save(Vendor): Vendor`<br>`existsByName(String): boolean` |

**Note**: VendorRepository in station module is read-only for most operations. Markup updates write through to identity module's Vendor table.

---

## Ubiquitous Language

| Term | Definition |
|------|------------|
| **Station** | A physical EV charging station (chargepoint) with one or more connectors, owned by a vendor. |
| **Chargepoint** | Synonym for Station; used interchangeably in business context. |
| **Connector** | A physical charging plug on a station (CCS, CHAdeMO, Type 2). |
| **Vendor** | An independent business entity that owns and operates charging stations. |
| **Group Label** | Optional categorization for stations within a vendor's portfolio (e.g., "Downtown", "Highway"). |
| **Unit Price** | Base price per kWh in tenths of cents (integer precision). |
| **Markup** | Platform fee percentage added to vendor's unit price, configured per vendor. |
| **Availability Status** | Station operational state: AVAILABLE, UNAVAILABLE, MAINTENANCE. |
| **Location** | Geographic coordinates (lat/lng) stored as PostGIS GEOGRAPHY Point for spatial queries. |
| **Soft Delete** | Logical deletion via `deletedAt` timestamp, preserving historical data. |
| **Basis Points** | Unit for markup percentage (1 BP = 0.01%, e.g., 1500 BP = 15.00%). |

---

## Story Coverage

| Acceptance Criterion | Modelled By |
|---------------------|-------------|
| Vendor creates chargepoint with location | `StationDomainService.createStation()`, `Location` value object, `StationCreatedEvent` |
| Vendor updates chargepoint name, group, price, availability | `StationDomainService.updateStation()`, `StationUpdatedEvent` |
| Vendor removes chargepoint (soft-delete) | `StationDomainService.deleteStation()`, `StationDeletedEvent`, `deletedAt` field |
| Location stored as PostGIS GEOGRAPHY(Point, 4326) | `Location` value object, `findNearby()` repository method |
| Vendor queries only their chargepoints | `StationRepository.findByVendorIdAndDeletedAtIsNull()` with vendor scoping |
| Admin sets vendor markup percentage | `MarkupDomainService.setVendorMarkup()`, `VendorMarkupChangedEvent` |
| Markup applied to vendor's unit prices | `MarkupDomainService.getEffectivePrice()`, `MarkupPercentage` value object |
| New sessions use new markup, historical sessions retain original | Markup captured at session start (handled by session module) |

---

## Completion Criteria

- [x] All domain entities identified and documented (Station, Vendor, Connector)
- [x] Business rules captured for each entity
- [x] Aggregate boundaries defined (Station aggregate, Vendor reference)
- [x] Domain events specified (5 events for CRUD and status changes)
- [x] Repository interfaces defined (StationRepository, ConnectorRepository, VendorRepository)
- [x] All story acceptance criteria covered by domain model
- [x] Value objects defined for precision-sensitive data (Money, Location, MarkupPercentage)
