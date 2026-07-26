package com.evcharging.adminportal.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Response DTO for platform-wide Admin Dashboard metrics. */
public record AdminDashboardSummaryResponse(
    BigDecimal totalRevenue,
    int totalSessions,
    int activeSessionsCount,
    int totalStationsCount,
    int totalVendorsCount,
    List<VendorSummaryDto> vendorBreakdowns) {

  public record VendorSummaryDto(
      UUID vendorId, String vendorName, BigDecimal revenue, int sessionCount) {}
}
