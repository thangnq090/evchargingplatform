package com.evcharging.session.application.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Public event published when a charging session completes successfully.
 *
 * <p>Lives in {@code application.events} (a Spring Modulith named interface) so other modules can
 * consume it without accessing internal domain types. All fields are primitive JDK types.
 */
public record SessionCompletedEvent(
    UUID sessionId,
    Instant endTime,
    BigDecimal totalEnergyKwh,
    BigDecimal totalAmountValue,
    String totalAmountCurrency) {}
