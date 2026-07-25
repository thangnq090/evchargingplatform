---
unit: 004-billing-pricing
bolt: 006-billing-pricing-1
stage: design
status: complete
updated: 2026-07-26T03:57:00+12:00
---

# Technical Design - Pricing & Billing

## Architecture Pattern

We will use the Hexagonal Architecture (Ports and Adapters) pattern, aligning with the project standards (ADR-003).

- **Domain Layer**: Clean, framework-free business logic. Models `Invoice`, `BillingAccount`, and `TariffRate`.
- **Application Layer**: Use cases and event listeners. Listens to `SessionCompletedEvent` to orchestrate cost calculation and invoice generation. Handles admin reports queries.
- **Infrastructure Layer**: Adapters for Spring Data JPA (PostgreSQL) and Spring Security.
- **Presentation Layer**: REST Controller for admin income reporting.

## Layer Structure

```text
com.evcharging.billing
├── api
│   └── controller
│       └── AdminBillingController.java         (REST API endpoints)
├── application
│   ├── dto
│   │   ├── IncomeReportResponse.java           (Admin report DTO)
│   │   └── InvoiceResponse.java                (Invoice retrieval DTO)
│   ├── listener
│   │   └── SessionCompletedEventListener.java  (Listens to SessionCompletedEvent)
│   └── service
│       └── BillingApplicationService.java       (Use case coordination)
├── domain
│   ├── event
│   │   └── InvoiceGeneratedEvent.java          (Domain event definition)
│   ├── model
│   │   ├── BillingAccount.java                 (Customer billing account aggregate)
│   │   ├── BillingAccountId.java               (Billing account ID)
│   │   ├── Invoice.java                        (Invoice aggregate root)
│   │   ├── InvoiceId.java                      (Invoice ID)
│   │   ├── InvoiceLineItem.java                (Value object for line item)
│   │   ├── InvoiceStatus.java                  (Enum: PENDING, PAID, VOIDED)
│   │   └── TariffRate.java                     (Value object for rate breakdown)
│   ├── repository
│   │   ├── BillingAccountRepository.java       (Repository interface)
│   │   └── InvoiceRepository.java              (Repository interface)
│   └── service
│       └── CostCalculator.java                 (Domain service for cost calculation)
└── infrastructure
    └── persistence
        ├── entity
        │   ├── BillingAccountEntity.java        (JPA Entity)
        │   ├── InvoiceEntity.java               (JPA Entity)
        │   └── InvoiceLineItemEntity.java       (JPA Entity)
        └── repository
            ├── BillingAccountRepositoryAdapter.java (Implements BillingAccountRepository)
            ├── InvoiceRepositoryAdapter.java        (Implements InvoiceRepository)
            ├── JpaBillingAccountRepository.java    (Spring Data JPA)
            └── JpaInvoiceRepository.java           (Spring Data JPA)
```

## API Design

| Endpoint | Method | Request | Response |
|----------|--------|---------|----------|
| `/api/v1/admin/billing/income` | `GET` | Query params:<br>- `startDate` (LocalDate, ISO-8601)<br>- `endDate` (LocalDate, ISO-8601)<br>- `vendorId` (UUID, optional) | `IncomeReportResponse` (JSON detailing total revenue, session count, and breakdown by vendor) |
| `/api/v1/billing/invoices/session/{sessionId}` | `GET` | Path variable: `sessionId` (UUID) | `InvoiceResponse` (JSON containing invoice status, line items, and breakdown) |

## Data Persistence

### Database Schema: `billing`

We will create a Flyway migration `V401__create_billing_schema.sql` creating the following tables under the `billing` schema:

| Table | Columns | Relationships |
|-------|---------|---------------|
| `billing.invoices` | `id` UUID PRIMARY KEY<br>`session_id` UUID NOT NULL UNIQUE<br>`customer_id` UUID NOT NULL<br>`vendor_id` UUID NOT NULL<br>`total_amount` NUMERIC(19,4) NOT NULL<br>`currency` VARCHAR(3) NOT NULL<br>`status` VARCHAR(20) NOT NULL<br>`created_at` TIMESTAMPTZ NOT NULL<br>`version` INTEGER NOT NULL DEFAULT 0 | Unique session ID mapped to invoice |
| `billing.invoice_line_items` | `id` UUID PRIMARY KEY<br>`invoice_id` UUID REFERENCES billing.invoices(id) ON DELETE CASCADE<br>`description` VARCHAR(255) NOT NULL<br>`unit_price_amount` NUMERIC(19,4) NOT NULL<br>`unit_price_currency` VARCHAR(3) NOT NULL<br>`quantity` NUMERIC(19,4) NOT NULL<br>`total_amount` NUMERIC(19,4) NOT NULL<br>`currency` VARCHAR(3) NOT NULL | Line items of the invoice |
| `billing.billing_accounts` | `id` UUID PRIMARY KEY<br>`customer_id` UUID NOT NULL UNIQUE<br>`balance_amount` NUMERIC(19,4) NOT NULL<br>`balance_currency` VARCHAR(3) NOT NULL<br>`total_spent_amount` NUMERIC(19,4) NOT NULL<br>`total_spent_currency` VARCHAR(3) NOT NULL<br>`last_billed_at` TIMESTAMPTZ<br>`version` INTEGER NOT NULL DEFAULT 0 | Customer billing account |

### Performance Indexes
- Index on `billing.invoices(created_at, vendor_id)` for admin reports.
- Index on `billing.invoice_line_items(invoice_id)`.

## Security Design

| Concern | Approach |
|---------|----------|
| **Authentication** | Handled globally via JWT token authentication relay. |
| **Authorization** | - `/api/v1/admin/billing/income`: Secured to `ADMIN` role only.<br>- `/api/v1/billing/invoices/session/{sessionId}`: Secured to `CUSTOMER` (only own invoice) or `VENDOR` (only vendor's invoice), or `ADMIN` roles. |
| **Data Isolation** | Enforced modular boundaries by ensuring queries only target tables in the `billing` schema. |

## NFR Implementation

| Requirement | Design Approach |
|-------------|-----------------|
| **Performance** | DB Indexes on date ranges and vendor IDs ensure fast query execution for admin dashboards. |
| **Scalability** | Asynchronous invoice generation triggered by `SessionCompletedEvent` offloads processing from the user-facing session completion flow. |
| **Reliability** | Database constraints (e.g. unique `session_id` in `invoices`) prevent duplicate invoicing. |

## Error Handling

| Error Type | Code | Response |
|------------|------|----------|
| Invoice not found | `NOT_FOUND` (404) | `{ "message": "Invoice not found for session ID: {sessionId}" }` |
| Unauthorized access | `FORBIDDEN` (403) | `{ "message": "Access Denied" }` |
| Invalid query params | `BAD_REQUEST` (400) | `{ "message": "Invalid date range or parameters" }` |

## External Dependencies

| Service | Purpose | Integration |
|---------|---------|-------------|
| `SessionCompletedEvent` | Triggers the billing process when a charging session ends | Spring Application Event (in-process) |
| `VendorMarkupApi` | Used to calculate final rates (already integrated within the session module at session start) | In-process interface dependency |
| `StationApi` | Used to fetch vendor information for reports/invoices if needed | In-process interface dependency |
