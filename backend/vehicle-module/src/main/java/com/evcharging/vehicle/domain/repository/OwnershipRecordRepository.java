package com.evcharging.vehicle.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.evcharging.vehicle.domain.model.OwnershipRecord;

/** Port (interface) for OwnershipRecord persistence. Implemented by infrastructure adapter. */
public interface OwnershipRecordRepository {

  OwnershipRecord save(OwnershipRecord record);

  Optional<OwnershipRecord> findActiveByVehicleId(UUID vehicleId);

  List<OwnershipRecord> findAllByVehicleId(UUID vehicleId);
}
