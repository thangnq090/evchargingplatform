package com.evcharging.vehicle.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evcharging.shared.api.ApiResponse;
import com.evcharging.vehicle.api.dto.OwnershipRecordResponse;
import com.evcharging.vehicle.api.dto.VehicleResponse;
import com.evcharging.vehicle.application.service.VehicleApplicationService;

import reactor.core.publisher.Mono;

/** Admin-only REST controller for vehicle management. */
@RestController
@RequestMapping("/api/v1/admin/vehicles")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVehicleController {

  private final VehicleApplicationService vehicleApplicationService;

  public AdminVehicleController(VehicleApplicationService vehicleApplicationService) {
    this.vehicleApplicationService = vehicleApplicationService;
  }

  /**
   * Get any vehicle by ID (admin bypass).
   *
   * <p>GET /api/v1/admin/vehicles/{vehicleId} → 200 OK | 404
   */
  @GetMapping("/{vehicleId}")
  public Mono<ResponseEntity<ApiResponse<VehicleResponse>>> getVehicle(
      @PathVariable UUID vehicleId) {
    return Mono.fromCallable(() -> vehicleApplicationService.adminGetVehicle(vehicleId))
        .map(
            opt ->
                opt.<ResponseEntity<ApiResponse<VehicleResponse>>>map(
                        v -> ResponseEntity.ok(ApiResponse.ok(VehicleResponse.from(v))))
                    .orElse(ResponseEntity.notFound().build()));
  }

  /**
   * Get full ownership history for a vehicle.
   *
   * <p>GET /api/v1/admin/vehicles/{vehicleId}/ownership → 200 OK
   */
  @GetMapping("/{vehicleId}/ownership")
  public Mono<ResponseEntity<ApiResponse<List<OwnershipRecordResponse>>>> getOwnershipHistory(
      @PathVariable UUID vehicleId) {
    return Mono.fromCallable(() -> vehicleApplicationService.adminGetOwnershipHistory(vehicleId))
        .map(
            records -> {
              List<OwnershipRecordResponse> response =
                  records.stream().map(OwnershipRecordResponse::from).toList();
              return ResponseEntity.ok(ApiResponse.ok(response));
            });
  }
}
