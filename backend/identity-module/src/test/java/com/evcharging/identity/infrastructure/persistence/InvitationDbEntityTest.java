package com.evcharging.identity.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.identity.domain.model.Invitation;
import com.evcharging.identity.domain.model.InvitationStatus;
import com.evcharging.identity.domain.model.Role;

@DisplayName("InvitationDbEntity")
class InvitationDbEntityTest {

  private Invitation createInvitation() {
    return Invitation.create(
        "invite@test.com", UUID.randomUUID(), Role.VENDOR_USER,
        "token123", Instant.now().plusSeconds(86400));
  }

  @Nested
  @DisplayName("from")
  class From {

    @Test
    @DisplayName("round-trips domain to entity and back")
    void shouldRoundTrip() {
      Invitation invitation = createInvitation();

      InvitationDbEntity entity = InvitationDbEntity.from(invitation, true);
      Invitation domain = entity.toDomain();

      assertThat(domain.getId()).isEqualTo(invitation.getId());
      assertThat(domain.getEmail()).isEqualTo("invite@test.com");
      assertThat(domain.getVendorId()).isEqualTo(invitation.getVendorId());
      assertThat(domain.getRole()).isEqualTo(Role.VENDOR_USER);
      assertThat(domain.getToken()).isEqualTo("token123");
      assertThat(domain.getStatus()).isEqualTo(InvitationStatus.PENDING);
      assertThat(entity.isNew()).isTrue();
    }

    @Test
    @DisplayName("marks as not new")
    void shouldMarkAsNotNew() {
      InvitationDbEntity entity = InvitationDbEntity.from(createInvitation(), false);
      assertThat(entity.isNew()).isFalse();
    }
  }
}
