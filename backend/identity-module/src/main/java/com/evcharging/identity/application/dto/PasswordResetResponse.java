package com.evcharging.identity.application.dto;

import java.util.UUID;

public record PasswordResetResponse(
    UUID userId, String temporaryPassword, boolean mustChangePassword, String message) {}
