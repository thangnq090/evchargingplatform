# API Conventions

## Overview
RESTful API design conventions for the EV Charging Platform ensuring consistency, discoverability, and maintainability across all modules. Aligned with ADR-0001 Decisions #5, #6, #11, #12.

## API Style

**REST + OpenAPI 3.1** for synchronous operations
**Server-Sent Events (SSE)** for real-time updates
**OCPP 1.6J over WebSocket** for charger communication (Device Gateway)

**Base URL**: `https://api.evcharging.com/api/v1`

**HTTP Methods**:
| Method | Purpose | Idempotent |
|--------|---------|------------|
| GET | Retrieve resource(s) | Yes |
| POST | Create resource, trigger action | No |
| PUT | Full resource replacement | Yes |
| PATCH | Partial resource update | Yes |
| DELETE | Remove resource | Yes |

**Resource Naming**:
- Plural nouns: `/stations`, `/sessions`, `/invoices`
- Hierarchical: `/stations/{stationId}/connectors`, `/sessions/{sessionId}/meter-readings`
- No verbs in URLs: Use HTTP methods instead

## Versioning

**URL Path Versioning**: `/api/v1/...`

**Strategy**:
- v1 = Current stable version
- Breaking changes → v2 (new path)
- Non-breaking additions: Add fields, new optional endpoints (same version)
- Deprecation: `Sunset` header + 6-month notice
- Version negotiation: `Accept: application/vnd.evcharging.v1+json` (optional)

## Response Format

### Success Responses

**Single Resource**:
```json
{
  "data": {
    "id": "sta_abc123",
    "name": "Downtown Fast Charger",
    "status": "AVAILABLE",
    "connectors": [
      { "id": "conn_1", "type": "CCS", "maxPowerKw": 150, "status": "AVAILABLE" }
    ],
    "location": { "lat": 52.5200, "lng": 13.4050 },
    "createdAt": "2026-01-15T10:30:00Z",
    "updatedAt": "2026-07-20T14:22:00Z"
  },
  "meta": {
    "timestamp": "2026-07-24T10:30:00Z",
    "version": "v1"
  }
}
```

**Collection**:
```json
{
  "data": [
    { "id": "sta_abc123", "name": "...", "status": "AVAILABLE" },
    { "id": "sta_def456", "name": "...", "status": "CHARGING" }
  ],
  "meta": {
    "timestamp": "2026-07-24T10:30:00Z",
    "pagination": {
      "cursor": "eyJpZCI6InN0YV9kZWY0NTYifQ==",
      "limit": 20,
      "hasMore": true
    }
  }
}
```

**Created Resource** (201):
```json
{
  "data": { "id": "sta_new789", "name": "New Station", "...": "..." },
  "meta": { "timestamp": "...", "version": "v1" }
}
```

### Error Responses (RFC 7807 ProblemDetail)

```json
{
  "type": "https://api.evcharging.com/errors/validation-failed",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Request validation failed for 2 fields",
  "instance": "/api/v1/stations",
  "timestamp": "2026-07-24T10:30:00Z",
  "errorCode": "VALIDATION_FAILED",
  "fieldErrors": {
    "name": "Station name is required",
    "connectors[0].maxPowerKw": "Must be between 1 and 350"
  }
}
```

**Common Error Types**:
| Type URI | Title | Status | When |
|----------|-------|--------|------|
| `/errors/validation-failed` | Validation Failed | 400 | Invalid request body/params |
| `/errors/unauthorized` | Unauthorized | 401 | Missing/invalid token |
| `/errors/forbidden` | Forbidden | 403 | Insufficient permissions |
| `/errors/not-found` | Not Found | 404 | Resource doesn't exist |
| `/errors/conflict` | Conflict | 409 | Idempotency key conflict, optimistic lock |
| `/errors/rate-limited` | Too Many Requests | 429 | Rate limit exceeded |
| `/errors/internal` | Internal Server Error | 500 | Unexpected server error |
| `/errors/service-unavailable` | Service Unavailable | 503 | Downstream dependency down |

