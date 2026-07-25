package com.evcharging.identity.application.dto;

import com.evcharging.identity.domain.model.Role;
import com.evcharging.identity.domain.model.User;
import com.evcharging.identity.domain.model.UserStatus;
import java.time.Instant;
import java.util.UUID;

/** Response DTO representing a user returned from registration or lookup. */
public record UserResponse(
    UUID id,
    String name,
    String email,
    Role role,
    UUID vendorId,
    UserStatus status,
    Instant createdAt) {

  /** Static factory method mapping a domain User to a UserResponse. */
  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getRole(),
        user.getVendorId(),
        user.getStatus(),
        user.getCreatedAt());
  }
}
