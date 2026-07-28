package com.evcharging.identity.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.identity.domain.model.User;
import com.evcharging.identity.domain.model.UserStatus;
import com.evcharging.identity.domain.model.Role;

@DisplayName("UserDbEntity")
class UserDbEntityTest {

  private static final UUID USER_UUID = UUID.randomUUID();

  private User createUser() {
    return User.reconstitute(
        USER_UUID, "Test User", "test@test.com", "$2a$hash", "1234567890",
        Role.CUSTOMER, UUID.randomUUID(), "CUST-001", UserStatus.ACTIVE, false,
        Instant.now(), Instant.now());
  }

  @Nested
  @DisplayName("from")
  class From {

    @Test
    @DisplayName("converts domain user to entity and back")
    void shouldRoundTrip() {
      User user = createUser();

      UserDbEntity entity = UserDbEntity.from(user, true);
      User domain = entity.toDomain();

      assertThat(domain.getId()).isEqualTo(USER_UUID);
      assertThat(domain.getName()).isEqualTo("Test User");
      assertThat(domain.getEmail()).isEqualTo("test@test.com");
      assertThat(domain.getPhone()).isEqualTo("1234567890");
      assertThat(domain.getRole()).isEqualTo(Role.CUSTOMER);
      assertThat(domain.getStatus()).isEqualTo(UserStatus.ACTIVE);
      assertThat(domain.isMustChangePassword()).isFalse();
    }

    @Test
    @DisplayName("marks as new")
    void shouldMarkAsNew() {
      UserDbEntity entity = UserDbEntity.from(createUser(), true);
      assertThat(entity.isNew()).isTrue();
    }

    @Test
    @DisplayName("marks as not new")
    void shouldMarkAsNotNew() {
      UserDbEntity entity = UserDbEntity.from(createUser(), false);
      assertThat(entity.isNew()).isFalse();
    }
  }

  @Nested
  @DisplayName("toDomain")
  class ToDomain {

    @Test
    @DisplayName("preserves mustChangePassword")
    void shouldPreserveMustChangePassword() {
      User user = User.reconstitute(
          UUID.randomUUID(), "Admin", "admin@test.com", "$2a$hash", null,
          Role.ADMIN, null, null, UserStatus.ACTIVE, true,
          Instant.now(), Instant.now());

      UserDbEntity entity = UserDbEntity.from(user, true);
      User domain = entity.toDomain();

      assertThat(domain.isMustChangePassword()).isTrue();
    }

    @Test
    @DisplayName("preserves vendorId and accountNumber")
    void shouldPreserveVendorId() {
      UUID vendorId = UUID.randomUUID();
      User user = User.reconstitute(
          UUID.randomUUID(), "Vendor Admin", "va@test.com", "$2a$hash", null,
          Role.VENDOR_ADMIN, vendorId, "VEN-001", UserStatus.ACTIVE, false,
          Instant.now(), Instant.now());

      User domain = UserDbEntity.from(user, true).toDomain();

      assertThat(domain.getVendorId()).isEqualTo(vendorId);
      assertThat(domain.getAccountNumber()).isEqualTo("VEN-001");
    }
  }
}
