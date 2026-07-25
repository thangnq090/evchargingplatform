package com.evcharging.identity.application.port.out;

import com.evcharging.identity.application.dto.LoginResponse;
import com.evcharging.identity.domain.model.User;

/**
 * Output port — issues a signed JWT access token for an authenticated user.
 *
 * <p>The application layer depends only on this interface. The infrastructure layer provides the
 * concrete implementation ({@code JwtIssuerService}).
 */
public interface TokenIssuerPort {

  /**
   * Issue a JWT access token for the given user.
   *
   * @param user the authenticated domain user
   * @return a {@link LoginResponse} containing the signed token and metadata
   */
  LoginResponse issue(User user);
}
