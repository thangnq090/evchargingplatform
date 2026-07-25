package com.evcharging.station.domain.model;

/** Connector availability status. */
public enum ConnectorStatus {
  /** Connector is available for use. */
  AVAILABLE,

  /** Connector is currently in use by a charging session. */
  IN_USE,

  /** Connector is unavailable (fault or maintenance). */
  UNAVAILABLE
}
