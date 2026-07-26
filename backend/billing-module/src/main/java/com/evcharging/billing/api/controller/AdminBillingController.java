package com.evcharging.billing.api.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.evcharging.billing.BillingApi.IncomeSummary;
import com.evcharging.billing.application.dto.InvoiceResponse;
import com.evcharging.billing.application.service.BillingApplicationService;
import com.evcharging.shared.api.ApiResponse;

import reactor.core.publisher.Mono;

@RestController
public class AdminBillingController {

  private final BillingApplicationService billingApplicationService;

  public AdminBillingController(BillingApplicationService billingApplicationService) {
    this.billingApplicationService = billingApplicationService;
  }

  /**
   * Admin: Generate income report filtered by date range and optionally by vendor. GET
   * /api/v1/admin/billing/income?startDate=...&endDate=...&vendorId=...
   */
  @GetMapping("/api/v1/admin/billing/income")
  @PreAuthorize("hasRole('ADMIN')")
  public Mono<ResponseEntity<ApiResponse<IncomeSummary>>> getAdminIncomeReport(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(required = false) UUID vendorId) {

    return Mono.fromCallable(
            () -> billingApplicationService.getAdminIncomeReport(startDate, endDate, vendorId))
        .map(report -> ResponseEntity.ok(ApiResponse.ok(report)));
  }

  /**
   * Retrieve invoice for a completed session (admin or session owner). GET
   * /api/v1/billing/invoices/session/{sessionId}
   */
  @GetMapping("/api/v1/billing/invoices/session/{sessionId}")
  @PreAuthorize(
      "hasRole('ADMIN') or hasRole('CUSTOMER') or hasRole('VENDOR_ADMIN') or hasRole('VENDOR_USER')")
  public Mono<ResponseEntity<ApiResponse<InvoiceResponse>>> getInvoiceBySession(
      @PathVariable UUID sessionId) {

    return Mono.fromCallable(() -> billingApplicationService.getInvoiceBySessionId(sessionId))
        .map(
            opt ->
                opt.<ResponseEntity<ApiResponse<InvoiceResponse>>>map(
                        invoice -> ResponseEntity.ok(ApiResponse.ok(invoice)))
                    .orElse(ResponseEntity.notFound().build()));
  }
}
