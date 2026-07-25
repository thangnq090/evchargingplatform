package com.evcharging.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA interface for {@link InvitationDbEntity}. Package-private. */
interface SpringDataInvitationRepository extends JpaRepository<InvitationDbEntity, UUID> {

  Optional<InvitationDbEntity> findByToken(String token);
}
