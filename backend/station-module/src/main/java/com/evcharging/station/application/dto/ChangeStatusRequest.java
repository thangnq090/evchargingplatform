package com.evcharging.station.application.dto;

import jakarta.validation.constraints.NotNull;

import com.evcharging.station.domain.model.StationStatus;

/** Request to change station availability status. */
public record ChangeStatusRequest(@NotNull StationStatus status) {}
