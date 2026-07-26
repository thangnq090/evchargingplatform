package com.evcharging.vehicle.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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

@DisplayName("VehicleApplicationService")
@ExtendWith(MockitoExtension.class)
class VehicleApplicationServiceTest {

  @Mock VehicleRepository vehicleRepository;
  @Mock OwnershipRecordRepository ownershipRecordRepository;
  @Mock ApplicationEventPublisher eventPublisher;

  VehicleApplicationService service;

  UUID customerId = UUID.randomUUID();
  String plateStr = "ABC-1234";
  String rfidStr = "04A3B5C2D1E0";

  @BeforeEach
  void setUp() {
    service =
        new VehicleApplicationService(vehicleRepository, ownershipRecordRepository, eventPublisher);
  }

  @Nested
  @DisplayName("registerVehicle")
  class RegisterVehicle {

    @Test
    @DisplayName("registers vehicle successfully and publishes VehicleRegisteredEvent")
    void shouldRegisterVehicleSuccessfully() {
      // Given
      given(
              vehicleRepository.existsByPlateAndStatus(
                  any(RegistrationPlate.class), any(VehicleStatus.class)))
          .willReturn(false);
      given(vehicleRepository.existsByRfid(any(RfidNumber.class))).willReturn(false);
      given(vehicleRepository.save(any(Vehicle.class))).willAnswer(inv -> inv.getArgument(0));
      given(ownershipRecordRepository.save(any(OwnershipRecord.class)))
          .willAnswer(inv -> inv.getArgument(0));

      // When
      Vehicle result = service.registerVehicle(customerId, plateStr, rfidStr);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getRegistrationPlate().getValue()).isEqualTo("ABC-1234");
      assertThat(result.getRfidNumber().getValue()).isEqualTo(rfidStr);
      assertThat(result.getCurrentOwnerId()).isEqualTo(customerId);
      assertThat(result.getStatus()).isEqualTo(VehicleStatus.ACTIVE);

      verify(ownershipRecordRepository).save(any(OwnershipRecord.class));

      ArgumentCaptor<VehicleRegisteredEvent> captor =
          ArgumentCaptor.forClass(VehicleRegisteredEvent.class);
      verify(eventPublisher).publishEvent(captor.capture());
      assertThat(captor.getValue().registrationPlate()).isEqualTo("ABC-1234");
      assertThat(captor.getValue().rfidNumber()).isEqualTo(rfidStr);
    }

    @Test
    @DisplayName("throws VehiclePlateConflictException when active plate exists")
    void shouldThrowPlateConflict() {
      given(
              vehicleRepository.existsByPlateAndStatus(
                  any(RegistrationPlate.class), any(VehicleStatus.class)))
          .willReturn(true);

      assertThatThrownBy(() -> service.registerVehicle(customerId, plateStr, rfidStr))
          .isInstanceOf(VehiclePlateConflictException.class);

      verify(vehicleRepository, never()).save(any());
    }

    @Test
    @DisplayName("throws VehicleRfidConflictException when RFID is already assigned")
    void shouldThrowRfidConflict() {
      given(
              vehicleRepository.existsByPlateAndStatus(
                  any(RegistrationPlate.class), any(VehicleStatus.class)))
          .willReturn(false);
      given(vehicleRepository.existsByRfid(any(RfidNumber.class))).willReturn(true);

      assertThatThrownBy(() -> service.registerVehicle(customerId, plateStr, rfidStr))
          .isInstanceOf(VehicleRfidConflictException.class);

      verify(vehicleRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("associateRfid")
  class AssociateRfid {

    @Test
    @DisplayName("associates RFID and publishes RfidAssociatedEvent")
    void shouldAssociateRfid() {
      Vehicle vehicle =
          Vehicle.register(
              RegistrationPlate.of(plateStr), null, customerId, java.time.Instant.now());

      given(vehicleRepository.findById(any(VehicleId.class))).willReturn(Optional.of(vehicle));
      given(vehicleRepository.existsByRfid(any(RfidNumber.class))).willReturn(false);
      given(vehicleRepository.save(any(Vehicle.class))).willAnswer(inv -> inv.getArgument(0));

      Vehicle updated = service.associateRfid(vehicle.getId().getValue(), customerId, rfidStr);

      assertThat(updated.getRfidNumber().getValue()).isEqualTo(rfidStr);

      ArgumentCaptor<RfidAssociatedEvent> captor =
          ArgumentCaptor.forClass(RfidAssociatedEvent.class);
      verify(eventPublisher).publishEvent(captor.capture());
      assertThat(captor.getValue().rfidNumber()).isEqualTo(rfidStr);
    }

    @Test
    @DisplayName("throws VehicleNotOwnedException when customer is not the owner")
    void shouldThrowNotOwned() {
      Vehicle vehicle =
          Vehicle.register(
              RegistrationPlate.of(plateStr), null, customerId, java.time.Instant.now());
      UUID otherCustomer = UUID.randomUUID();

      given(vehicleRepository.findById(any(VehicleId.class))).willReturn(Optional.of(vehicle));

      assertThatThrownBy(
              () -> service.associateRfid(vehicle.getId().getValue(), otherCustomer, rfidStr))
          .isInstanceOf(VehicleNotOwnedException.class);
    }
  }

  @Nested
  @DisplayName("delistVehicle")
  class DelistVehicle {

    @Test
    @DisplayName("delists vehicle and closes active ownership record")
    void shouldDelistVehicle() {
      Vehicle vehicle =
          Vehicle.register(
              RegistrationPlate.of(plateStr), null, customerId, java.time.Instant.now());
      OwnershipRecord record =
          OwnershipRecord.createActive(
              vehicle.getId().getValue(), customerId, java.time.Instant.now());

      given(vehicleRepository.findById(any(VehicleId.class))).willReturn(Optional.of(vehicle));
      given(ownershipRecordRepository.findActiveByVehicleId(vehicle.getId().getValue()))
          .willReturn(Optional.of(record));

      service.delistVehicle(vehicle.getId().getValue(), customerId);

      assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.DE_LISTED);
      assertThat(record.isActive()).isFalse();

      verify(vehicleRepository).save(vehicle);
      verify(ownershipRecordRepository).save(record);

      ArgumentCaptor<VehicleDelistedEvent> captor =
          ArgumentCaptor.forClass(VehicleDelistedEvent.class);
      verify(eventPublisher).publishEvent(captor.capture());
      assertThat(captor.getValue().vehicleId()).isEqualTo(vehicle.getId().getValue());
    }
  }
}
