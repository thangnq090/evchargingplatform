package com.evcharging.vehicle.api.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.evcharging.shared.api.ApiResponse;
import com.evcharging.shared.security.SecurityUtils;
import com.evcharging.vehicle.api.dto.AssociateRfidRequest;
import com.evcharging.vehicle.api.dto.RegisterVehicleRequest;
import com.evcharging.vehicle.api.dto.VehicleResponse;
import com.evcharging.vehicle.application.service.VehicleApplicationService;
import com.evcharging.vehicle.domain.model.Vehicle;

import reactor.core.publisher.Mono;

/** Customer-facing REST controller for vehicle lifecycle operations. */
@RestController
@RequestMapping("/api/v1/vehicles")
@PreAuthorize("hasRole('CUSTOMER')")
public class VehicleController {

  private final VehicleApplicationService vehicleApplicationService;

  public VehicleController(VehicleApplicationService vehicleApplicationService) {
    this.vehicleApplicationService = vehicleApplicationService;
  }

  /**
   * Register a new vehicle.
   *
   * <p>POST /api/v1/vehicles → 201 Created
   */
  @PostMapping
  public Mono<ResponseEntity<ApiResponse<VehicleResponse>>> registerVehicle(
      @Valid @RequestBody RegisterVehicleRequest request) {
    return SecurityUtils.getReactiveUserId()
        .map(
            customerId -> {
              Vehicle vehicle =
                  vehicleApplicationService.registerVehicle(
                      customerId, request.registrationPlate(), request.rfidNumber());
              return ResponseEntity.status(HttpStatus.CREATED)
                  .body(ApiResponse.ok(VehicleResponse.from(vehicle)));
            });
  }

  /**
   * List my ACTIVE vehicles.
   *
   * <p>GET /api/v1/vehicles?page=0&limit=20 → 200 OK
   */
  @GetMapping
  public Mono<ResponseEntity<ApiResponse<List<VehicleResponse>>>> listMyVehicles(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int limit) {
    return SecurityUtils.getReactiveUserId()
        .map(
            customerId -> {
              List<Vehicle> list =
                  vehicleApplicationService.listMyVehicles(customerId, page, Math.min(limit, 100));
              List<VehicleResponse> vehicles = list.stream().map(VehicleResponse::from).toList();
              return ResponseEntity.ok(ApiResponse.ok(vehicles));
            });
  }

  /**
   * Get a specific vehicle (must be owner).
   *
   * <p>GET /api/v1/vehicles/{vehicleId} → 200 OK | 403 | 404
   */
  @GetMapping("/{vehicleId}")
  public Mono<ResponseEntity<ApiResponse<VehicleResponse>>> getVehicle(
      @PathVariable UUID vehicleId) {
    return SecurityUtils.getReactiveUserId()
        .map(
            customerId ->
                vehicleApplicationService
                    .getMyVehicle(vehicleId, customerId)
                    .<ResponseEntity<ApiResponse<VehicleResponse>>>map(
                        v -> ResponseEntity.ok(ApiResponse.ok(VehicleResponse.from(v))))
                    .orElse(ResponseEntity.notFound().build()));
  }

  /**
   * Associate an RFID tag with a vehicle.
   *
   * <p>PATCH /api/v1/vehicles/{vehicleId}/rfid → 200 OK | 403 | 404 | 409
   */
  @PatchMapping("/{vehicleId}/rfid")
  public Mono<ResponseEntity<ApiResponse<VehicleResponse>>> associateRfid(
      @PathVariable UUID vehicleId, @Valid @RequestBody AssociateRfidRequest request) {
    return SecurityUtils.getReactiveUserId()
        .map(
            customerId -> {
              Vehicle vehicle =
                  vehicleApplicationService.associateRfid(
                      vehicleId, customerId, request.rfidNumber());
              return ResponseEntity.ok(ApiResponse.ok(VehicleResponse.from(vehicle)));
            });
  }

  /**
   * De-list a vehicle.
   *
   * <p>DELETE /api/v1/vehicles/{vehicleId} → 204 No Content | 403 | 404 | 422
   */
  @DeleteMapping("/{vehicleId}")
  public Mono<ResponseEntity<Void>> delistVehicle(@PathVariable UUID vehicleId) {
    return SecurityUtils.getReactiveUserId()
        .map(
            customerId -> {
              vehicleApplicationService.delistVehicle(vehicleId, customerId);
              return ResponseEntity.<Void>noContent().build();
            });
  }

  /**
   * Look up a vehicle by RFID (customers and internal callers).
   *
   * <p>GET /api/v1/vehicles/lookup/rfid/{rfid} → 200 OK | 404
   */
  @GetMapping("/lookup/rfid/{rfid}")
  @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
  public Mono<ResponseEntity<ApiResponse<VehicleResponse>>> lookupByRfid(
      @PathVariable String rfid) {
    return Mono.fromCallable(() -> vehicleApplicationService.lookupByRfid(rfid))
        .map(
            opt ->
                opt.<ResponseEntity<ApiResponse<VehicleResponse>>>map(
                        v -> ResponseEntity.ok(ApiResponse.ok(VehicleResponse.from(v))))
                    .orElse(ResponseEntity.notFound().build()));
  }
}
