package com.evcharging.identity.domain.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("User Domain Tests")
class UserTest {

  @Nested
  @DisplayName("createAdmin")
  class CreateAdmin {

    @Test
    @DisplayName("creates admin with ACTIVE status and no vendor")
    void shouldCreateAdminWithActiveStatus() {
      User user = User.createAdmin("Alice", "Alice@Example.COM", "hash");

      assertThat(user.getId()).isNotNull();
      assertThat(user.getEmail()).isEqualTo("alice@example.com"); // lowercased
      assertThat(user.getName()).isEqualTo("Alice");
      assertThat(user.getRole()).isEqualTo(Role.ADMIN);
      assertThat(user.getVendorId()).isNull();
      assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
      assertThat(user.getCreatedAt()).isNotNull();
    }
  }

  @Nested
  @DisplayName("createCustomer")
  class CreateCustomer {

    @Test
    @DisplayName("creates CUSTOMER with auto-generated account number and phone")
    void shouldCreateCustomerWithAccountNumber() {
      User customer =
          User.createCustomer(
              "Charlie", "Charlie@Example.com", "hash", "+1234567890", "ACC-12345678");

      assertThat(customer.getId()).isNotNull();
      assertThat(customer.getEmail()).isEqualTo("charlie@example.com");
      assertThat(customer.getPhone()).isEqualTo("+1234567890");
      assertThat(customer.getAccountNumber()).isEqualTo("ACC-12345678");
      assertThat(customer.getRole()).isEqualTo(Role.CUSTOMER);
      assertThat(customer.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("rejects null accountNumber")
    void shouldRejectNullAccountNumber() {
      assertThatThrownBy(
              () ->
                  User.createCustomer(
                      "Charlie", "charlie@example.com", "hash", "+1234567890", null))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("createVendorUser")
  class CreateVendorUser {

    @Test
    @DisplayName("creates VENDOR_ADMIN successfully")
    void shouldCreateVendorAdmin() {
      var vendorId = java.util.UUID.randomUUID();
      User user =
          User.createVendorUser("Bob", "bob@example.com", "hash", Role.VENDOR_ADMIN, vendorId);

      assertThat(user.getRole()).isEqualTo(Role.VENDOR_ADMIN);
      assertThat(user.getVendorId()).isEqualTo(vendorId);
      assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("rejects ADMIN role")
    void shouldRejectAdminRole() {
      var vendorId = java.util.UUID.randomUUID();
      assertThatThrownBy(() -> User.createVendorUser("x", "x@x.com", "hash", Role.ADMIN, vendorId))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rejects null vendorId")
    void shouldRejectNullVendorId() {
      assertThatThrownBy(
              () -> User.createVendorUser("x", "x@x.com", "hash", Role.VENDOR_ADMIN, null))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("suspend and activate")
  class StatusTransitions {

    @Test
    @DisplayName("suspends an ACTIVE user")
    void shouldSuspendActiveUser() {
      User user = User.createAdmin("Alice", "alice@example.com", "hash");
      user.suspend();
      assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
    }

    @Test
    @DisplayName("throws when suspending a non-ACTIVE user")
    void shouldThrowWhenSuspendingAlreadySuspended() {
      User user = User.createAdmin("Alice", "alice@example.com", "hash");
      user.suspend();
      assertThatThrownBy(user::suspend).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("reactivates a SUSPENDED user")
    void shouldActivateSuspendedUser() {
      User user = User.createAdmin("Alice", "alice@example.com", "hash");
      user.suspend();
      user.activate();
      assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
  }
}
