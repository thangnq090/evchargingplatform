package com.evcharging.identity.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Domain event emitted when a new customer registers. */
public record CustomerRegisteredEvent(
    UUID userId, String email, String name, String accountNumber, Instant occurredOn) {}
