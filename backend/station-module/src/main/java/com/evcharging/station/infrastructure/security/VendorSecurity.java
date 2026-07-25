package com.evcharging.station.infrastructure.security;

import java.util.UUID;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

/** Security component for vendor-specific access checks in SpEL expressions. */
@Component("vendorSecurity")
public class VendorSecurity {

  /**
   * Checks if the currently authenticated user is a platform admin or a vendor admin owning the
   * specified vendor.
   *
   * @param vendorId vendor ID to check
   * @return Mono emitting true if authorized, false otherwise
   */
  public Mono<Boolean> checkAccess(UUID vendorId) {
    return org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
        .map(ctx -> ctx.getAuthentication())
        .map(
            auth -> {
              if (auth == null) {
                return false;
              }

              // Check if platform admin
              boolean isAdmin =
                  auth.getAuthorities().stream()
                      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
              if (isAdmin) {
                return true;
              }

              // Check if vendor admin and owns the vendor
              boolean isVendorAdmin =
                  auth.getAuthorities().stream()
                      .anyMatch(a -> a.getAuthority().equals("ROLE_VENDOR_ADMIN"));
              if (isVendorAdmin && auth instanceof JwtAuthenticationToken jwtAuth) {
                Object vId = jwtAuth.getToken().getClaim("vendor_id");
                if (vId instanceof String s) {
                  return UUID.fromString(s).equals(vendorId);
                }
              }
              return false;
            })
        .defaultIfEmpty(false);
  }
}
