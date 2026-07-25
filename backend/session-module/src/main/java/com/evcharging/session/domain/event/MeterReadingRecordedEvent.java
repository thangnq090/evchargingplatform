package com.evcharging.session.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

import com.evcharging.session.domain.model.SessionId;

/** Event published when a meter reading is recorded for a session. */
public record MeterReadingRecordedEvent(
    SessionId sessionId, Instant timestamp, BigDecimal energyDeliveredKwh, BigDecimal powerKw) {}
