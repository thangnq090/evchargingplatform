package com.evcharging.billing;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Published API for the billing module. Other modules access billing functionality through this
 * interface to maintain module boundaries (ADR-003, ADR-005).
 *
 * <p>Returns simple values rather than internal DTOs to avoid cross-module type exposure.
 */
public interface BillingApi {

  /**
   * Returns income report summary for admin or vendor dashboard.
   *
   * @return [totalRevenue, sessionCount] — simple value array to avoid DTO exposure
   */
  IncomeSummary getAdminIncomeReport(LocalDate startDate, LocalDate endDate, UUID vendorId);

  /** Lightweight result — avoids exposing internal DTO types. */
  record IncomeSummary(double totalRevenue, int sessionCount) {}
}