**HTTP Status Codes**:
| Code | Use Case |
|------|----------|
| 200 | Successful GET, PUT, PATCH |
| 201 | Successful POST (resource created) |
| 204 | Successful DELETE, POST (no content) |
| 400 | Validation error, malformed request |
| 401 | Missing/invalid authentication |
| 403 | Authenticated but not authorized |
| 404 | Resource not found |
| 409 | Conflict (idempotency, concurrent modification) |
| 422 | Semantic error (business rule violation) |
| 429 | Rate limited |
| 500 | Internal server error |
| 503 | Service unavailable (circuit breaker open) |

## Pagination Strategy

**Cursor-based Pagination** (opaque cursor, stable ordering)

**Request**:
```
GET /api/v1/stations?limit=20&cursor=eyJpZCI6InN0YV9hYmMifQ==
GET /api/v1/sessions?limit=50&status=CHARGING&cursor=...
```

**Response**:
```json
{
  "data": [...],
  "meta": {
    "pagination": {
      "cursor": "eyJpZCI6InN0YV9kZWY0NTYifQ==",
      "limit": 20,
      "hasMore": true
    }
  }
}
```

**Rules**:
- Default limit: 20, max: 100
- Cursor encodes: last seen ID + sort criteria
- Sort: Default `createdAt DESC` (newest first)
- Explicit sort: `?sort=name:asc,createdAt:desc`
- No offset pagination (performance on large datasets)

## Filtering & Search

**Query Parameters**:
```
GET /api/v1/stations?status=AVAILABLE&vendorId=ven_123&connectorType=CCS
GET /api/v1/sessions?startedAfter=2026-07-01T00:00:00Z&endedBefore=2026-07-31T23:59:59Z
GET /api/v1/invoices?search=INV-2026&status=PAID,OVERDUE
```

**Conventions**:
- Exact match: `?field=value`
- Multiple values: `?status=PAID,OVERDUE` (OR logic)
- Range: `?field[gte]=100&field[lte]=500`
- Date ranges: `startedAfter`, `startedBefore` (ISO 8601)
- Text search: `?search=query` (full-text where supported)
- Prefix: `filter[field]=value` for complex filters

## Request/Response Standards

### Headers

**Request**:
| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | Yes* | `Bearer <jwt>` |
| `Content-Type` | Yes* | `application/json` (for POST/PUT/PATCH) |
| `Accept` | No | `application/vnd.evcharging.v1+json` |
| `Idempotency-Key` | For mutations | UUID for safe retries |
| `X-Correlation-ID` | Auto | Added by Gateway if missing |

**Response**:
| Header | Description |
|--------|-------------|
| `Content-Type` | `application/json` or `application/problem+json` |
| `X-Correlation-ID` | Echoed for tracing |
| `X-Request-ID` | Unique request identifier |
| `RateLimit-Limit` | Request limit in window |
| `RateLimit-Remaining` | Remaining requests |
| `RateLimit-Reset` | Unix timestamp of reset |
| `Sunset` | Deprecation date (RFC 8594) |

### Request Body

**Create** (POST):
```json
{
  "name": "Highway Rest Stop Charger",
  "location": { "lat": 48.8566, "lng": 2.3522 },
  "connectors": [
    { "type": "CCS", "maxPowerKw": 300 },
    { "type": "TYPE_2", "maxPowerKw": 22 }
  ],
  "vendorId": "ven_abc123"
}
```

**Update** (PATCH - partial):
```json
{
  "status": "MAINTENANCE",
  "connectors": [
    { "id": "conn_1", "status": "UNAVAILABLE" }
  ]
}
```

**Bulk Operations** (POST to collection with action):
```json
POST /api/v1/stations/bulk
{
  "action": "updateStatus",
  "ids": ["sta_1", "sta_2", "sta_3"],
  "payload": { "status": "MAINTENANCE" }
}
```

### Idempotency

**Required for**: POST (create), PATCH, PUT, DELETE
**Header**: `Idempotency-Key: <uuid>`
**Behavior**:
- Server stores key + response for 24 hours
- Duplicate key → returns original response (2xx or 4xx)
- Different payload with same key → 409 Conflict
- Key format: UUID v4

## Authentication & Authorization

**Authentication**: JWT Bearer tokens (Spring Cloud Gateway validated)
**Token Claims**:
```json
{
  "sub": "usr_abc123",
  "roles": ["CUSTOMER"],
  "scopes": ["station:read", "session:start"],
  "vendorId": "ven_xyz789",  // for VENDOR role
  "exp": 1721818200
}
```

