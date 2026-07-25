package com.evcharging.station.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Published when a vendor's markup percentage is updated. */
public record VendorMarkupChangedEvent(
    UUID vendorId,
    int oldMarkupBasisPoints,
    int newMarkupBasisPoints,
    UUID changedBy,
    Instant timestamp) {}
