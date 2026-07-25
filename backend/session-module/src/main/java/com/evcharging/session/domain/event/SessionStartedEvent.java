package com.evcharging.session.domain.event;

import java.time.Instant;

import com.evcharging.session.domain.model.SessionId;
import com.evcharging.shared.kernel.Money;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.shared.kernel.UserId;

/** Event published when a charging session is successfully started. */
public record SessionStartedEvent(
    SessionId sessionId,
    StationId stationId,
    Integer connectorId,
    UserId customerId,
    Money unitRate,
    Instant startTime) {}
