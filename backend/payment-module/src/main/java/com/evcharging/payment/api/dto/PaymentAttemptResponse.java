package com.evcharging.payment.api.dto;

import java.util.UUID;

public record PaymentAttemptResponse(
    UUID id, int attemptNumber, String status, String errorCode, String errorMessage) {}
