package com.evcharging.vehicle.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.evcharging.shared.api.ApiResponse;
import com.evcharging.vehicle.application.service.VehicleNotFoundException;
import com.evcharging.vehicle.application.service.VehicleNotOwnedException;
import com.evcharging.vehicle.application.service.VehiclePlateConflictException;
import com.evcharging.vehicle.application.service.VehicleRfidConflictException;

import java.util.UUID;

@DisplayName("VehicleExceptionHandler")
class VehicleExceptionHandlerTest {

  private VehicleExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new VehicleExceptionHandler();
  }

  @Nested
  @DisplayName("handleNotFound")
  class HandleNotFound {

    @Test
    @DisplayName("returns 404 with error code")
    void shouldReturn404() {
      UUID vehicleId = UUID.randomUUID();
      VehicleNotFoundException ex = new VehicleNotFoundException(vehicleId);

      ResponseEntity<ApiResponse<Void>> response = handler.handleNotFound(ex);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error()).isNotNull();
      assertThat(response.getBody().error().code()).isEqualTo("VEHICLE_NOT_FOUND");
    }
  }

  @Nested
  @DisplayName("handleNotOwned")
  class HandleNotOwned {

    @Test
    @DisplayName("returns 403 with error code")
    void shouldReturn403() {
      VehicleNotOwnedException ex = new VehicleNotOwnedException(UUID.randomUUID(), UUID.randomUUID());

      ResponseEntity<ApiResponse<Void>> response = handler.handleNotOwned(ex);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
      assertThat(response.getBody().error().code()).isEqualTo("VEHICLE_NOT_OWNED");
    }
  }

  @Nested
  @DisplayName("handlePlateConflict")
  class HandlePlateConflict {

    @Test
    @DisplayName("returns 409 with error code")
    void shouldReturn409() {
      VehiclePlateConflictException ex = new VehiclePlateConflictException("ABC-123");

      ResponseEntity<ApiResponse<Void>> response = handler.handlePlateConflict(ex);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
      assertThat(response.getBody().error().code()).isEqualTo("VEHICLE_PLATE_CONFLICT");
    }
  }

  @Nested
  @DisplayName("handleRfidConflict")
  class HandleRfidConflict {

    @Test
    @DisplayName("returns 409 with error code")
    void shouldReturn409() {
      VehicleRfidConflictException ex = new VehicleRfidConflictException("RFID-001");

      ResponseEntity<ApiResponse<Void>> response = handler.handleRfidConflict(ex);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
      assertThat(response.getBody().error().code()).isEqualTo("VEHICLE_RFID_CONFLICT");
    }
  }

  @Nested
  @DisplayName("handleValidation")
  class HandleValidation {

    @Test
    @DisplayName("returns 400 with validation error")
    void shouldReturn400() {
      IllegalArgumentException ex = new IllegalArgumentException("Invalid input");

      ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(ex);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody().error().code()).isEqualTo("VALIDATION_ERROR");
    }
  }

  @Nested
  @DisplayName("handleBusinessRule")
  class HandleBusinessRule {

    @Test
    @DisplayName("returns 422 with business rule violation")
    void shouldReturn422() {
      IllegalStateException ex = new IllegalStateException("Business rule violated");

      ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessRule(ex);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
      assertThat(response.getBody().error().code()).isEqualTo("BUSINESS_RULE_VIOLATION");
    }
  }
}
