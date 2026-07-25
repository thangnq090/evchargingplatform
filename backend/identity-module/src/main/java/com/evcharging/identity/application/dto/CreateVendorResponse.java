package com.evcharging.identity.application.dto;

import java.util.UUID;

/** Response DTO returned after a vendor is created. Contains the invitation details. */
public record CreateVendorResponse(
    UUID vendorId,
    String vendorName,
    UUID invitationId,
    String invitationToken,
    String invitedEmail) {}
