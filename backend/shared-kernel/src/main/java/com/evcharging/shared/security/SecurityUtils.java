package com.evcharging.shared.security;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

/**
 * Utility class for security-related operations. Provides methods for extracting user information from
 * JWT tokens in both synchronous and reactive contexts.
 */
public final class SecurityUtils {

  private SecurityUtils() {
    throw new UnsupportedOperationException("Utility class");
  }

  /** Gets the current authenticated user's ID from the security context (Sync). */
  public static Optional<UUID> getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return extractUserIdFromAuth(authentication);
  }

  /** Gets the current user's email from the JWT token (Sync). */
  public static Optional<String> getCurrentUserEmail() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return extractEmailFromAuth(authentication);
  }

  /** Gets the current user's roles from the JWT token (Sync). */
  public static List<String> getCurrentUserRoles() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return extractRolesFromAuth(authentication);
  }

  /** Checks if the current user has a specific role (Sync). */
  public static boolean hasRole(String role) {
    return getCurrentUserRoles().contains(role);
  }

  /** Extracts the JWT token from the current authentication (Sync). */
  public static Optional<Jwt> getCurrentJwt() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
      return Optional.of(jwtAuth.getToken());
    }
    return Optional.empty();
  }

  /** Gets a custom claim from the current JWT token (Sync). */
  @SuppressWarnings("unchecked")
  public static <T> Optional<T> getClaim(String claimName) {
    return getCurrentJwt().map(jwt -> (T) jwt.getClaim(claimName));
  }

  /** Extracts the vendor ID from the current JWT token (Sync). */
  public static Optional<UUID> getCurrentVendorId() {
    return getClaim("vendor_id").map(o -> UUID.fromString((String) o));
  }

  /** Reactive helper: Gets the current authenticated user's ID from ReactiveSecurityContextHolder. */
  public static Mono<UUID> getReactiveUserId() {
    return org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
        .map(ctx -> ctx.getAuthentication())
        .flatMap(auth -> Mono.justOrEmpty(extractUserIdFromAuth(auth)));
  }

  /** Reactive helper: Gets the current vendor ID from ReactiveSecurityContextHolder. */
  public static Mono<UUID> getReactiveVendorId() {
    return org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
        .map(ctx -> ctx.getAuthentication())
        .flatMap(
            auth -> {
              if (auth instanceof JwtAuthenticationToken jwtAuth) {
                Object vId = jwtAuth.getToken().getClaim("vendor_id");
                if (vId instanceof String s) {
                  return Mono.just(UUID.fromString(s));
                }
              }
              return Mono.empty();
            });
  }

  private static Optional<UUID> extractUserIdFromAuth(Authentication authentication) {
    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
      Jwt jwt = jwtAuth.getToken();
      String sub = jwt.getSubject();
      if (sub != null) {
        try {
          return Optional.of(UUID.fromString(sub));
        } catch (IllegalArgumentException e) {
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  private static Optional<String> extractEmailFromAuth(Authentication authentication) {
    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
      Jwt jwt = jwtAuth.getToken();
      return Optional.ofNullable(jwt.getClaimAsString("email"));
    }
    return Optional.empty();
  }

  private static List<String> extractRolesFromAuth(Authentication authentication) {
    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
      Jwt jwt = jwtAuth.getToken();
      Object realmAccess = jwt.getClaim("realm_access");
      if (realmAccess instanceof java.util.Map<?, ?> map) {
        Object roles = map.get("roles");
        if (roles instanceof Collection<?> collection) {
          return collection.stream()
              .filter(String.class::isInstance)
              .map(String.class::cast)
              .toList();
        }
      }
    }
    return List.of();
  }
}
