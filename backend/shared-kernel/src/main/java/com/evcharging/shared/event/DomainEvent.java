package com.evcharging.shared.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all domain events in the EV Charging Platform.
 *
 * <p>Domain events represent something that happened in the domain that other parts of the system
 * may be interested in. They are immutable and contain only primitive data (no entity references).
 *
 * <p>Naming convention: Past tense, e.g., {@code SessionStartedEvent}, {@code PaymentCapturedEvent}
 */
public interface DomainEvent {

  /** Unique identifier for this event instance. Used for idempotency and deduplication. */
  UUID getEventId();

  /** Timestamp when the event occurred (UTC). */
  Instant getOccurredAt();

  /** Event type name (typically the simple class name). Used for routing and serialization. */
  String getEventType();

  /**
   * Correlation ID linking related events across modules. Propagated from the originating
   * command/request.
   */
  String getCorrelationId();

  /**
   * Causation ID linking to the event that caused this event. Useful for event sourcing and
   * debugging.
   */
  String getCausationId();

  /** Default implementation providing common event metadata. */
  abstract class AbstractDomainEvent implements DomainEvent {

    private final UUID eventId;
    private final Instant occurredAt;
    private final String correlationId;
    private final String causationId;

    protected AbstractDomainEvent() {
      this(UUID.randomUUID(), Instant.now(), null, null);
    }

    protected AbstractDomainEvent(String correlationId) {
      this(UUID.randomUUID(), Instant.now(), correlationId, null);
    }

    protected AbstractDomainEvent(String correlationId, String causationId) {
      this(UUID.randomUUID(), Instant.now(), correlationId, causationId);
    }

    protected AbstractDomainEvent(
        UUID eventId, Instant occurredAt, String correlationId, String causationId) {
      this.eventId = eventId;
      this.occurredAt = occurredAt;
      this.correlationId = correlationId;
      this.causationId = causationId;
    }

    @Override
    public UUID getEventId() {
      return eventId;
    }

    @Override
    public Instant getOccurredAt() {
      return occurredAt;
    }

    @Override
    public String getEventType() {
      return getClass().getSimpleName();
    }

    @Override
    public String getCorrelationId() {
      return correlationId;
    }

    @Override
    public String getCausationId() {
      return causationId;
    }

    @Override
    public String toString() {
      return String.format(
          "%s{eventId=%s, occurredAt=%s, correlationId=%s, causationId=%s}",
          getEventType(), eventId, occurredAt, correlationId, causationId);
    }
  }
}
