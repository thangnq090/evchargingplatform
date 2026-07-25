package com.evcharging.station.application.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.evcharging.shared.kernel.Location;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.shared.kernel.VendorId;
import com.evcharging.station.application.dto.ChangeStatusRequest;
import com.evcharging.station.application.dto.CreateStationRequest;
import com.evcharging.station.application.dto.StationResponse;
import com.evcharging.station.application.dto.UpdateStationRequest;
import com.evcharging.station.domain.model.Connector;
import com.evcharging.station.domain.model.Station;
import com.evcharging.station.domain.model.StationStatus;
import com.evcharging.station.domain.repository.ConnectorRepository;
import com.evcharging.station.domain.repository.StationRepository;
import com.evcharging.station.domain.repository.VendorRepository;
import com.evcharging.station.domain.service.StationDomainService;

/** Application service for Station use cases. */
@Service
@Validated
@Transactional
public class StationApplicationService {

  private final StationDomainService domainService;
  private final StationRepository stationRepository;
  private final ConnectorRepository connectorRepository;
  private final VendorRepository vendorRepository;
  private final ApplicationEventPublisher eventPublisher;

  public StationApplicationService(
      StationDomainService domainService,
      StationRepository stationRepository,
      ConnectorRepository connectorRepository,
      VendorRepository vendorRepository,
      ApplicationEventPublisher eventPublisher) {
    this.domainService = domainService;
    this.stationRepository = stationRepository;
    this.connectorRepository = connectorRepository;
    this.vendorRepository = vendorRepository;
    this.eventPublisher = eventPublisher;
  }

  /** Creates a new station. */
  public StationResponse createStation(VendorId vendorId, @Valid CreateStationRequest request) {
    List<Connector> connectors =
        request.connectors().stream()
            .map(c -> Connector.create(UUID.randomUUID(), c.type(), c.maxPowerKw()))
            .collect(Collectors.toList());

    Station station =
        domainService.createStation(
            vendorId,
            request.name(),
            request.groupLabel(),
            request.unitPriceTenthCents(),
            request.location(),
            connectors);

    Station saved = stationRepository.save(station);

    eventPublisher.publishEvent(
        new com.evcharging.station.domain.event.StationCreatedEvent(
            saved.getId(),
            saved.getVendorId(),
            saved.getName(),
            saved.getLocation().getLatitude().doubleValue(),
            saved.getLocation().getLongitude().doubleValue(),
            saved.getUnitPriceTenthCents(),
            java.time.Instant.now()));

    return toResponse(saved);
  }

  /** Gets a station by ID. */
  @Transactional(readOnly = true)
  public StationResponse getStation(StationId stationId) {
    Station station =
        stationRepository
            .findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Station not found: " + stationId));
    return toResponse(station);
  }

  /** Lists stations for a vendor (paginated). */
  @Transactional(readOnly = true)
  public List<StationResponse> listStations(
      VendorId vendorId, String status, int limit, String cursor) {
    StationStatus stationStatus =
        status != null ? StationStatus.valueOf(status.toUpperCase()) : null;
    List<Station> stations =
        stationStatus != null
            ? stationRepository.findByVendorIdAndStatus(vendorId.getValue(), stationStatus)
            : stationRepository.findByVendorId(vendorId.getValue());

    // TODO: implement cursor-based pagination
    return stations.stream().limit(limit).map(this::toResponse).collect(Collectors.toList());
  }

  /** Updates a station. */
  public StationResponse updateStation(StationId stationId, UpdateStationRequest request) {
    Station station =
        stationRepository
            .findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Station not found: " + stationId));

    // Note: location change not supported for MVP
    station.update(request.name(), request.groupLabel(), request.unitPriceTenthCents(), null);
    Station saved = stationRepository.save(station);

    eventPublisher.publishEvent(
        new com.evcharging.station.domain.event.StationUpdatedEvent(
            saved.getId(),
            saved.getVendorId(),
            java.util.Map.of(
                "name", saved.getName(),
                "groupLabel", saved.getGroupLabel(),
                "unitPriceTenthCents", saved.getUnitPriceTenthCents()),
            java.time.Instant.now()));

    return toResponse(saved);
  }

  /** Changes station availability status. */
  public StationResponse changeStatus(StationId stationId, ChangeStatusRequest request) {
    Station station =
        stationRepository
            .findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Station not found: " + stationId));

    StationStatus oldStatus = station.getStatus();
    station.changeStatus(request.status());
    Station saved = stationRepository.save(station);

    eventPublisher.publishEvent(
        new com.evcharging.station.domain.event.StationStatusChangedEvent(
            saved.getId(),
            saved.getVendorId(),
            oldStatus,
            saved.getStatus(),
            java.time.Instant.now()));

    return toResponse(saved);
  }

  /** Soft-deletes a station. */
  public void deleteStation(StationId stationId) {
    Station station =
        stationRepository
            .findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Station not found: " + stationId));

    station.delete();
    stationRepository.save(station);

    eventPublisher.publishEvent(
        new com.evcharging.station.domain.event.StationDeletedEvent(
            station.getId(), station.getVendorId(), java.time.Instant.now()));
  }

  /** Finds stations near a location. */
  @Transactional(readOnly = true)
  public List<StationResponse> findNearby(double lat, double lng, double radiusKm) {
    Location location = Location.of(lat, lng);
    List<Station> stations = domainService.findNearby(location, radiusKm);
    return stations.stream().map(this::toResponse).collect(Collectors.toList());
  }

  private StationResponse toResponse(Station station) {
    return new StationResponse(
        station.getId().toString(),
        station.getVendorId().toString(),
        station.getName(),
        station.getGroupLabel(),
        station.getUnitPriceTenthCents(),
        station.getStatus().name(),
        station.getLocation(),
        station.getConnectors().stream()
            .map(
                c ->
                    new StationResponse.ConnectorResponse(
                        c.getId().toString(),
                        c.getType().name(),
                        c.getMaxPowerKw(),
                        c.getStatus().name()))
            .collect(Collectors.toList()),
        station.getCreatedAt(),
        station.getUpdatedAt());
  }
}
