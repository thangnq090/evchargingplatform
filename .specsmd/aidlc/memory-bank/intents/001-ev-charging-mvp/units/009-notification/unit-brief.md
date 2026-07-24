---
unit: 009-notification
intent: 001-ev-charging-mvp
phase: inception
status: draft
created: "2026-07-24T15:00:00Z"
updated: "2026-07-24T15:00:00Z"
---

# Unit Brief: Notification Service

## Purpose
Handle multi-channel notification delivery. For MVP, all notifications are logged to console only. Channel abstraction is designed to support future Email, SMS, and Push Notification adapters.

## Scope

### In Scope
- Notification domain event consumption
- Console logger adapter (prints notification payload)
- Notification event types: session start, session complete, payment succeeded, payment failed
- Notification channel abstraction (for future adapters)
- Correlation ID propagation in notifications

### Out of Scope
- Email delivery (deferred)
- SMS delivery (deferred)
- Push notification (deferred)
- User notification preferences (deferred)

---

## Assigned Requirements

| FR | Requirement | Priority |
|----|-------------|----------|
| FR-18 | Console Log Notifications | Could |

---

## Dependencies

### Depends On
| Unit | Reason |
|------|--------|
| `003-session-management` | Session events |
| `005-payment-processing` | Payment events |

---

## Technical Context

### Notification Channel Abstraction
```java
public interface NotificationChannel {
    boolean supports(NotificationType type);
    void send(Notification notification);
}

public class ConsoleNotificationChannel implements NotificationChannel { ... }
// Future: EmailNotificationChannel, SmsNotificationChannel, PushNotificationChannel
```

---

## Bolt Suggestions

| Bolt | Type | Stories | Objective |
|------|------|---------|-----------|
| bolt-009-notification-1 | Simple | S1 | Channel abstraction, console adapter, event consumption |
