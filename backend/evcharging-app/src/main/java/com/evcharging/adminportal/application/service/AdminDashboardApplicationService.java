package com.evcharging.adminportal.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evcharging.adminportal.api.dto.AdminDashboardSummaryResponse;
import com.evcharging.adminportal.api.dto.VendorDashboardSummaryResponse;
import com.evcharging.billing.application.dto.IncomeReportResponse;
import com.evcharging.billing.application.service.BillingApplicationService;
import com.evcharging.identity.VendorMarkupApi;

/**
 * Application service for Admin and Vendor Portal dashboards. Aggregates data from modules without
 * owning domain state.
 */
@Service
@Transactional(readOnly = true)
public class AdminDashboardApplicationService {

  private final BillingApplicationService billingApplicationService;
  private final VendorMarkupApi vendorMarkupApi;

  public AdminDashboardApplicationService(
      BillingApplicationService billingApplicationService, VendorMarkupApi vendorMarkupApi) {
    this.billingApplicationService = billingApplicationService;
    this.vendorMarkupApi = vendorMarkupApi;
  }

  /** Gets platform-wide summary metrics for Admin Dashboard. */
  public AdminDashboardSummaryResponse getAdminDashboardSummary(
      LocalDate startDate, LocalDate endDate, UUID vendorId) {
    LocalDate start = startDate != null ? startDate : LocalDate.now().minusMonths(1);
    LocalDate end = endDate != null ? endDate : LocalDate.now();

    IncomeReportResponse report =
        billingApplicationService.getAdminIncomeReport(start, end, vendorId);

    List<AdminDashboardSummaryResponse.VendorSummaryDto> vendorBreakdowns =
        report.breakdowns().stream()
            .map(
                b ->
                    new AdminDashboardSummaryResponse.VendorSummaryDto(
                        b.vendorId(), b.vendorName(), b.revenue(), b.sessionCount()))
            .toList();

    return new AdminDashboardSummaryResponse(
        report.totalRevenue(),
        report.sessionCount(),
        0, // active sessions aggregate placeholder
        0, // stations aggregate placeholder
        vendorBreakdowns.size(),
        vendorBreakdowns);
  }

  /** Gets vendor-scoped summary metrics for Vendor Dashboard. */
  public VendorDashboardSummaryResponse getVendorDashboardSummary(UUID vendorId) {
    LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
    LocalDate today = LocalDate.now();

    IncomeReportResponse report =
        billingApplicationService.getAdminIncomeReport(startOfMonth, today, vendorId);

    String vendorName = vendorMarkupApi.getVendorName(vendorId).orElse("Vendor " + vendorId);

    return new VendorDashboardSummaryResponse(
        vendorId,
        vendorName,
        report.totalRevenue(),
        report.sessionCount(),
        0, // active stations
        0 // total stations
        );
  }
}
