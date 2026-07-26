package com.evcharging.vehicle.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evcharging.vehicle.domain.event.RfidAssociatedEvent;
import com.evcharging.vehicle.domain.event.VehicleDelistedEvent;
import com.evcharging.vehicle.domain.event.VehicleRegisteredEvent;
import com.evcharging.vehicle.domain.model.OwnershipRecord;
import com.evcharging.vehicle.domain.model.RegistrationPlate;
import com.evcharging.vehicle.domain.model.RfidNumber;
import com.evcharging.vehicle.domain.model.Vehicle;
import com.evcharging.vehicle.domain.model.VehicleId;
import com.evcharging.vehicle.domain.model.VehicleStatus;
import com.evcharging.vehicle.domain.repository.OwnershipRecordRepository;
import com.evcharging.vehicle.domain.repository.VehicleRepository;

/**
 * Application service orchestrating vehicle use cases.
 *
 * <p>Owns all transactions. Delegates business rules to domain objects. Publishes domain events
 * after successful state transitions.
 */
@Service
@Transactional
public class VehicleApplicationService {

  private static final Logger log = LoggerFactory.getLogger(VehicleApplicationService.class);

  private final VehicleRepository vehicleRepository;
  private final OwnershipRecordRepository ownershipRecordRepository;
  private final ApplicationEventPublisher eventPublisher;

