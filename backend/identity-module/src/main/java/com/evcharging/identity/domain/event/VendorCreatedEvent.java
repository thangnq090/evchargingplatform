package com.evcharging.identity.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Published when a new Vendor is created by an administrator. */
public record VendorCreatedEvent(UUID vendorId, String name, Instant timestamp) {}
