package com.evcharging.identity.domain.model;

/** Fine-grained security permissions for EV Charging Platform endpoints. */
public enum Permission {
  STATION_READ,
  STATION_WRITE,
  STATION_MANAGE,
  SESSION_READ,
  SESSION_START,
  SESSION_STOP,
  BILLING_READ,
  BILLING_MANAGE,
  USER_READ,
  USER_MANAGE,
  CREDENTIAL_RESET,
  VENDOR_READ,
  VENDOR_MANAGE
}
