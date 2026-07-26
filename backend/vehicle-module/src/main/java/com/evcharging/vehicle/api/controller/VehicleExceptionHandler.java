package com.evcharging.vehicle.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.evcharging.shared.api.ApiResponse;
import com.evcharging.vehicle.application.service.VehicleNotFoundException;
import com.evcharging.vehicle.application.service.VehicleNotOwnedException;
import com.evcharging.vehicle.application.service.VehiclePlateConflictException;
import com.evcharging.vehicle.application.service.VehicleRfidConflictException;

/** Maps vehicle domain exceptions to RFC 7807-aligned API error responses. */
@RestControllerAdvice(basePackages = "com.evcharging.vehicle")
public class VehicleExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(VehicleExceptionHandler.class);

  @ExceptionHandler(VehicleNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNotFound(VehicleNotFoundException ex) {
    log.debug("Vehicle not found: {}", ex.getVehicleId());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error("VEHICLE_NOT_FOUND", ex.getMessage()));
  }

  @ExceptionHandler(VehicleNotOwnedException.class)
  public ResponseEntity<ApiResponse<Void>> handleNotOwned(VehicleNotOwnedException ex) {
    log.warn("Vehicle ownership violation: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ApiResponse.error("VEHICLE_NOT_OWNED", ex.getMessage()));
  }

  @ExceptionHandler(VehiclePlateConflictException.class)
  public ResponseEntity<ApiResponse<Void>> handlePlateConflict(VehiclePlateConflictException ex) {
    log.info("Plate conflict: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ApiResponse.error("VEHICLE_PLATE_CONFLICT", ex.getMessage()));
  }

  @ExceptionHandler(VehicleRfidConflictException.class)
  public ResponseEntity<ApiResponse<Void>> handleRfidConflict(VehicleRfidConflictException ex) {
    log.info("RFID conflict: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ApiResponse.error("VEHICLE_RFID_CONFLICT", ex.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidation(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error("VALIDATION_ERROR", ex.getMessage()));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusinessRule(IllegalStateException ex) {
    log.warn("Business rule violation: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(ApiResponse.error("BUSINESS_RULE_VIOLATION", ex.getMessage()));
  }
}
