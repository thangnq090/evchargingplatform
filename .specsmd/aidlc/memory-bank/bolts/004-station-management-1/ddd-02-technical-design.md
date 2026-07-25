---
unit: 002-station-management
bolt: 004-station-management-1
stage: design
status: complete
updated: "2026-07-25T10:38:12Z"
---

# Technical Design - Station Management

## Architecture Pattern

**Hexagonal Architecture (Ports & Adapters)** following the platform's modular monolith pattern defined in ADR-003.

**Rationale**:
- Domain layer has zero framework dependencies (pure Java)
- Infrastructure adapters implement ports defined by domain
- Application services orchestrate use cases without exposing infrastructure
- Consistent with existing identity module architecture

---

## Layer Structure

```text
┌─────────────────────────────────────────────────────────────────┐
│                      API Layer (Presentation)                    │
│  StationController, VendorController, AdminMarkupController     │
│  - REST endpoints, Request/Response DTOs                         │
├─────────────────────────────────────────────────────────────────┤
│                   Application Layer (Use Cases)                  │
│  StationApplicationService, MarkupApplicationService             │
│  - Orchestration, transactions, event publishing                 │
├─────────────────────────────────────────────────────────────────┤
│                        Domain Layer                              │
│  Station, Vendor, Connector aggregates                           │
│  StationDomainService, MarkupDomainService                       │
│  StationRepository, VendorRepository (ports)                     │
│  Domain Events: StationCreatedEvent, VendorMarkupChangedEvent    │
├─────────────────────────────────────────────────────────────────┤
│                   Infrastructure Layer                           │
│  JpaStationRepository, JpaVendorRepository (adapters)            │
│  StationJpaEntity, VendorJpaEntity, ConnectorJpaEntity           │
│  PostGIS LocationType, Flyway migrations                         │
│  InMemoryMarkupCacheAdapter (MVP), RedisMarkupCacheAdapter (future) │
└─────────────────────────────────────────────────────────────────┘
```

---

## API Design

### Station Endpoints (Vendor-scoped)

| Endpoint | Method | Request | Response | Authorization |
|----------|--------|---------|----------|---------------|
| `/api/v1/stations` | POST | CreateStationRequest | StationResponse | VENDOR_ADMIN, VENDOR_USER |
| `/api/v1/stations/{stationId}` | GET | - | StationResponse | VENDOR_ADMIN, VENDOR_USER (own), ADMIN (all) |
| `/api/v1/stations` | GET | StationQueryParams | StationListResponse | VENDOR (own), ADMIN (all) |
| `/api/v1/stations/{stationId}` | PATCH | UpdateStationRequest | StationResponse | VENDOR_ADMIN, VENDOR_USER |
| `/api/v1/stations/{stationId}` | DELETE | - | 204 No Content | VENDOR_ADMIN |
| `/api/v1/stations/{stationId}/status` | PUT | ChangeStatusRequest | StationResponse | VENDOR_ADMIN, VENDOR_USER |
| `/api/v1/stations/nearby` | GET | NearbyQueryParams | StationListResponse | PUBLIC (all authenticated) |

### Vendor Markup Endpoints (Admin-scoped)

| Endpoint | Method | Request | Response | Authorization |
|----------|--------|---------|----------|---------------|
| `/api/v1/admin/vendors/{vendorId}/markup` | PUT | SetMarkupRequest | VendorResponse | ADMIN |
| `/api/v1/vendors/{vendorId}/markup` | GET | - | MarkupResponse | VENDOR_ADMIN (own), ADMIN (all) |

### Request/Response Schemas

**CreateStationRequest**:
```json
{
  "name": "Downtown Fast Charger",
  "groupLabel": "Downtown",
  "unitPriceTenthCents": 350,
  "location": { "lat": 52.5200, "lng": 13.4050 },
  "connectors": [
    { "type": "CCS", "maxPowerKw": 150 },
    { "type": "TYPE_2", "maxPowerKw": 22 }
  ]
}
```

**StationResponse**:
```json
{
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "vendorId": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Downtown Fast Charger",
    "groupLabel": "Downtown",
    "unitPriceTenthCents": 350,
    "status": "AVAILABLE",
    "location": { "lat": 52.5200, "lng": 13.4050 },
    "connectors": [
      { "id": "conn-uuid-1", "type": "CCS", "maxPowerKw": 150, "status": "AVAILABLE" },
      { "id": "conn-uuid-2", "type": "TYPE_2", "maxPowerKw": 22, "status": "AVAILABLE" }
    ],
    "createdAt": "2026-07-25T10:00:00Z",
    "updatedAt": "2026-07-25T10:00:00Z"
  },
  "meta": { "timestamp": "2026-07-25T10:00:00Z", "version": "v1" }
}
```

