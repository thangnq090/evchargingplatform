package com.evcharging.identity.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Published when a new platform administrator successfully registers. */
public record AdminRegisteredEvent(UUID userId, String email, String name, Instant timestamp) {}
