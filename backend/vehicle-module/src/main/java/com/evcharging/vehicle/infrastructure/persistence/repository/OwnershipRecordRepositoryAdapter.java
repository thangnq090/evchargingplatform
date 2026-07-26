package com.evcharging.vehicle.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.evcharging.vehicle.domain.model.OwnershipRecord;
import com.evcharging.vehicle.domain.repository.OwnershipRecordRepository;
import com.evcharging.vehicle.infrastructure.persistence.entity.OwnershipRecordEntity;

/** Adapter implementing the OwnershipRecordRepository port using Spring Data JPA. */
@Repository
public class OwnershipRecordRepositoryAdapter implements OwnershipRecordRepository {

  private final JpaOwnershipRecordRepository jpaOwnershipRecordRepository;

  public OwnershipRecordRepositoryAdapter(
      JpaOwnershipRecordRepository jpaOwnershipRecordRepository) {
    this.jpaOwnershipRecordRepository = jpaOwnershipRecordRepository;
  }

  @Override
  public OwnershipRecord save(OwnershipRecord record) {
    OwnershipRecordEntity entity = OwnershipRecordEntity.fromDomain(record);
    OwnershipRecordEntity saved = jpaOwnershipRecordRepository.save(entity);
    return saved.toDomain();
  }

  @Override
  public Optional<OwnershipRecord> findActiveByVehicleId(UUID vehicleId) {
    return jpaOwnershipRecordRepository
        .findActiveByVehicleId(vehicleId)
        .map(OwnershipRecordEntity::toDomain);
  }

  @Override
  public List<OwnershipRecord> findAllByVehicleId(UUID vehicleId) {
    return jpaOwnershipRecordRepository.findAllByVehicleIdOrderByStartDateDesc(vehicleId).stream()
        .map(OwnershipRecordEntity::toDomain)
        .toList();
  }
}
