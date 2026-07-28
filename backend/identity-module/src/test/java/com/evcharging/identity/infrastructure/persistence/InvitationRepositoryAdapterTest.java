package com.evcharging.identity.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evcharging.identity.domain.model.Invitation;
import com.evcharging.identity.domain.model.Role;

@DisplayName("InvitationRepositoryAdapter")
@ExtendWith(MockitoExtension.class)
class InvitationRepositoryAdapterTest {

  @Mock private SpringDataInvitationRepository jpa;

  private InvitationRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new InvitationRepositoryAdapter(jpa);
  }

  private Invitation createInvitation() {
    return Invitation.create(
        "invite@test.com", UUID.randomUUID(), Role.VENDOR_USER,
        "token123", Instant.now().plusSeconds(86400));
  }

  private InvitationDbEntity createEntity() {
    return InvitationDbEntity.from(createInvitation(), true);
  }

  @Nested
  @DisplayName("save")
  class Save {

    @Test
    @DisplayName("saves new invitation")
    void shouldSaveNewInvitation() {
      Invitation invitation = createInvitation();
      given(jpa.existsById(invitation.getId())).willReturn(false);
      given(jpa.save(any(InvitationDbEntity.class))).willAnswer(inv -> inv.getArgument(0));

      Invitation result = adapter.save(invitation);

      assertThat(result).isNotNull();
      then(jpa).should().save(any(InvitationDbEntity.class));
    }

    @Test
    @DisplayName("updates existing invitation")
    void shouldUpdateExistingInvitation() {
      Invitation invitation = createInvitation();
      given(jpa.existsById(invitation.getId())).willReturn(true);
      given(jpa.save(any(InvitationDbEntity.class))).willAnswer(inv -> inv.getArgument(0));

      Invitation result = adapter.save(invitation);

      assertThat(result).isNotNull();
    }
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("returns invitation when found")
    void shouldReturnInvitation() {
      UUID id = UUID.randomUUID();
      given(jpa.findById(id)).willReturn(Optional.of(createEntity()));

      Optional<Invitation> result = adapter.findById(id);
      assertThat(result).isPresent();
    }

    @Test
    @DisplayName("returns empty when not found")
    void shouldReturnEmpty() {
      UUID id = UUID.randomUUID();
      given(jpa.findById(id)).willReturn(Optional.empty());

      assertThat(adapter.findById(id)).isEmpty();
    }
  }

  @Nested
  @DisplayName("findByToken")
  class FindByToken {

    @Test
    @DisplayName("returns invitation by token")
    void shouldReturnByToken() {
      given(jpa.findByToken("token123")).willReturn(Optional.of(createEntity()));

      Optional<Invitation> result = adapter.findByToken("token123");
      assertThat(result).isPresent();
    }

    @Test
    @DisplayName("returns empty when token not found")
    void shouldReturnEmpty() {
      given(jpa.findByToken("unknown")).willReturn(Optional.empty());

      assertThat(adapter.findByToken("unknown")).isEmpty();
    }
  }
}
