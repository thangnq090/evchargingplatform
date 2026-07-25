package com.evcharging.identity.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Invitation Domain Tests")
class InvitationTest {

  @Nested
  @DisplayName("create")
  class Create {

    @Test
    @DisplayName("creates PENDING invitation with lowercase email")
    void shouldCreatePendingInvitation() {
      UUID vendorId = UUID.randomUUID();
      Invitation invitation =
          Invitation.create(
              "ADMIN@Vendor.COM",
              vendorId,
              Role.VENDOR_ADMIN,
              "tok",
              Instant.now().plus(48, ChronoUnit.HOURS));

      assertThat(invitation.getEmail()).isEqualTo("admin@vendor.com");
      assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.PENDING);
      assertThat(invitation.isValid()).isTrue();
    }

    @Test
    @DisplayName("rejects ADMIN role")
    void shouldRejectAdminRole() {
      assertThatThrownBy(
              () ->
                  Invitation.create(
                      "a@b.com",
                      UUID.randomUUID(),
                      Role.ADMIN,
                      "tok",
                      Instant.now().plus(1, ChronoUnit.HOURS)))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("accept")
  class Accept {

    @Test
    @DisplayName("transitions PENDING to ACCEPTED")
    void shouldAcceptPendingInvitation() {
      Invitation invitation =
          Invitation.create(
              "a@b.com",
              UUID.randomUUID(),
              Role.VENDOR_ADMIN,
              "tok",
              Instant.now().plus(1, ChronoUnit.HOURS));

      invitation.accept();
      assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
    }

    @Test
    @DisplayName("throws when already accepted")
    void shouldThrowOnDoubleAccept() {
      Invitation invitation =
          Invitation.create(
              "a@b.com",
              UUID.randomUUID(),
              Role.VENDOR_ADMIN,
              "tok",
              Instant.now().plus(1, ChronoUnit.HOURS));
      invitation.accept();

      assertThatThrownBy(invitation::accept).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("throws when expired")
    void shouldThrowWhenExpired() {
      Invitation invitation =
          Invitation.create(
              "a@b.com",
              UUID.randomUUID(),
              Role.VENDOR_ADMIN,
              "tok",
              Instant.now().minus(1, ChronoUnit.HOURS)); // already in the past

      assertThatThrownBy(invitation::accept).isInstanceOf(IllegalStateException.class);
    }
  }
}
