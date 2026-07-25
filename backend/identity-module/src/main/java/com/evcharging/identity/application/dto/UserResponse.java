package com.evcharging.identity.application.dto;

import java.time.Instant;
import java.util.UUID;

import com.evcharging.identity.domain.model.Role;
import com.evcharging.identity.domain.model.User;
import com.evcharging.identity.domain.model.UserStatus;

/** Response DTO representing a user returned from registration or lookup. */
public record UserResponse(
    UUID id,
    String name,
    String email,
    String phone,
    Role role,
    UUID vendorId,
    String accountNumber,
    UserStatus status,
    Instant createdAt) {

  /** Static factory method mapping a domain User to a UserResponse. */
  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getPhone(),
        user.getRole(),
        user.getVendorId(),
        user.getAccountNumber(),
        user.getStatus(),
        user.getCreatedAt());
  }
}