  public VehicleApplicationService(
      VehicleRepository vehicleRepository,
      OwnershipRecordRepository ownershipRecordRepository,
      ApplicationEventPublisher eventPublisher) {
    this.vehicleRepository = vehicleRepository;
    this.ownershipRecordRepository = ownershipRecordRepository;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Register a new vehicle for a customer.
   *
   * @param customerId the registering customer's ID (from JWT sub)
   * @param rawPlate raw registration plate input
   * @param rawRfid optional raw RFID number
   * @return the saved vehicle
   * @throws VehiclePlateConflictException if plate is already ACTIVE on the platform
   * @throws VehicleRfidConflictException if RFID is already globally assigned
   */
  public Vehicle registerVehicle(UUID customerId, String rawPlate, String rawRfid) {
    RegistrationPlate plate = RegistrationPlate.of(rawPlate);
    RfidNumber rfid = (rawRfid != null && !rawRfid.isBlank()) ? RfidNumber.of(rawRfid) : null;

    if (vehicleRepository.existsByPlateAndStatus(plate, VehicleStatus.ACTIVE)) {
      throw new VehiclePlateConflictException(plate.getValue());
    }
    if (rfid != null && vehicleRepository.existsByRfid(rfid)) {
      throw new VehicleRfidConflictException(rfid.getValue());
    }

    Instant now = Instant.now();
    Vehicle vehicle = Vehicle.register(plate, rfid, customerId, now);
    Vehicle saved = vehicleRepository.save(vehicle);

    OwnershipRecord ownership =
        OwnershipRecord.createActive(saved.getId().getValue(), customerId, now);
    ownershipRecordRepository.save(ownership);

    eventPublisher.publishEvent(
        new VehicleRegisteredEvent(
            saved.getId().getValue(),
            customerId,
            saved.getRegistrationPlate().getValue(),
            rfid != null ? rfid.getValue() : null,
            now));

    log.info(
        "Vehicle registered: vehicleId={}, plate={}, customerId={}",
        saved.getId(),
        plate,
        customerId);
    return saved;
  }

  /**
   * Associate an RFID tag with an existing ACTIVE vehicle.
   *
   * @param vehicleId target vehicle ID
   * @param customerId requesting customer (must be owner)
   * @param rawRfid RFID to associate
   * @return updated vehicle
   * @throws VehicleNotFoundException if not found
   * @throws VehicleNotOwnedException if customerId does not match current owner
   * @throws VehicleRfidConflictException if RFID already globally assigned
   */
  public Vehicle associateRfid(UUID vehicleId, UUID customerId, String rawRfid) {
    Vehicle vehicle = requireOwnedVehicle(vehicleId, customerId);
    RfidNumber rfid = RfidNumber.of(rawRfid);

    if (vehicleRepository.existsByRfid(rfid)) {
      throw new VehicleRfidConflictException(rfid.getValue());
    }

    vehicle.associateRfid(rfid);
    Vehicle saved = vehicleRepository.save(vehicle);

    Instant now = Instant.now();
    eventPublisher.publishEvent(new RfidAssociatedEvent(vehicleId, rfid.getValue(), now));

    log.info("RFID associated: vehicleId={}, rfid={}, customerId={}", vehicleId, rfid, customerId);
    return saved;
  }

  /**
   * De-list (soft-delete) a vehicle.
   *
   * @param vehicleId target vehicle ID
   * @param customerId requesting customer (must be owner)
   * @throws VehicleNotFoundException if not found
   * @throws VehicleNotOwnedException if customerId does not match current owner
   */
  public void delistVehicle(UUID vehicleId, UUID customerId) {
    Vehicle vehicle = requireOwnedVehicle(vehicleId, customerId);

    Instant now = Instant.now();
    vehicle.delist(now);
    vehicleRepository.save(vehicle);

    ownershipRecordRepository
        .findActiveByVehicleId(vehicleId)
        .ifPresent(
            record -> {
              record.close(now);
              ownershipRecordRepository.save(record);
            });

    eventPublisher.publishEvent(
        new VehicleDelistedEvent(
            vehicleId, customerId, vehicle.getRegistrationPlate().getValue(), now));

    log.info("Vehicle delisted: vehicleId={}, customerId={}", vehicleId, customerId);
  }

  // ── Query Methods ─────────────────────────────────────────────────────────

  /** List ACTIVE vehicles owned by a customer. */
  @Transactional(readOnly = true)
  public List<Vehicle> listMyVehicles(UUID customerId, int page, int size) {
    return vehicleRepository.findByOwnerAndStatus(customerId, VehicleStatus.ACTIVE, page, size);
  }

  /** Get a specific vehicle by ID, scoped to owner. */
  @Transactional(readOnly = true)
  public Optional<Vehicle> getMyVehicle(UUID vehicleId, UUID customerId) {
    return vehicleRepository
        .findById(VehicleId.of(vehicleId))
        .filter(v -> v.getCurrentOwnerId().equals(customerId));
  }

  /** Lookup vehicles by partial plate prefix (ILIKE), scoped to ACTIVE status. */
  @Transactional(readOnly = true)
  public List<Vehicle> lookupByPlatePrefix(String plateQuery, int page, int size) {
    RegistrationPlate plate = RegistrationPlate.of(plateQuery);
    return vehicleRepository.findByPlatePrefixAndStatus(plate, VehicleStatus.ACTIVE, page, size);
  }

  /** Lookup a vehicle by exact RFID (used internally by session management). */
  @Transactional(readOnly = true)
  public Optional<Vehicle> lookupByRfid(String rawRfid) {
    return vehicleRepository.findByRfid(RfidNumber.of(rawRfid));
  }

  // ── Admin Methods ─────────────────────────────────────────────────────────

  /** Admin: get any vehicle by ID regardless of owner. */
  @Transactional(readOnly = true)
  public Optional<Vehicle> adminGetVehicle(UUID vehicleId) {
    return vehicleRepository.findById(VehicleId.of(vehicleId));
  }

  /** Admin: get full ownership history for a vehicle. */
  @Transactional(readOnly = true)
  public List<OwnershipRecord> adminGetOwnershipHistory(UUID vehicleId) {
    return ownershipRecordRepository.findAllByVehicleId(vehicleId);
  }

  // ── Private Helpers ───────────────────────────────────────────────────────

  private Vehicle requireOwnedVehicle(UUID vehicleId, UUID customerId) {
    Vehicle vehicle =
        vehicleRepository
            .findById(VehicleId.of(vehicleId))
            .orElseThrow(() -> new VehicleNotFoundException(vehicleId));
    if (!vehicle.getCurrentOwnerId().equals(customerId)) {
      throw new VehicleNotOwnedException(vehicleId, customerId);
    }
    return vehicle;
  }
}
