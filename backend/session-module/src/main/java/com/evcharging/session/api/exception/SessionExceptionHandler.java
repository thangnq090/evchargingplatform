package com.evcharging.session.api.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.evcharging.session.api.controller.SessionController;
import com.evcharging.shared.api.ApiResponse;

@RestControllerAdvice(assignableTypes = {SessionController.class})
public class SessionExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(SessionExceptionHandler.class);

  @ExceptionHandler(WebExchangeBindException.class)
  ResponseEntity<ApiResponse<Void>> handleValidation(WebExchangeBindException ex) {
    List<String> details =
        ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
    return ResponseEntity.badRequest()
        .body(ApiResponse.error("VALIDATION_FAILED", "Request validation failed", details));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(ApiResponse.error("UNPROCESSABLE_ENTITY", ex.getMessage()));
  }

  @ExceptionHandler(IllegalStateException.class)
  ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(ApiResponse.error("UNPROCESSABLE_ENTITY", ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
    log.error("Unhandled exception in session module", ex);
    return ResponseEntity.internalServerError()
        .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
  }
}
