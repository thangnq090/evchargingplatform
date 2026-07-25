package com.evcharging.identity.infrastructure.persistence;

import com.evcharging.identity.domain.model.Invitation;
import com.evcharging.identity.domain.repository.InvitationRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Infrastructure adapter implementing the domain {@link InvitationRepository} port via Spring Data
 * JPA.
 */
@Repository
class InvitationRepositoryAdapter implements InvitationRepository {

  private final SpringDataInvitationRepository jpa;

  InvitationRepositoryAdapter(SpringDataInvitationRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Invitation save(Invitation invitation) {
    boolean isNew = !jpa.existsById(invitation.getId());
    InvitationDbEntity entity = InvitationDbEntity.from(invitation, isNew);
    InvitationDbEntity saved = jpa.save(entity);
    return saved.toDomain();
  }

  @Override
  public Optional<Invitation> findById(UUID id) {
    return jpa.findById(id).map(InvitationDbEntity::toDomain);
  }

  @Override
  public Optional<Invitation> findByToken(String token) {
    return jpa.findByToken(token).map(InvitationDbEntity::toDomain);
  }
}
