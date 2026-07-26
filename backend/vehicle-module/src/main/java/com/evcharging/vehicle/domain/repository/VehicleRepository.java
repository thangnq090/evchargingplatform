package com.evcharging.vehicle.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.evcharging.vehicle.domain.model.RegistrationPlate;
import com.evcharging.vehicle.domain.model.RfidNumber;
import com.evcharging.vehicle.domain.model.Vehicle;
import com.evcharging.vehicle.domain.model.VehicleId;
import com.evcharging.vehicle.domain.model.VehicleStatus;

/** Port (interface) for Vehicle persistence. Implemented by infrastructure adapter. */
public interface VehicleRepository {

  Vehicle save(Vehicle vehicle);

  Optional<Vehicle> findById(VehicleId id);

  Optional<Vehicle> findByPlateAndStatus(RegistrationPlate plate, VehicleStatus status);

  Optional<Vehicle> findByRfid(RfidNumber rfid);

  List<Vehicle> findByOwnerAndStatus(UUID customerId, VehicleStatus status, int page, int size);

  List<Vehicle> findByPlatePrefixAndStatus(
      RegistrationPlate plate, VehicleStatus status, int page, int size);

  boolean existsByPlateAndStatus(RegistrationPlate plate, VehicleStatus status);

  boolean existsByRfid(RfidNumber rfid);
}
