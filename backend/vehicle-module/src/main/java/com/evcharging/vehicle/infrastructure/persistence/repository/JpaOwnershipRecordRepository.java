package com.evcharging.vehicle.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.evcharging.vehicle.infrastructure.persistence.entity.OwnershipRecordEntity;

/** Spring Data JPA repository for OwnershipRecordEntity. */
public interface JpaOwnershipRecordRepository extends JpaRepository<OwnershipRecordEntity, UUID> {

  @Query(
      "SELECT o FROM OwnershipRecordEntity o "
          + "WHERE o.vehicleId = :vehicleId AND o.endDate IS NULL")
  Optional<OwnershipRecordEntity> findActiveByVehicleId(@Param("vehicleId") UUID vehicleId);

  List<OwnershipRecordEntity> findAllByVehicleIdOrderByStartDateDesc(UUID vehicleId);
}
