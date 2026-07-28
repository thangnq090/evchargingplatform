package com.evcharging.billing.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("IncomeReportResponse")
class IncomeReportResponseTest {

  @Test
  @DisplayName("record with all fields")
  void shouldCreateIncomeReportResponse() {
    IncomeReportResponse.VendorBreakdownDto breakdown =
        new IncomeReportResponse.VendorBreakdownDto(
            UUID.randomUUID(), "ACME", new BigDecimal("100.00"), 5);

    IncomeReportResponse response = new IncomeReportResponse(
        new BigDecimal("500.00"), 20, List.of(breakdown));

    assertThat(response.totalRevenue()).isEqualByComparingTo(new BigDecimal("500.00"));
    assertThat(response.sessionCount()).isEqualTo(20);
    assertThat(response.breakdowns()).hasSize(1);
    assertThat(response.breakdowns().get(0).vendorName()).isEqualTo("ACME");
    assertThat(response.breakdowns().get(0).revenue()).isEqualByComparingTo(new BigDecimal("100.00"));
    assertThat(response.breakdowns().get(0).sessionCount()).isEqualTo(5);
  }
}
