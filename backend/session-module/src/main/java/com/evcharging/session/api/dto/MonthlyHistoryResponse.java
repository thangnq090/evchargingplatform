package com.evcharging.session.api.dto;

import java.math.BigDecimal;
import java.util.List;

import com.evcharging.shared.kernel.Money;

public record MonthlyHistoryResponse(List<MonthGroup> history) {

  public record MonthGroup(String month, MonthTotals totals, List<SessionResponse> sessions) {}

  public record MonthTotals(int totalSessions, BigDecimal totalEnergyKwh, Money totalAmount) {}
}
