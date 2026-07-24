# Story: Console Log Notification Channel

## User Story
As a **Developer**
I want to **log notification events to console with correlation IDs**
So that **notifications can be observed during development and tested before integrating real channels**

## Acceptance Criteria
- [ ] Given a session start event, When published, Then a notification event is logged to console
- [ ] Given a payment success event, When published, Then a notification event is logged to console
- [ ] Given a payment failure event, When published, Then a notification event is logged to console
- [ ] Given any notification, When logged, Then correlation_id and session_id are included

## Technical Notes
- NotificationChannel interface for future adapters
- ConsoleNotificationChannel implements the interface
- Future: EmailNotificationChannel, SmsNotificationChannel, PushNotificationChannel

## Dependencies
- Story 003-001 (Session lifecycle)
- Story 005-002 (Payment orchestrator)
