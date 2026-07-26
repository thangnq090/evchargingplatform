package com.evcharging.adminportal.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evcharging.adminportal.api.dto.AdminDashboardSummaryResponse;
import com.evcharging.adminportal.api.dto.VendorDashboardSummaryResponse;
import com.evcharging.billing.BillingApi;
import com.evcharging.billing.BillingApi.IncomeSummary;
import com.evcharging.identity.VendorMarkupApi;

/**
 * Application service for Admin and Vendor Portal dashboards. Aggregates data from modules without
 * owning domain state.
 */
@Service
@Transactional(readOnly = true)
public class AdminDashboardApplicationService {

  private final BillingApi billingApi;
  private final VendorMarkupApi vendorMarkupApi;

  public AdminDashboardApplicationService(BillingApi billingApi, VendorMarkupApi vendorMarkupApi) {
    this.billingApi = billingApi;
    this.vendorMarkupApi = vendorMarkupApi;
  }

  /** Gets platform-wide summary metrics for Admin Dashboard. */
  public AdminDashboardSummaryResponse getAdminDashboardSummary(
      LocalDate startDate, LocalDate endDate, UUID vendorId) {
    LocalDate start = startDate != null ? startDate : LocalDate.now().minusMonths(1);
    LocalDate end = endDate != null ? endDate : LocalDate.now();

    IncomeSummary report = billingApi.getAdminIncomeReport(start, end, vendorId);

    return new AdminDashboardSummaryResponse(
        BigDecimal.valueOf(report.totalRevenue()),
        report.sessionCount(),
        0, // active sessions aggregate placeholder
        0, // stations aggregate placeholder
        0, // vendor count
        List.of());
  }

  /** Gets vendor-scoped summary metrics for Vendor Dashboard. */
  public VendorDashboardSummaryResponse getVendorDashboardSummary(UUID vendorId) {
    LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
    LocalDate today = LocalDate.now();

    IncomeSummary report = billingApi.getAdminIncomeReport(startOfMonth, today, vendorId);

    String vendorName = vendorMarkupApi.getVendorName(vendorId).orElse("Vendor " + vendorId);

    return new VendorDashboardSummaryResponse(
        vendorId,
        vendorName,
        BigDecimal.valueOf(report.totalRevenue()),
        report.sessionCount(),
        0, // active stations
        0 // total stations
        );
  }
}
