package com.evcharging.shared.security;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Utility class for security-related operations. Provides common methods for extracting user
 * information from JWT tokens and checking permissions.
 */
public final class SecurityUtils {

  private SecurityUtils() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Gets the current authenticated user's ID from the security context.
   *
   * @return Optional containing the user ID if authenticated
   */
  public static Optional<UUID> getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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

  /**
   * Gets the current user's email from the JWT token.
   *
   * @return Optional containing the email if present
   */
  public static Optional<String> getCurrentUserEmail() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
      Jwt jwt = jwtAuth.getToken();
      return Optional.ofNullable(jwt.getClaimAsString("email"));
    }
    return Optional.empty();
  }

  /**
   * Gets the current user's roles from the JWT token.
   *
   * @return List of roles (empty if not authenticated or no roles claim)
   */
  public static List<String> getCurrentUserRoles() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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

  /**
   * Checks if the current user has a specific role.
   *
   * @param role the role to check
   * @return true if user has the role
   */
  public static boolean hasRole(String role) {
    return getCurrentUserRoles().contains(role);
  }

  /**
   * Checks if the current user has any of the specified roles.
   *
   * @param roles the roles to check
   * @return true if user has at least one of the roles
   */
  public static boolean hasAnyRole(String... roles) {
    List<String> userRoles = getCurrentUserRoles();
    for (String role : roles) {
      if (userRoles.contains(role)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Extracts the JWT token from the current authentication.
   *
   * @return Optional containing the JWT if authenticated via JWT
   */
  public static Optional<Jwt> getCurrentJwt() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
      return Optional.of(jwtAuth.getToken());
    }
    return Optional.empty();
  }

  /**
   * Gets a custom claim from the current JWT token.
   *
   * @param claimName the name of the claim
   * @param <T> the expected type
   * @return Optional containing the claim value if present
   */
  @SuppressWarnings("unchecked")
  public static <T> Optional<T> getClaim(String claimName) {
    return getCurrentJwt().map(jwt -> (T) jwt.getClaim(claimName));
  }

  /**
   * Extracts the vendor ID from the current JWT token (for multi-tenancy).
   *
   * @return Optional containing the vendor ID if present
   */
  public static Optional<UUID> getCurrentVendorId() {
    return getClaim("vendor_id").map(o -> UUID.fromString((String) o));
  }

  /**
   * Extracts the correlation ID from the request or generates a new one.
   *
   * @param request the HTTP request
   * @return correlation ID
   */
  public static String getOrCreateCorrelationId(HttpServletRequest request) {
    String correlationId = request.getHeader("X-Correlation-ID");
    if (correlationId == null || correlationId.isBlank()) {
      correlationId = UUID.randomUUID().toString();
    }
    return correlationId;
  }

  /**
   * Checks if the current authentication is authenticated and not anonymous.
   *
   * @return true if authenticated
   */
  public static boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null
        && authentication.isAuthenticated()
        && !"anonymousUser".equals(authentication.getPrincipal());
  }
}
