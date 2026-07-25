package com.evcharging.billing.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record IncomeReportResponse(
    BigDecimal totalRevenue,
    int sessionCount,
    List<VendorBreakdownDto> breakdowns
) {
  public record VendorBreakdownDto(
      UUID vendorId,
      String vendorName,
      BigDecimal revenue,
      int sessionCount
  ) {}
}
