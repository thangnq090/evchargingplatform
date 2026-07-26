package com.evcharging.vehicle.application.service;

/** Thrown when an RFID number is already globally assigned to another vehicle. */
public class VehicleRfidConflictException extends RuntimeException {

  public VehicleRfidConflictException(String rfid) {
    super("RFID '" + rfid + "' is already assigned to another vehicle");
  }
}
