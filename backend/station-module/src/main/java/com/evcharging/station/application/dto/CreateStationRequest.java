package com.evcharging.station.application.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.evcharging.shared.kernel.Location;
import com.evcharging.station.domain.model.ConnectorType;

/** Request to create a new charging station. */
public record CreateStationRequest(
    @NotBlank @Size(max = 100) String name,
    @Size(max = 50) String groupLabel,
    @NotNull @Min(0) Integer unitPriceTenthCents,
    @NotNull Location location,
    @NotNull @Size(min = 1, max = 10) List<ConnectorRequest> connectors) {

  /** Connector specification for creation. */
  public record ConnectorRequest(
      @NotNull ConnectorType type, @NotNull @Min(1) @Max(500) Integer maxPowerKw) {}
}
