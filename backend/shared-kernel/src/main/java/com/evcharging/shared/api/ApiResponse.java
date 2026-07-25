package com.evcharging.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

/**
 * Standard API response envelope used by all REST endpoints.
 *
 * <pre>
 * Success: { "success": true,  "data": {...},  "error": null, "timestamp": "..." }
 * Error:   { "success": false, "data": null,   "error": {...}, "timestamp": "..." }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, ApiError error, Instant timestamp) {

  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(true, data, null, Instant.now());
  }

  public static <T> ApiResponse<T> error(String code, String message) {
    return new ApiResponse<>(false, null, new ApiError(code, message, List.of()), Instant.now());
  }

  public static <T> ApiResponse<T> error(String code, String message, List<String> details) {
    return new ApiResponse<>(false, null, new ApiError(code, message, details), Instant.now());
  }

  /** Structured error payload embedded in an error response. */
  public record ApiError(String code, String message, List<String> details) {}
}
