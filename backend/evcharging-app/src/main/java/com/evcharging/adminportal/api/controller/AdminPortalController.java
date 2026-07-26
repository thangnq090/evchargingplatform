package com.evcharging.adminportal.api.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.evcharging.adminportal.api.dto.AdminDashboardSummaryResponse;
import com.evcharging.adminportal.api.dto.VendorDashboardSummaryResponse;
import com.evcharging.adminportal.application.service.AdminDashboardApplicationService;
import com.evcharging.shared.api.ApiResponse;
import com.evcharging.shared.security.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

/** REST Controller for Admin and Vendor Portal dashboards. */
@Tag(
    name = "Admin & Vendor Portal Analytics",
    description =
        "Endpoints for platform administrators and vendor operators to view system metrics and revenue performance")
@RestController
@RequestMapping("/api/v1")
public class AdminPortalController {

  private final AdminDashboardApplicationService dashboardService;

  public AdminPortalController(AdminDashboardApplicationService dashboardService) {
    this.dashboardService = dashboardService;
  }

  /** GET /api/v1/admin/dashboard → Platform-wide metrics for Admin users. */
  @Operation(
      summary = "Get Platform Admin Analytics Dashboard",
      description =
          "Retrieves platform-wide summary metrics (revenue, total sessions, active stations, vendor performance). Requires ROLE_ADMIN.")
  @GetMapping("/admin/dashboard")
  @PreAuthorize("hasRole('ADMIN')")
  public Mono<ResponseEntity<ApiResponse<AdminDashboardSummaryResponse>>> getAdminDashboard(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate,
      @RequestParam(required = false) UUID vendorId) {

    return Mono.fromCallable(
            () -> dashboardService.getAdminDashboardSummary(startDate, endDate, vendorId))
        .map(summary -> ResponseEntity.ok(ApiResponse.ok(summary)));
  }

  /** GET /api/v1/vendor/dashboard → Vendor-scoped metrics. */
  @Operation(
      summary = "Get Vendor Operator Dashboard Metrics",
      description =
          "Retrieves vendor-scoped performance metrics (station counts, revenue, session stats). Requires ROLE_VENDOR_ADMIN or ROLE_VENDOR_USER.")
  @GetMapping("/vendor/dashboard")
  @PreAuthorize("hasRole('VENDOR_ADMIN') or hasRole('VENDOR_USER')")
  public Mono<ResponseEntity<ApiResponse<VendorDashboardSummaryResponse>>> getVendorDashboard() {
    return SecurityUtils.getReactiveVendorId()
        .map(
            vendorId -> {
              VendorDashboardSummaryResponse summary =
                  dashboardService.getVendorDashboardSummary(vendorId);
              return ResponseEntity.ok(ApiResponse.ok(summary));
            });
  }
}
