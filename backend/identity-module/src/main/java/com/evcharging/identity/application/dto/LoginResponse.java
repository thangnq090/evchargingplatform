package com.evcharging.identity.application.dto;

import java.util.UUID;

/**
 * Response payload for {@code POST /api/v1/identity/auth/login}.
 *
 * @param accessToken JWT access token (HS256)
 * @param expiresIn token lifetime in seconds
 * @param userId the authenticated user's ID
 * @param role the authenticated user's role
 * @param vendorId the authenticated user's vendor ID (null for ADMIN)
 */
public record LoginResponse(
    String accessToken,
    long expiresIn,
    UUID userId,
    String role,
    UUID vendorId,
    String refreshToken,
    boolean mustChangePassword) {}