**SetMarkupRequest**:
```json
{
  "markupBasisPoints": 1500
}
```

**NearbyQueryParams**:
```
?lat=52.5200&lng=13.4050&radiusKm=10&limit=20&status=AVAILABLE
```

---

## Data Persistence

### Database Schema

**Schema**: `station` (per ADR-004: schema-per-module)

**Tables**:

| Table | Columns | Indexes | Relationships |
|-------|---------|---------|---------------|
| `station.stations` | `id UUID PK`, `vendor_id UUID NOT NULL`, `name VARCHAR(100) NOT NULL`, `group_label VARCHAR(50)`, `unit_price_tenth_cents INTEGER NOT NULL`, `status VARCHAR(20) NOT NULL`, `location GEOGRAPHY(Point, 4326) NOT NULL`, `created_at TIMESTAMPTZ`, `updated_at TIMESTAMPTZ`, `deleted_at TIMESTAMPTZ` | `idx_stations_vendor_id`, `idx_stations_status`, `idx_stations_location (GIST)`, `idx_stations_vendor_name UNIQUE(vendor_id, name) WHERE deleted_at IS NULL` | FK: `vendor_id → identity.vendors(id)` (read-only) |
| `station.connectors` | `id UUID PK`, `station_id UUID NOT NULL`, `type VARCHAR(20) NOT NULL`, `max_power_kw INTEGER NOT NULL`, `status VARCHAR(20) NOT NULL`, `created_at TIMESTAMPTZ` | `idx_connectors_station_id` | FK: `station_id → station.stations(id) ON DELETE CASCADE` |
| `station.vendor_markup_history` | `id UUID PK`, `vendor_id UUID NOT NULL`, `markup_basis_points INTEGER NOT NULL`, `changed_by UUID NOT NULL`, `changed_at TIMESTAMPTZ` | `idx_markup_history_vendor_id` | FK: `vendor_id → identity.vendors(id)`, `changed_by → identity.users(id)` |

**PostGIS Configuration**:
- Extension: `CREATE EXTENSION IF NOT EXISTS postgis;`
- Location column: `location GEOGRAPHY(Point, 4326)` (WGS 84)
- Spatial index: `CREATE INDEX idx_stations_location ON station.stations USING GIST (location);`

**Row-Level Security** (ADR-017):
```sql
ALTER TABLE station.stations ENABLE ROW LEVEL SECURITY;

CREATE POLICY vendor_isolation ON station.stations
  USING (vendor_id = current_setting('app.current_vendor_id', true)::uuid)
  WITH CHECK (vendor_id = current_setting('app.current_vendor_id', true)::uuid);

-- Admin bypass
CREATE POLICY admin_full_access ON station.stations
  USING (current_setting('app.user_role', true) = 'ADMIN');
```

### Flyway Migrations

**Location**: `src/main/resources/db/migration/station/`

**Naming Convention**: `V{version}__{description}.sql`

| File | Description |
|------|-------------|
| `V001__create_station_schema.sql` | Create `station` schema, PostGIS extension |
| `V002__create_stations_table.sql` | Create stations table with location column |
| `V003__create_connectors_table.sql` | Create connectors table |
| `V004__create_vendor_markup_history.sql` | Create markup audit table |
| `V005__add_station_indexes.sql` | Add spatial and performance indexes |
| `V006__enable_rls_policies.sql` | Enable Row-Level Security |

---

## Security Design

| Concern | Approach |
|---------|----------|
| **Authentication** | JWT validation via Spring Security OAuth2 Resource Server (per ADR-007) |
| **Authorization** | Role-based + scope-based: `@PreAuthorize("hasRole('VENDOR_ADMIN')")`, scope check: `station:write` |
| **Vendor Isolation** | Row-Level Security (RLS) enforces vendor_id = JWT vendor_id claim (per ADR-017) |
| **Admin Bypass** | ADMIN role bypasses RLS via policy or `SET LOCAL app.current_vendor_id = 'all'` |
| **Input Validation** | Bean Validation (JSR-380) on request DTOs |
| **Location Validation** | Lat: -90 to 90, Lng: -180 to 180, validated at API layer |
| **Price Validation** | Unit price >= 0, markup 0-10000 basis points |
| **Idempotency** | Idempotency-Key header on POST/PATCH, stored in Redis for 24h |

