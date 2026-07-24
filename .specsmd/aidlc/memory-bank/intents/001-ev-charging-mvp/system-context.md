---
intent: 001-ev-charging-mvp
phase: inception
status: context-defined
updated: "2026-07-24T15:00:00Z"
---

# EV Charging Platform MVP - System Context

## System Overview
A cloud-based modular monolith platform for managing EV charging operations. The platform connects administrators, vendors, customers, and charging devices to enable secure, reliable charging services. External integrations include payment providers, while notifications are console-logged for MVP.

## Actors

| Actor | Type | Description |
|-------|------|-------------|
| **Administrator** | Human | Platform operator managing vendors, viewing income, setting markup, resetting credentials |
| **Vendor** | Human | Chargepoint owner managing resources, viewing income/activity, generating reports |
| **Vendor User** | Human | Vendor employee managing vendor resources per assigned permissions (VENDOR_ADMIN / VENDOR_USER) |
| **Customer** | Human | End-user registering, performing charging sessions, managing vehicles, viewing history |
| **Charging Station (Device)** | System | Physical EV charger communicating via OCPP 1.6J over WebSocket |
| **Charging Station Operator** | Human | Physical installer/operator who registers and maintains the station hardware |

## External Systems

| System | Direction | Purpose | Protocol | MVP Stage |
|--------|-----------|---------|----------|-----------|
| **Payment Provider** | Outbound | Process payments (authorize, capture, refund, void) | REST / HTTPS | MockPayment (MVP); Stripe/Adyen later |
| **OCPP Chargers** | Bidirectional | Charger communication protocol (Start/Stop Transaction, MeterValues, Heartbeat) | WebSocket (OCPP 1.6J) | Full (MVP) |
| **Future: Notification Provider** | Outbound | Email/SMS/Push delivery | REST / SMTP | Console log only (MVP) |
| **Future: Search Engine** | Bidirectional | Advanced search and analytics | REST | PostgreSQL FTS (MVP); OpenSearch later |

## Context Diagram

```mermaid
C4Context
    title System Context - EV Charging Platform MVP

    Person(admin, "Administrator", "Platform operator managing vendors, income, markup")
    Person(vendorUser, "Vendor User", "VENDOR_ADMIN / VENDOR_USER managing chargepoints")
    Person(customer, "Customer", "End-user performing charging sessions")
    
    System_Boundary(platform, "EV Charging Platform") {
        System(gateway, "API Gateway", "Spring Cloud Gateway, JWT validation, rate limiting")
        System(identity, "Identity & Access", "User auth, registration, RBAC")
        System(station, "Station Management", "Chargepoint CRUD, availability")
        System(session, "Session Management", "Charging lifecycle, metering")
        System(vehicle, "Vehicle Management", "Vehicle registration, RFID, ownership")
        System(billing, "Pricing & Billing", "Tariffs, markup, cost calculation, invoicing")
        System(payment, "Payment Processing", "Payment orchestration, provider abstraction")
        System(notification, "Notification", "Console log (MVP)")
        System(gateway_ocpp, "Device Gateway", "OCPP 1.6J WebSocket, protocol translation")
    }
    
    System_Ext(charger, "OCPP Charger", "Physical EV charger (OCPP 1.6J)")
    System_Ext(payment_provider, "Payment Provider", "Stripe / Adyen (future); MockPayment (MVP)")
    System_Ext(search_engine, "Search Engine", "PostgreSQL FTS (MVP); OpenSearch (future)")
    
    Rel(admin, gateway, "Manages platform via REST API")
    Rel(vendorUser, gateway, "Manages chargepoints via REST API")
    Rel(customer, gateway, "Performs charging via REST API")
    Rel(gateway, identity, "Authenticates requests")
    Rel(gateway, station, "Routes station requests")
    Rel(gateway, session, "Routes session requests")
    Rel(gateway, vehicle, "Routes vehicle requests")
    Rel(gateway, billing, "Routes billing requests")
    
    Rel(charger, gateway_ocpp, "OCPP 1.6J WebSocket")
    Rel(gateway_ocpp, session, "Domain events: ChargingStarted, MeterValueReceived, ChargingStopped")
    
    Rel(payment, payment_provider, "Authorize, capture, refund, void")
    Rel(session, billing, "Domain events for cost calculation")
    Rel(billing, payment, "Domain events for payment orchestration")
    Rel(session, notification, "Domain events for session notifications")
    Rel(session, search_engine, "Index data for full-text search")
```

## External Integrations

| Integration | Protocol | Data Exchanged | Risk |
|-------------|----------|----------------|------|
| **OCPP 1.6J Chargers** | WebSocket (WSS) | Start/StopTransaction, MeterValues, Heartbeat, StatusNotification, Authorize | High — core domain reliability |
| **Payment Provider** | REST/HTTPS | Payment authorization, capture, refund, void | High — financial operations, PCI scope |
| **PostgreSQL (FTS)** | JDBC | Session, customer, vehicle data for search | Low — internal |
| **Future: Notification Provider** | REST/SMTP | Session events, payment confirmations, alerts | Medium — deferred |

## Data Flows

### Inbound
- **Admin/Vendor/Customer requests**: JSON over HTTPS → API Gateway → JWT validation → Module routing
- **OCPP WebSocket frames**: OCPP 1.6J messages → Device Gateway → Domain events → Domain modules
- **Future: Payment provider webhooks**: Payment status updates → Gateway → Payment Processing Module

### Outbound
- **Domain events**: In-process event bus (ApplicationEventPublisher) → Module event handlers
- **Payment provider calls**: Payment Processing Module → REST/HTTPS → External provider
- **Future: Notifications**: Notification Module → Console (MVP) → Email/SMS/Push later

## High-Level Constraints

- Modular monolith architecture (single deployable unit)
- Spring Boot 4 + Java 21
- PostgreSQL single instance, schema-per-module
- In-process domain events (no external message broker for MVP)
- Spring Cloud Gateway for JWT validation and routing
- Containerized deployment with Helm
- Single-region multi-AZ (no multi-region for MVP)

## Key NFR Goals

| Area | Target | Priority |
|------|--------|----------|
| **Performance** | API p95 < 200ms; Charging command < 2s | Must |
| **Availability** | 99.9% uptime | Must |
| **Recovery** | RTO < 30 min, RPO < 1 min | Must |
| **Security** | JWT (RS256/ES256), RBAC, TLS 1.3 | Must |
| **Idempotency** | All mutations idempotent via idempotency key | Must |
| **Audit** | Immutable audit log for all business events | Must |
| **Observability** | Correlation IDs across all modules | Must |
