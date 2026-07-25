package com.evcharging.identity.application.dto;

import java.time.Instant;
import java.util.UUID;

import com.evcharging.identity.domain.model.Role;
import com.evcharging.identity.domain.model.User;

public record UserSummaryResponse(
    UUID id,
    String name,
    String email,
    Role role,
    UUID vendorId,
    String status,
    boolean mustChangePassword,
    Instant createdAt) {

  public static UserSummaryResponse from(User user) {
    return new UserSummaryResponse(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getRole(),
        user.getVendorId(),
        user.getStatus().name(),
        user.isMustChangePassword(),
        user.getCreatedAt());
  }
}