### Authorization Matrix

| Operation | ADMIN | VENDOR_ADMIN | VENDOR_USER | CUSTOMER |
|-----------|-------|--------------|-------------|----------|
| Create Station | ✅ All | ✅ Own vendor | ❌ | ❌ |
| Read Station | ✅ All | ✅ Own vendor | ✅ Own vendor | ✅ Public (AVAILABLE only) |
| Update Station | ✅ All | ✅ Own vendor | ✅ Own vendor | ❌ |
| Delete Station | ✅ All | ✅ Own vendor | ❌ | ❌ |
| Change Status | ✅ All | ✅ Own vendor | ✅ Own vendor | ❌ |
| Set Markup | ✅ All | ❌ | ❌ | ❌ |
| View Markup | ✅ All | ✅ Own vendor | ❌ | ❌ |

---

## NFR Implementation

| Requirement | Design Approach |
|-------------|-----------------|
| **Performance** | Spatial index on location for <100ms proximity queries; connection pooling via HikariCP; Redis cache for vendor markup lookups |
| **Scalability** | Read replicas for station queries; horizontal scaling via HPA; no sticky sessions needed |
| **Reliability** | Soft-delete preserves data; markup history for audit trail; optimistic locking via `updated_at` |
| **Data Integrity** | FK constraints to identity.vendors (read-only); transaction boundaries at application service level |
| **Observability** | OpenTelemetry instrumentation; structured logs with station_id, vendor_id; metrics: `stations_created_total`, `station_status_changes_total` |
| **Multi-Tenancy** | RLS enforces vendor isolation; JWT vendor_id claim drives RLS policy; admin has global view |

### Performance Optimizations

1. **Spatial Queries**: PostGIS GIST index enables efficient radius searches
2. **Caching**: Vendor markup cached via `MarkupCachePort` abstraction. MVP uses in-memory Caffeine (5 min TTL, 10k max entries). Swappable to Redis via `RedisMarkupCacheAdapter` without code changes. Event-driven invalidation via `VendorMarkupChangedEvent`.
3. **Connection Pooling**: HikariCP with max 20 connections per instance
4. **Pagination**: Cursor-based pagination for station lists (max 100 per page)
5. **Lazy Loading**: Connectors loaded on-demand via JPA `@OneToMany(fetch = LAZY)`

---

## Error Handling

| Error Type | HTTP Status | Code | Response |
|------------|-------------|------|----------|
| Station not found | 404 | `STATION_NOT_FOUND` | `ProblemDetail` with stationId |
| Vendor not found | 404 | `VENDOR_NOT_FOUND` | `ProblemDetail` with vendorId |
| Station name duplicate | 409 | `STATION_NAME_EXISTS` | `ProblemDetail` with name field |
| Invalid location | 400 | `INVALID_LOCATION` | `ProblemDetail` with field errors |
| Invalid markup range | 400 | `INVALID_MARKUP` | `ProblemDetail` with markup field |
| Vendor access denied | 403 | `VENDOR_ACCESS_DENIED` | `ProblemDetail` |
| Station deleted | 410 | `STATION_DELETED` | `ProblemDetail` for soft-deleted station |

**Exception Hierarchy**:
```
DomainException
├── StationNotFoundException
├── StationDeletedException
├── DuplicateStationNameException
├── InvalidLocationException
└── InvalidMarkupException
```

---

## Integration Points

### Internal Dependencies (Ports)

| Module | Integration Type | Purpose |
|--------|------------------|---------|
| Identity | Domain Event Listener | Listen to `VendorCreatedEvent` to initialize vendor context |
| Identity | Read-only Repository | Access Vendor aggregate via `VendorRepository` (no writes) |

### Vendor Markup Cache Port

The domain layer defines a generic `MarkupCachePort` interface. The application layer depends on this port, not on any concrete cache implementation. This enables swapping the cache backend without touching business logic.

**Port Interface** (domain layer):
```java
public interface MarkupCachePort {
    Optional<Integer> getMarkupBasisPoints(VendorId vendorId);
    void putMarkupBasisPoints(VendorId vendorId, int markupBasisPoints);
    void evict(VendorId vendorId);
}
```

**MVP Adapter**: `InMemoryMarkupCacheAdapter` — uses `Caffeine` (in-process, zero network dependency).
**Future Adapter**: `RedisMarkupCacheAdapter` — uses Spring Data Redis for distributed caching (multi-pod deployments).

