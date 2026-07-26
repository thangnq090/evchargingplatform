package com.evcharging.station.api.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.evcharging.shared.api.ApiResponse;
import com.evcharging.shared.kernel.VendorId;
import com.evcharging.station.application.service.MarkupApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

/** REST controller for Admin markup configuration. */
@Tag(
    name = "Admin Station Markup Management",
    description = "Endpoints for platform administrators to configure vendor percentage markups")
@RestController
@RequestMapping("/api/v1/admin/vendors")
public class AdminMarkupController {

  private final MarkupApplicationService service;

  public AdminMarkupController(MarkupApplicationService service) {
    this.service = service;
  }

  /** Sets the markup percentage for a vendor (admin only). */
  @Operation(
      summary = "Set Vendor Pricing Markup",
      description =
          "Configures vendor markup in basis points (e.g. 500 basis points = 5.00%). Requires ROLE_ADMIN.")
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
  @Operation(
      summary = "Get Vendor Pricing Markup",
      description = "Retrieves the current configured pricing markup in basis points for a vendor.")
  @GetMapping("/{vendorId}/markup")
  @PreAuthorize("@vendorSecurity.checkAccess(#vendorId)")
  public Mono<ResponseEntity<ApiResponse<MarkupResponse>>> getMarkup(@PathVariable UUID vendorId) {
    return Mono.fromCallable(() -> service.getMarkup(VendorId.of(vendorId)))
        .map(
            markup ->
                ResponseEntity.ok(ApiResponse.ok(new MarkupResponse(markup.getBasisPoints()))));
  }

  /** Request to set vendor markup. */
  @Schema(description = "Request body to configure vendor markup")
  public record SetMarkupRequest(
      @Schema(description = "Markup percentage in basis points (100 bp = 1%)", example = "500")
          int markupBasisPoints) {}

  /** Response containing markup basis points. */
  @Schema(description = "Response payload containing vendor markup basis points")
  public record MarkupResponse(
      @Schema(description = "Configured markup percentage in basis points", example = "500")
          int markupBasisPoints) {}
}
