package com.evcharging.session.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

import com.evcharging.session.domain.model.SessionId;

/** Event published when a charging session fails. */
public record SessionFailedEvent(
    SessionId sessionId, Instant endTime, BigDecimal totalEnergyKwh, String errorCode) {}
