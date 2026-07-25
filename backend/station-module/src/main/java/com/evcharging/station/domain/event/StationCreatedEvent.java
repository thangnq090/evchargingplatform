package com.evcharging.station.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Published when a new station is created. */
public record StationCreatedEvent(
    UUID stationId,
    UUID vendorId,
    String name,
    double latitude,
    double longitude,
    int unitPriceTenthCents,
    Instant timestamp) {}