**Authorization** (per endpoint):
| Resource | ADMIN | VENDOR | CUSTOMER |
|----------|-------|--------|----------|
| `/stations` (read) | ✅ All | ✅ Own | ✅ Public |
| `/stations` (write) | ✅ All | ✅ Own | ❌ |
| `/sessions` (start) | ✅ | ❌ | ✅ Own |
| `/sessions` (read) | ✅ All | ✅ Own stations | ✅ Own |
| `/billing` (read) | ✅ All | ✅ Own | ✅ Own |
| `/payments` | ✅ All | ❌ | ✅ Own |
| `/admin/*` | ✅ | ❌ | ❌ |

**Scope Format**: `{resource}:{action}` (e.g., `station:read`, `session:start`)

## Real-time (SSE)

**Endpoint**: `GET /api/v1/events?channels=session:{sessionId},station:{stationId}`

**Headers**:
```
Accept: text/event-stream
Cache-Control: no-cache
Connection: keep-alive
Authorization: Bearer <jwt>
```

**Event Format**:
```
event: session.status.changed
data: {"sessionId":"ses_abc","status":"CHARGING","timestamp":"2026-07-24T10:30:00Z"}

event: station.health.changed
data: {"stationId":"sta_123","health":"DEGRADED","details":{"connector_2":"ERROR"}}
```

**Channels**:
- `session:{sessionId}` — Session lifecycle events
- `station:{stationId}` — Station health, connector status
- `vendor:{vendorId}` — Vendor-scoped alerts
- `admin:alerts` — System-wide alerts (ADMIN only)

**Reconnection**: Client handles reconnect with `Last-Event-ID` header

## OpenAPI Documentation

**Generation**: `springdoc-openapi` (Spring Boot) + `swagger-ui`
**Location**: `/api/v1/docs` (Swagger UI), `/api/v1/openapi.json` (spec)

**Annotations** (required on all endpoints):
```java
@Operation(
  summary = "Start charging session",
  description = "Initiates a charging session for the given connector",
  security = @SecurityRequirement(name = "bearerAuth")
)
@ApiResponses({
  @ApiResponse(responseCode = "201", description = "Session created",
    content = @Content(schema = @Schema(implementation = SessionResponse.class))),
  @ApiResponse(responseCode = "404", description = "Connector not found",
    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
  @ApiResponse(responseCode = "409", description = "Connector already in use",
    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
  @ApiResponse(responseCode = "422", description = "Station offline or unavailable",
    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
@PostMapping("/connectors/{connectorId}/sessions")
public ResponseEntity<SessionResponse> startSession(...) { ... }
```

**Tags**: Group by module (`station`, `session`, `billing`, `payment`, `identity`)

## Rate Limiting

**Headers** (on all responses):
```
RateLimit-Limit: 1000
RateLimit-Remaining: 999
RateLimit-Reset: 1721821800
```

**Limits** (per user/IP):
| Tier | Requests/Minute | Burst |
|------|-----------------|-------|
| Anonymous | 60 | 10 |
| Authenticated | 1000 | 200 |
| Vendor Portal | 5000 | 1000 |
| Admin | 10000 | 2000 |

**Device Gateway**: Per-station limits (OCPP message rate)

## Webhooks (Outbound)

**Delivery**: POST to registered URL with `X-Webhook-Signature` (HMAC-SHA256)
**Retry**: Exponential backoff (1m, 5m, 15m, 1h, 6h, 24h) — max 7 days
**Events**: `session.completed`, `payment.succeeded`, `payment.failed`, `station.offline`, `invoice.generated`

## Decision Relationships

- **Tech Stack → API Conventions**: Spring Boot 4 + springdoc → OpenAPI 3.1 generation
- **System Architecture → API Conventions**: Module boundaries → API tags, versioning per module
- **Auth (ADR-0001 #6) → API Conventions**: JWT + scopes → Authorization matrix
- **Observability (ADR-0001 #9) → API Conventions**: Correlation IDs, structured errors
- **Rate Limiting (ADR-0001 #11) → API Conventions**: Headers, tiers
- **Multi-Tenancy (ADR-0001 #12) → API Conventions**: Vendor-scoped filtering