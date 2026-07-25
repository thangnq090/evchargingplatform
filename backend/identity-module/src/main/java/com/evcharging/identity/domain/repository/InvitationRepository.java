package com.evcharging.identity.domain.repository;

import com.evcharging.identity.domain.model.Invitation;
import java.util.Optional;
import java.util.UUID;

/** Domain port — persistence contract for Invitation aggregates. */
public interface InvitationRepository {

  Invitation save(Invitation invitation);

  Optional<Invitation> findById(UUID id);

  Optional<Invitation> findByToken(String token);
}
