package com.evcharging.shared.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Converts JWT tokens to Spring Security Authentication tokens across platform modules.
 *
 * <p>Extracts roles and permissions from JWT claims:
 * <ul>
 *   <li>{@code realm_access.roles}
 *   <li>{@code resource_access.{client}.roles}
 *   <li>Top-level {@code role} and {@code roles}
 *   <li>Custom claims: {@code permissions}, {@code vendor_id}
 * </ul>
 */
@Component
public class PlatformJwtAuthenticationConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {

  private static final String REALM_ACCESS = "realm_access";
  private static final String RESOURCE_ACCESS = "resource_access";
  private static final String ROLES = "roles";
  private static final String PERMISSIONS = "permissions";
  private static final String VENDOR_ID = "vendor_id";

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
    return new JwtAuthenticationToken(jwt, authorities);
  }

  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    // Extract realm roles
    List<GrantedAuthority> realmRoles = extractRealmRoles(jwt);

    // Extract resource (client) roles
    List<GrantedAuthority> resourceRoles = extractResourceRoles(jwt);

    // Extract custom permissions claim
    List<GrantedAuthority> permissions = extractPermissions(jwt);

    // Extract vendor ID for multi-tenancy
    List<GrantedAuthority> vendorAuthorities = extractVendorAuthorities(jwt);

    return Stream.of(realmRoles, resourceRoles, permissions, vendorAuthorities)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  private List<GrantedAuthority> extractRealmRoles(Jwt jwt) {
    List<String> roles = new java.util.ArrayList<>();

    // 1. Check realm_access.roles
    Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS);
    if (realmAccess != null && realmAccess.containsKey(ROLES)) {
      @SuppressWarnings("unchecked")
      List<String> r = (List<String>) realmAccess.get(ROLES);
      roles.addAll(r);
    }

    // 2. Check top-level role string
    String singleRole = jwt.getClaim("role");
    if (singleRole != null) {
      roles.add(singleRole.replaceFirst("^ROLE_", ""));
    }

    // 3. Check top-level roles list
    List<String> topRoles = jwt.getClaim("roles");
    if (topRoles != null) {
      topRoles.forEach(r -> roles.add(r.replaceFirst("^ROLE_", "")));
    }

    return roles.stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
        .collect(Collectors.toList());
  }

  private List<GrantedAuthority> extractResourceRoles(Jwt jwt) {
    Map<String, Object> resourceAccess = jwt.getClaim(RESOURCE_ACCESS);
    if (resourceAccess == null) {
      return List.of();
    }
    return resourceAccess.values().stream()
        .filter(Map.class::isInstance)
        .map(Map.class::cast)
        .filter(map -> map.containsKey(ROLES))
        .flatMap(
            map -> {
              @SuppressWarnings("unchecked")
              List<String> roles = (List<String>) map.get(ROLES);
              return roles.stream();
            })
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
        .collect(Collectors.toList());
  }

  private List<GrantedAuthority> extractPermissions(Jwt jwt) {
    Object permissionsClaim = jwt.getClaim(PERMISSIONS);
    if (permissionsClaim instanceof List<?> list) {
      return list.stream()
          .filter(String.class::isInstance)
          .map(String.class::cast)
          .map(perm -> new SimpleGrantedAuthority("PERM_" + perm.toUpperCase()))
          .collect(Collectors.toList());
    }
    return List.of();
  }

  private List<GrantedAuthority> extractVendorAuthorities(Jwt jwt) {
    Object vendorIdClaim = jwt.getClaim(VENDOR_ID);
    if (vendorIdClaim instanceof String vendorId) {
      return List.of(new SimpleGrantedAuthority("VENDOR_" + vendorId));
    }
    return List.of();
  }
}
