package com.evcharging.identity.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for {@code POST /api/v1/identity/auth/login}.
 *
 * @param email the user's email address
 * @param password the user's raw password (compared against BCrypt hash)
 */
public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
