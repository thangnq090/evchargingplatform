package com.evcharging.identity.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Published when an invitation is issued to a prospective vendor admin. */
public record VendorInvitationIssuedEvent(
    UUID invitationId, UUID vendorId, String email, String token, Instant expiresAt) {}