**Cache Configuration** (in assembly/infrastructure config):
```java
@Configuration
public class CacheConfig {
    // MVP: In-memory Caffeine cache
    @Bean
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    public MarkupCachePort markupCachePort() {
        Cache<String, Integer> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(10_000)
            .build();
        return new InMemoryMarkupCacheAdapter(cache);
    }

    // Future: Redis cache (activate by providing Redis config)
    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public MarkupCachePort redisMarkupCachePort(RedisConnectionFactory factory) {
        return new RedisMarkupCacheAdapter(factory);
    }
}
```

**Cache Invalidation**: Triggered by `VendorMarkupChangedEvent` via event listener.

### Published Events

| Event | Consumers | Purpose |
|-------|-----------|---------|
| `StationCreatedEvent` | Session module, Device Gateway | Notify new station available |
| `StationUpdatedEvent` | Session module, Admin Portal | Sync station data |
| `StationDeletedEvent` | Session module | Prevent new sessions on deleted station |
| `StationStatusChangedEvent` | Device Gateway, Admin Portal | Update station availability |
| `VendorMarkupChangedEvent` | Billing module | Recalculate pricing for future sessions |

### Event Publishing Pattern

```java
@Transactional
public Station createStation(CreateStationCommand command) {
    Station station = stationDomainService.createStation(command);
    stationRepository.save(station);
    // Event published automatically via @DomainEvents
    applicationEventPublisher.publishEvent(
        new StationCreatedEvent(station.getId(), station.getVendorId(), ...)
    );
    return station;
}
```

---

## Testing Strategy

### Unit Tests (Domain Layer)

- Domain entity behavior (Station, Connector)
- Domain service logic (MarkupDomainService)
- Value object validation (Location, Money, MarkupPercentage)

### Integration Tests (Infrastructure Layer)

- Repository operations with Testcontainers (PostgreSQL + PostGIS)
- Spatial queries (findNearby)
- RLS policy enforcement
- Flyway migrations

### API Tests (Presentation Layer)

- REST endpoint contracts (MockMvc)
- Authorization checks (role/scope)
- Request validation
- Error responses

### Test Data Setup

```java
@Testcontainers
class StationRepositoryIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgis/postgis:16-3.4")
        .withDatabaseName("evcharging_test");
    
    @Test
    void shouldFindNearbyStations() {
        Station station = stationRepository.save(
            new Station(stationId, vendorId, "Test Station", 
                new Location(52.5200, 13.4050), 350, StationStatus.AVAILABLE)
        );
        
        List<Station> nearby = stationRepository.findNearby(
            new Location(52.5200, 13.4050), 10.0
        );
        
        assertThat(nearby).contains(station);
    }
}
```

---

## Observability

### Metrics (Micrometer)

| Metric | Type | Tags | Description |
|--------|------|------|-------------|
| `stations_created_total` | Counter | vendor_id | Total stations created |
| `stations_deleted_total` | Counter | vendor_id | Total stations soft-deleted |
| `station_status_changes_total` | Counter | vendor_id, status | Status transitions |
| `markup_updates_total` | Counter | vendor_id | Markup changes |
| `nearby_query_duration_seconds` | Timer | - | Proximity query latency |

### Logging (SLF4J + Logback)

**Structured JSON format with MDC**:
```json
{
  "timestamp": "2026-07-25T10:30:00Z",
  "level": "INFO",
  "logger": "com.evcharging.station.application.StationApplicationService",
  "message": "Station created",
  "stationId": "550e8400-e29b-41d4-a716-446655440000",
  "vendorId": "123e4567-e89b-12d3-a456-426614174000",
  "correlationId": "abc-123-def"
}
```

### Tracing (OpenTelemetry)

**Spans**:
- `StationApplicationService.createStation` (business operation)
- `StationRepository.save` (database)
- `StationRepository.findNearby` (spatial query)

**Baggage**: `station_id`, `vendor_id` propagated to downstream services

---

## Completion Criteria

- [x] Architecture pattern selected (Hexagonal)
- [x] All layers designed with responsibilities
- [x] API contracts defined (9 endpoints)
- [x] Database schema designed (3 tables with PostGIS)
- [x] Security patterns applied (RLS, RBAC, scopes)
- [x] NFRs addressed in design (performance, scalability, observability)
- [x] Integration points documented (events, ports)
- [x] Testing strategy defined
