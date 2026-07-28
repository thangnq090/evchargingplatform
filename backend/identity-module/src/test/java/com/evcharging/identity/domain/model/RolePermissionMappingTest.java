package com.evcharging.identity.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RolePermissionMapping")
class RolePermissionMappingTest {

  @Nested
  @DisplayName("getPermissionsFor")
  class GetPermissionsFor {

    @Test
    @DisplayName("ADMIN has all permissions")
    void shouldReturnAllForAdmin() {
      Set<Permission> permissions = RolePermissionMapping.getPermissionsFor(Role.ADMIN);

      assertThat(permissions).containsExactlyInAnyOrder(Permission.values());
    }

    @Test
    @DisplayName("VENDOR_ADMIN has station, session, billing, user, vendor permissions")
    void shouldReturnVendorAdminPermissions() {
      Set<Permission> permissions = RolePermissionMapping.getPermissionsFor(Role.VENDOR_ADMIN);

      assertThat(permissions).contains(
          Permission.STATION_READ, Permission.STATION_WRITE, Permission.STATION_MANAGE,
          Permission.SESSION_READ, Permission.SESSION_START, Permission.SESSION_STOP,
          Permission.BILLING_READ, Permission.BILLING_MANAGE,
          Permission.USER_READ, Permission.USER_MANAGE, Permission.VENDOR_READ);
      assertThat(permissions).doesNotContain(Permission.VENDOR_MANAGE, Permission.CREDENTIAL_RESET);
    }

    @Test
    @DisplayName("VENDOR_USER has station, session read, billing read")
    void shouldReturnVendorUserPermissions() {
      Set<Permission> permissions = RolePermissionMapping.getPermissionsFor(Role.VENDOR_USER);

      assertThat(permissions).contains(
          Permission.STATION_READ, Permission.SESSION_READ,
          Permission.SESSION_START, Permission.SESSION_STOP, Permission.BILLING_READ);
      assertThat(permissions).doesNotContain(Permission.STATION_WRITE, Permission.USER_MANAGE);
    }

    @Test
    @DisplayName("CUSTOMER has session and billing read")
    void shouldReturnCustomerPermissions() {
      Set<Permission> permissions = RolePermissionMapping.getPermissionsFor(Role.CUSTOMER);

      assertThat(permissions).contains(
          Permission.SESSION_READ, Permission.SESSION_START,
          Permission.SESSION_STOP, Permission.BILLING_READ);
      assertThat(permissions).doesNotContain(Permission.STATION_READ, Permission.USER_READ);
    }

    @Test
    @DisplayName("null role returns empty set")
    void shouldReturnEmptyForNull() {
      assertThat(RolePermissionMapping.getPermissionsFor(null)).isEmpty();
    }
  }
}
