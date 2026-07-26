package com.evcharging.station.api.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.evcharging.shared.api.ApiResponse;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.shared.kernel.VendorId;
import com.evcharging.shared.pagination.PaginatedList;
import com.evcharging.station.application.dto.ChangeStatusRequest;
import com.evcharging.station.application.dto.CreateStationRequest;
import com.evcharging.station.application.dto.StationResponse;
import com.evcharging.station.application.dto.UpdateStationRequest;
import com.evcharging.station.application.service.StationApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

/** REST controller for Station management. */
@Tag(
    name = "Station & Charging Location Management",
    description =
        "Endpoints for managing EV charging locations, EVSEs, connectors, and operational statuses")
@RestController
@RequestMapping("/api/v1/stations")
public class StationController {

  private final StationApplicationService service;

  public StationController(StationApplicationService service) {
    this.service = service;
  }

  @Operation(
      summary = "Create Charging Station",
      description =
          "Creates a new charging station site with EVSEs and connectors under the authenticated vendor's account.")
  @PostMapping
  @PreAuthorize("hasRole('VENDOR_ADMIN') or hasRole('VENDOR_USER')")
  public Mono<ResponseEntity<ApiResponse<StationResponse>>> createStation(
      @Valid @RequestBody CreateStationRequest request) {

    return com.evcharging.shared.security.SecurityUtils.getReactiveVendorId()
        .map(VendorId::of)
        .switchIfEmpty(
            Mono.error(new IllegalArgumentException("Vendor ID not found in security context")))
        .flatMap(vendorId -> Mono.fromCallable(() -> service.createStation(vendorId, request)))
        .map(
            response ->
                ResponseEntity.created(URI.create("/api/v1/stations/" + response.id()))
                    .body(ApiResponse.ok(response)));
  }

  @Operation(
      summary = "Get Station Details",
      description =
          "Retrieves full details of a specific charging station including EVSE and connector configurations.")
  @GetMapping("/{stationId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR_ADMIN') or hasRole('VENDOR_USER')")
  public Mono<ResponseEntity<ApiResponse<StationResponse>>> getStation(
      @PathVariable UUID stationId) {
    return Mono.fromCallable(() -> service.getStation(StationId.of(stationId)))
        .map(response -> ResponseEntity.ok(ApiResponse.ok(response)));
  }

  @Operation(
      summary = "List Charging Stations (Paginated)",
      description =
          "Retrieves a paginated list of charging stations, filtered by status and vendor ID.")
  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR_ADMIN') or hasRole('VENDOR_USER')")
  public Mono<ResponseEntity<ApiResponse<PaginatedList<StationResponse>>>> listStations(
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(required = false) String cursor) {

    return com.evcharging.shared.security.SecurityUtils.getReactiveVendorId()
        .map(Optional::of)
        .defaultIfEmpty(Optional.empty())
        .flatMap(
            optVendorId ->
                Mono.fromCallable(
                    () -> {
                      VendorId vendorId = optVendorId.map(VendorId::of).orElse(null);
                      return service.listStations(vendorId, status, limit, cursor);
                    }))
        .map(stations -> ResponseEntity.ok(ApiResponse.ok(stations)));
  }

  @Operation(
      summary = "Update Station Details",
      description =
          "Updates station metadata (name, address, location coordinates) for an existing charging station.")
  @PatchMapping("/{stationId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR_ADMIN') or hasRole('VENDOR_USER')")
  public Mono<ResponseEntity<ApiResponse<StationResponse>>> updateStation(
      @PathVariable UUID stationId, @Valid @RequestBody UpdateStationRequest request) {

    return Mono.fromCallable(() -> service.updateStation(StationId.of(stationId), request))
        .map(response -> ResponseEntity.ok(ApiResponse.ok(response)));
  }

  @Operation(
      summary = "Update Station Operational Status",
      description =
          "Changes operational status of a charging station (e.g. AVAILABLE, OCCUPIED, UNKOWN/OFFLINE, OUT_OF_SERVICE).")
  @PutMapping("/{stationId}/status")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR_ADMIN') or hasRole('VENDOR_USER')")
  public Mono<ResponseEntity<ApiResponse<StationResponse>>> changeStatus(
      @PathVariable UUID stationId, @Valid @RequestBody ChangeStatusRequest request) {

    return Mono.fromCallable(() -> service.changeStatus(StationId.of(stationId), request))
        .map(response -> ResponseEntity.ok(ApiResponse.ok(response)));
  }

  @DeleteMapping("/{stationId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR_ADMIN')")
  public Mono<ResponseEntity<Void>> deleteStation(@PathVariable UUID stationId) {
    return Mono.<Void>fromRunnable(() -> service.deleteStation(StationId.of(stationId)))
        .thenReturn(ResponseEntity.noContent().build());
  }

  @GetMapping("/nearby")
  @PreAuthorize("isAuthenticated()")
  public Mono<ResponseEntity<ApiResponse<List<StationResponse>>>> findNearby(
      @RequestParam double lat,
      @RequestParam double lng,
      @RequestParam(defaultValue = "10") double radiusKm,
      @RequestParam(defaultValue = "AVAILABLE") String status) {

    return Mono.fromCallable(
            () -> {
              List<StationResponse> stations = service.findNearby(lat, lng, radiusKm);
              if (!"ALL".equalsIgnoreCase(status)) {
                stations =
                    stations.stream().filter(s -> s.status().equalsIgnoreCase(status)).toList();
              }
              return stations;
            })
        .map(stations -> ResponseEntity.ok(ApiResponse.ok(stations)));
  }
}
