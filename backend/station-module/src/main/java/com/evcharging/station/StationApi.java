package com.evcharging.station;

import java.util.List;
import java.util.UUID;

import com.evcharging.shared.kernel.StationId;

/**
 * Public API exposed by the station module for other modules to consume. Located in the root
 * package to satisfy Spring Modulith exposure rules.
 */
public interface StationApi {

  StationDetails getStationDetails(StationId stationId);

  record StationDetails(
      UUID id,
      String status,
      String vendorId,
      int unitPriceTenthCents,
      List<ConnectorDetails> connectors) {}

  record ConnectorDetails(String id, String status) {}
}
