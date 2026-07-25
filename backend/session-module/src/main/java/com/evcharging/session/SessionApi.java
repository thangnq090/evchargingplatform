package com.evcharging.session;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Public API exposed by the session module for other modules to consume. Located in the root
 * package to satisfy Spring Modulith exposure rules.
 */
public interface SessionApi {

  Optional<SessionDetails> getSessionDetails(UUID sessionId);

  record SessionDetails(
      UUID sessionId,
      UUID stationId,
      Integer connectorId,
      UUID customerId,
      UUID vehicleId,
      String status,
      Instant startTime,
      Instant endTime,
      BigDecimal totalEnergyKwh,
      BigDecimal unitRateAmount,
      String unitRateCurrency,
      BigDecimal totalAmount) {}
}
