package com.evcharging.session.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

import com.evcharging.session.domain.model.SessionId;
import com.evcharging.shared.kernel.Money;

/** Event published when a charging session completes successfully. */
public record SessionCompletedEvent(
    SessionId sessionId, Instant endTime, BigDecimal totalEnergyKwh, Money totalAmount) {}
