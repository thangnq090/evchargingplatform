package com.evcharging.vehicle.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.evcharging.vehicle.domain.model.RegistrationPlate;
import com.evcharging.vehicle.domain.model.RfidNumber;
import com.evcharging.vehicle.domain.model.Vehicle;
import com.evcharging.vehicle.domain.model.VehicleId;
import com.evcharging.vehicle.domain.model.VehicleStatus;
import com.evcharging.vehicle.domain.repository.VehicleRepository;
import com.evcharging.vehicle.infrastructure.persistence.entity.VehicleEntity;

/** Adapter implementing the VehicleRepository port using Spring Data JPA. */
@Repository
public class VehicleRepositoryAdapter implements VehicleRepository {

  private final JpaVehicleRepository jpaVehicleRepository;

  public VehicleRepositoryAdapter(JpaVehicleRepository jpaVehicleRepository) {
    this.jpaVehicleRepository = jpaVehicleRepository;
  }

  @Override
  public Vehicle save(Vehicle vehicle) {
    VehicleEntity entity = VehicleEntity.fromDomain(vehicle);
    VehicleEntity saved = jpaVehicleRepository.save(entity);
    return saved.toDomain();
  }

  @Override
  public Optional<Vehicle> findById(VehicleId id) {
    return jpaVehicleRepository.findById(id.getValue()).map(VehicleEntity::toDomain);
  }

  @Override
  public Optional<Vehicle> findByPlateAndStatus(RegistrationPlate plate, VehicleStatus status) {
    return jpaVehicleRepository
        .findByRegistrationPlateAndStatus(plate.getValue(), status.name())
        .map(VehicleEntity::toDomain);
  }

  @Override
  public Optional<Vehicle> findByRfid(RfidNumber rfid) {
    return jpaVehicleRepository.findByRfidNumber(rfid.getValue()).map(VehicleEntity::toDomain);
  }

  @Override
  public List<Vehicle> findByOwnerAndStatus(
      UUID customerId, VehicleStatus status, int page, int size) {
    return jpaVehicleRepository
        .findByCurrentOwnerIdAndStatus(customerId, status.name(), PageRequest.of(page, size))
        .getContent()
        .stream()
        .map(VehicleEntity::toDomain)
        .toList();
  }

  @Override
  public List<Vehicle> findByPlatePrefixAndStatus(
      RegistrationPlate plate, VehicleStatus status, int page, int size) {
    return jpaVehicleRepository
        .findByPlateLikeAndStatus(plate.getValue(), status.name(), PageRequest.of(page, size))
        .getContent()
        .stream()
        .map(VehicleEntity::toDomain)
        .toList();
  }

  @Override
  public boolean existsByPlateAndStatus(RegistrationPlate plate, VehicleStatus status) {
    return jpaVehicleRepository.existsByRegistrationPlateAndStatus(plate.getValue(), status.name());
  }

  @Override
  public boolean existsByRfid(RfidNumber rfid) {
    return jpaVehicleRepository.existsByRfidNumber(rfid.getValue());
  }
}
