package com.evcharging.identity.presentation;

import com.evcharging.shared.api.ApiResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

/** Global exception handler for the identity module WebFlux REST layer. */
@RestControllerAdvice(basePackages = "com.evcharging.identity.presentation")
class IdentityExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(IdentityExceptionHandler.class);

  @ExceptionHandler(WebExchangeBindException.class)
  ResponseEntity<ApiResponse<Void>> handleWebExchangeValidation(WebExchangeBindException ex) {
    List<String> details =
        ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .toList();
    return ResponseEntity.badRequest()
        .body(ApiResponse.error("VALIDATION_FAILED", "Request validation failed", details));
  }

  @ExceptionHandler(BadCredentialsException.class)
  ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error("UNAUTHORIZED", ex.getMessage()));
  }

  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException ex) {
    return ResponseEntity.status(ex.getStatusCode())
        .body(ApiResponse.error(ex.getStatusCode().toString(), ex.getReason() != null ? ex.getReason() : ex.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.badRequest()
        .body(ApiResponse.error("BAD_REQUEST", ex.getMessage()));
  }

  @ExceptionHandler(IllegalStateException.class)
  ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ApiResponse.error("CONFLICT", ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
    log.error("Unhandled exception in identity module", ex);
    return ResponseEntity.internalServerError()
        .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
  }
}
