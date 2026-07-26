package com.evcharging.adminportal.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** Response DTO for vendor-scoped Vendor Dashboard metrics. */
public record VendorDashboardSummaryResponse(
    UUID vendorId,
    String vendorName,
    BigDecimal currentMonthRevenue,
    int currentMonthSessionsCount,
    int activeStationsCount,
    int totalStationsCount) {}
