package com.evcharging.identity.domain.event;

import com.evcharging.identity.domain.model.Role;
import java.util.UUID;

/** Published when an invited user accepts their invitation and registers. */
public record VendorInvitationAcceptedEvent(
    UUID invitationId, UUID userId, String email, UUID vendorId, Role role) {}
