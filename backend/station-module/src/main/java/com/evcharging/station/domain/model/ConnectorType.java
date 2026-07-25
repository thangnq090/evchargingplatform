package com.evcharging.station.domain.model;

/** Connector/plug type supported by the station. */
public enum ConnectorType {
  /** Combined Charging System (CCS) - DC fast charging. */
  CCS,

  /** CHAdeMO - DC fast charging (Japanese standard). */
  CHADEMO,

  /** Type 2 (Mennekes) - AC charging (European standard). */
  TYPE_2
}
