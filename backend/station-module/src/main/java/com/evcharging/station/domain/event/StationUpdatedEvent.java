package com.evcharging.station.domain.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Published when a station is updated. */
public record StationUpdatedEvent(
    UUID stationId, UUID vendorId, Map<String, Object> changes, Instant timestamp) {}
