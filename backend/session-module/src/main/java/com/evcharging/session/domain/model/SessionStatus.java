package com.evcharging.session.domain.model;

/** Business lifecycle status of a charging session. */
public enum SessionStatus {
  PENDING,
  CHARGING,
  COMPLETED,
  FAILED
}
