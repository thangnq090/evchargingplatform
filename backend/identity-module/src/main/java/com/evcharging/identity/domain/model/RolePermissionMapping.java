package com.evcharging.identity.domain.model;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Static domain helper defining the mapping of roles to sets of fine-grained permissions. */
public final class RolePermissionMapping {

  private static final Map<Role, Set<Permission>> MAPPINGS =
      Map.of(
          Role.ADMIN, EnumSet.allOf(Permission.class),
          Role.VENDOR_ADMIN,
              EnumSet.of(
                  Permission.STATION_READ,
                  Permission.STATION_WRITE,
                  Permission.STATION_MANAGE,
                  Permission.SESSION_READ,
                  Permission.SESSION_START,
                  Permission.SESSION_STOP,
                  Permission.BILLING_READ,
                  Permission.BILLING_MANAGE,
                  Permission.USER_READ,
                  Permission.USER_MANAGE,
                  Permission.VENDOR_READ),
          Role.VENDOR_USER,
              EnumSet.of(
                  Permission.STATION_READ,
                  Permission.SESSION_READ,
                  Permission.SESSION_START,
                  Permission.SESSION_STOP,
                  Permission.BILLING_READ),
          Role.CUSTOMER,
              EnumSet.of(
                  Permission.SESSION_READ,
                  Permission.SESSION_START,
                  Permission.SESSION_STOP,
                  Permission.BILLING_READ));

  private RolePermissionMapping() {}

  public static Set<Permission> getPermissionsFor(Role role) {
    if (role == null) {
      return Set.of();
    }
    return MAPPINGS.getOrDefault(role, Set.of());
  }
}
