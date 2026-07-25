package com.evcharging.identity.domain.event;

import com.evcharging.identity.domain.model.Role;
import java.time.Instant;
import java.util.UUID;

/** Published when a VENDOR_ADMIN adds a new VENDOR_USER to their organisation. */
public record VendorUserCreatedEvent(
    UUID userId, UUID vendorId, String email, Role role, Instant timestamp) {}
