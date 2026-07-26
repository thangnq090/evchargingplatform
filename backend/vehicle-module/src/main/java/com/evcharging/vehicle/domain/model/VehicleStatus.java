package com.evcharging.vehicle.domain.model;

/** Vehicle lifecycle status. */
public enum VehicleStatus {
  /** Vehicle is active and visible to its owner. */
  ACTIVE,

  /** Vehicle has been soft-deleted. Plate is eligible for re-registration. */
  DE_LISTED
}
