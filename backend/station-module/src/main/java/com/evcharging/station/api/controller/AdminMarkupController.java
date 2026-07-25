package com.evcharging.station.api.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.evcharging.shared.api.ApiResponse;
import com.evcharging.shared.kernel.VendorId;
import com.evcharging.station.application.service.MarkupApplicationService;

import reactor.core.publisher.Mono;

/** REST controller for Admin markup configuration. */
@RestController
@RequestMapping("/api/v1/admin/vendors")
public class AdminMarkupController {

  private final MarkupApplicationService service;

  public AdminMarkupController(MarkupApplicationService service) {
    this.service = service;
  }

  /** Sets the markup percentage for a vendor (admin only). */
  @PutMapping("/{vendorId}/markup")
  @PreAuthorize("hasRole('ADMIN')")
  public Mono<ResponseEntity<ApiResponse<MarkupResponse>>> setMarkup(
      @PathVariable UUID vendorId, @RequestBody SetMarkupRequest request) {

    return com.evcharging.shared.security.SecurityUtils.getReactiveUserId()
        .defaultIfEmpty(UUID.randomUUID()) // fallback
        .flatMap(
            adminId ->
                Mono.fromCallable(
                    () ->
                        service.setMarkup(
                            VendorId.of(vendorId), request.markupBasisPoints(), adminId)))
        .map(
            markup ->
                ResponseEntity.ok(ApiResponse.ok(new MarkupResponse(markup.getBasisPoints()))));
  }

  /** Gets the markup percentage for a vendor. */
  @GetMapping("/{vendorId}/markup")
  @PreAuthorize("@vendorSecurity.checkAccess(#vendorId)")
  public Mono<ResponseEntity<ApiResponse<MarkupResponse>>> getMarkup(@PathVariable UUID vendorId) {
    return Mono.fromCallable(() -> service.getMarkup(VendorId.of(vendorId)))
        .map(
            markup ->
                ResponseEntity.ok(ApiResponse.ok(new MarkupResponse(markup.getBasisPoints()))));
  }

  /** Request to set vendor markup. */
  public record SetMarkupRequest(int markupBasisPoints) {}

  /** Response containing markup basis points. */
  public record MarkupResponse(int markupBasisPoints) {}
}
