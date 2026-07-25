package com.evcharging.station.domain.event;

import java.time.Instant;
import java.util.UUID;

import com.evcharging.station.domain.model.StationStatus;

/** Published when a station's availability status changes. */
public record StationStatusChangedEvent(
    UUID stationId,
    UUID vendorId,
    StationStatus oldStatus,
    StationStatus newStatus,
    Instant timestamp) {}
