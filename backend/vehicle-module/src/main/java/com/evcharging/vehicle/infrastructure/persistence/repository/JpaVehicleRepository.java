package com.evcharging.vehicle.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.evcharging.vehicle.infrastructure.persistence.entity.VehicleEntity;

/** Spring Data JPA repository for VehicleEntity. */
public interface JpaVehicleRepository extends JpaRepository<VehicleEntity, UUID> {

  Optional<VehicleEntity> findByRegistrationPlateAndStatus(String registrationPlate, String status);

  Optional<VehicleEntity> findByRfidNumber(String rfidNumber);

  Page<VehicleEntity> findByCurrentOwnerIdAndStatus(
      UUID currentOwnerId, String status, Pageable pageable);

  boolean existsByRegistrationPlateAndStatus(String registrationPlate, String status);

  boolean existsByRfidNumber(String rfidNumber);

  @Query(
      "SELECT v FROM VehicleEntity v "
          + "WHERE v.status = :status "
          + "AND UPPER(v.registrationPlate) LIKE UPPER(CONCAT(:platePrefix, '%'))")
  Page<VehicleEntity> findByPlateLikeAndStatus(
      @Param("platePrefix") String platePrefix, @Param("status") String status, Pageable pageable);
}
