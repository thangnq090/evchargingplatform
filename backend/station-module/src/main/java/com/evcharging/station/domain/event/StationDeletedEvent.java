package com.evcharging.station.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Published when a station is soft-deleted. */
public record StationDeletedEvent(UUID stationId, UUID vendorId, Instant timestamp) {}
