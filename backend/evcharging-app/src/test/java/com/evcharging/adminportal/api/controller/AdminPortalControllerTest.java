package com.evcharging.adminportal.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.evcharging.adminportal.api.dto.AdminDashboardSummaryResponse;
import com.evcharging.adminportal.api.dto.VendorDashboardSummaryResponse;
import com.evcharging.adminportal.application.service.AdminDashboardApplicationService;

@ExtendWith(MockitoExtension.class)
class AdminPortalControllerTest {

  @Mock private AdminDashboardApplicationService dashboardService;

  @InjectMocks private AdminPortalController controller;

  private UUID vendorId;

  @BeforeEach
  void setUp() {
    vendorId = UUID.randomUUID();
  }

  @Test
  void shouldReturnAdminDashboardSummary() {
    LocalDate start = LocalDate.now().minusDays(7);
    LocalDate end = LocalDate.now();

    AdminDashboardSummaryResponse summary =
        new AdminDashboardSummaryResponse(
            BigDecimal.valueOf(500.00),
            10,
            2,
            5,
            1,
            List.of(
                new AdminDashboardSummaryResponse.VendorSummaryDto(
                    vendorId, "Vendor A", BigDecimal.valueOf(500.00), 10)));

    when(dashboardService.getAdminDashboardSummary(eq(start), eq(end), eq(vendorId)))
        .thenReturn(summary);

    controller
        .getAdminDashboard(start, end, vendorId)
        .subscribe(
            response -> {
              assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
              assertThat(response.getBody()).isNotNull();
              assertThat(response.getBody().data().totalRevenue())
                  .isEqualTo(BigDecimal.valueOf(500.00));
              assertThat(response.getBody().data().totalSessions()).isEqualTo(10);
            });
  }

  @Test
  void shouldReturnVendorDashboardSummary() {
    VendorDashboardSummaryResponse summary =
        new VendorDashboardSummaryResponse(
            vendorId, "Vendor A", BigDecimal.valueOf(250.00), 5, 2, 4);

    when(dashboardService.getVendorDashboardSummary(eq(vendorId))).thenReturn(summary);

    VendorDashboardSummaryResponse result = dashboardService.getVendorDashboardSummary(vendorId);
    assertThat(result.vendorId()).isEqualTo(vendorId);
    assertThat(result.currentMonthRevenue()).isEqualTo(BigDecimal.valueOf(250.00));
  }
}
