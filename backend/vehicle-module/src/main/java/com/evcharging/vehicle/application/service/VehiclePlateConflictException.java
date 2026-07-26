package com.evcharging.vehicle.application.service;

/** Thrown when a registration plate is already registered and ACTIVE on the platform. */
public class VehiclePlateConflictException extends RuntimeException {

  public VehiclePlateConflictException(String plate) {
    super("Registration plate '" + plate + "' is already registered and active");
  }
}
