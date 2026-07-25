package com.evcharging.station.application.dto;

import java.time.Instant;
import java.util.List;

import com.evcharging.shared.kernel.Location;

/** Response containing station details. */
public record StationResponse(
    String id,
    String vendorId,
    String name,
    String groupLabel,
    Integer unitPriceTenthCents,
    String status,
    Location location,
    List<ConnectorResponse> connectors,
    Instant createdAt,
    Instant updatedAt) {

  /** Connector details in response. */
  public record ConnectorResponse(String id, String type, Integer maxPowerKw, String status) {}
}
