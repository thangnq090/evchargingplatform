---
unit: 004-billing-pricing
bolt: 006-billing-pricing-1
stage: model
status: complete
updated: 2026-07-26T03:56:00+12:00
---

# Static Model - Pricing & Billing

## Bounded Context

The **Pricing & Billing** bounded context manages charging tariff policies, calculates session charging costs, generates invoices, updates billing accounts, and provides income reporting for administrators and vendors. It is decoupled from the Session Management and Payment Processing contexts, interacting with them via asynchronous domain events.

## Domain Entities

| Entity | Properties | Business Rules |
|--------|------------|----------------|
| **Invoice** | `id` (InvoiceId)<br>`sessionId` (SessionId)<br>`customerId` (UserId)<br>`vendorId` (UserId)<br>`status` (InvoiceStatus)<br>`lineItems` (List)<br>`totalAmount` (Money)<br>`createdAt` (Instant) | Represents a finalized invoice for a charging session. Must have at least one line item. Total amount is the sum of line items. |
| **BillingAccount** | `id` (BillingAccountId)<br>`customerId` (UserId)<br>`balance` (Money)<br>`totalSpent` (Money)<br>`lastBilledAt` (Instant) | Tracks the financial standing of a customer. Accumulates total spent and tracks outstanding balance. |

## Value Objects

| Value Object | Properties | Constraints |
|--------------|------------|-------------|
| **InvoiceId** | `value` (UUID) | Unique identifier for an invoice. Cannot be null. |
| **BillingAccountId** | `value` (UUID) | Unique identifier for a billing account. Cannot be null. |
| **InvoiceLineItem** | `description` (String)<br>`unitPrice` (Money)<br>`quantity` (BigDecimal)<br>`totalAmount` (Money) | Describes a specific charge (e.g., energy used or markup fee). Prices and quantities must be positive. `totalAmount` must equal `unitPrice` * `quantity`. |
| **TariffRate** | `baseRate` (Money)<br>`markupRate` (Money)<br>`totalRate` (Money) | Immutable representation of the rate active at session start: Base rate + Markup rate. |

## Aggregates

| Aggregate Root | Members | Invariants |
|----------------|---------|------------|
| **Invoice** | `InvoiceLineItem` | - Line items list cannot be empty.<br>- `totalAmount` must match the sum of all line items.<br>- Cannot be modified once paid or voided. |
| **BillingAccount** | None | - `totalSpent` must be non-negative.<br>- Track customer balance correctly when charging invoices or receiving payments. |

## Domain Events

| Event | Trigger | Payload |
|-------|---------|---------|
| **InvoiceGeneratedEvent** | Invoice is created and finalized. | `invoiceId` (InvoiceId), `sessionId` (SessionId), `customerId` (UserId), `vendorId` (UserId), `totalAmount` (Money), `createdAt` (Instant) |
| **BillingAccountUpdatedEvent** | Billing account balance or total spent changes. | `accountId` (BillingAccountId), `customerId` (UserId), `newBalance` (Money), `newTotalSpent` (Money) |

## Domain Services

| Service | Operations | Dependencies |
|---------|------------|--------------|
| **CostCalculator** | `calculateCost(energyKwh, baseRate, markup)`: TariffRate | None (Pure business calculation service based on formula) |
| **InvoiceGenerator** | `generateInvoice(sessionCompletedEvent)`: Invoice | `CostCalculator` |

## Repository Interfaces

| Repository | Entity | Methods |
|------------|--------|---------|
| **InvoiceRepository** | `Invoice` | `save(Invoice)`<br>`findById(InvoiceId)`<br>`findBySessionId(SessionId)`<br>`findByVendorIdAndCreatedAtBetween(UserId, Instant, Instant)`<br>`findAllByCreatedAtBetween(Instant, Instant)` |
| **BillingAccountRepository** | `BillingAccount` | `save(BillingAccount)`<br>`findByCustomerId(UserId)` |

## Ubiquitous Language

| Term | Definition |
|------|------------|
| **Invoice** | A financial document detailing charging fees for a completed session. |
| **Invoice Line Item** | An individual charge on an invoice, e.g., Base Charging Fee or Admin Markup Fee. |
| **Billing Account** | The customer profile tracking overall balance and platform spending. |
| **Base Rate** | The charging rate per kWh configured by the vendor (excluding markup). |
| **Markup Rate** | The additional platform fee per kWh added by the platform administrator. |
| **Total Rate** | The combined charging rate per kWh billed to the customer (Base Rate + Markup Rate). |
| **Income Report** | Aggregated reports of revenue and session counts for administrators or vendors. |
