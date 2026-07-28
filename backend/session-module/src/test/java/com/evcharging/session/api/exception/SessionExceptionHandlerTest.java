package com.evcharging.session.api.exception;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.evcharging.shared.api.ApiResponse;

@DisplayName("SessionExceptionHandler")
class SessionExceptionHandlerTest {

  private SessionExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new SessionExceptionHandler();
  }

  @Nested
  @DisplayName("handleValidation")
  class HandleValidation {

    @Test
    @DisplayName("returns 400 with validation details")
    void shouldReturn400() {
      WebExchangeBindException ex = org.mockito.Mockito.mock(WebExchangeBindException.class);
      BindingResult bindingResult = org.mockito.Mockito.mock(BindingResult.class);
      FieldError fieldError = new FieldError("request", "stationId", "must not be null");

      org.mockito.BDDMockito.given(ex.getBindingResult()).willReturn(bindingResult);
      org.mockito.BDDMockito.given(bindingResult.getFieldErrors()).willReturn(List.of(fieldError));

      ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(ex);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().success()).isFalse();
      assertThat(response.getBody().error().code()).isEqualTo("VALIDATION_FAILED");
    }
  }

  @Nested
  @DisplayName("handleIllegalArgument")
  class HandleIllegalArgument {

    @Test
    @DisplayName("returns 422 with message")
    void shouldReturn422() {
      IllegalArgumentException ex = new IllegalArgumentException("Session not found");

      ResponseEntity<ApiResponse<Void>> response = handler.handleIllegalArgument(ex);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error().message()).isEqualTo("Session not found");
    }
  }

  @Nested
  @DisplayName("handleIllegalState")
  class HandleIllegalState {

    @Test
    @DisplayName("returns 422 with message")
    void shouldReturn422() {
      IllegalStateException ex = new IllegalStateException("Station not available");

      ResponseEntity<ApiResponse<Void>> response = handler.handleIllegalState(ex);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error().message()).isEqualTo("Station not available");
    }
  }

  @Nested
  @DisplayName("handleAccessDenied")
  class HandleAccessDenied {

    @Test
    @DisplayName("returns 403 with message")
    void shouldReturn403() {
      org.springframework.security.access.AccessDeniedException ex =
          new org.springframework.security.access.AccessDeniedException("Forbidden");

      ResponseEntity<ApiResponse<Void>> response = handler.handleAccessDenied(ex);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error().code()).isEqualTo("FORBIDDEN");
    }

    @Test
    @DisplayName("returns default message when null")
    void shouldReturnDefaultMessage() {
      org.springframework.security.access.AccessDeniedException ex =
          new org.springframework.security.access.AccessDeniedException(null);

      ResponseEntity<ApiResponse<Void>> response = handler.handleAccessDenied(ex);

      assertThat(response.getBody().error().message()).isEqualTo("Access denied");
    }
  }

  @Nested
  @DisplayName("handleGeneral")
  class HandleGeneral {

    @Test
    @DisplayName("returns 500 for unhandled exception")
    void shouldReturn500() {
      Exception ex = new RuntimeException("unexpected");

      ResponseEntity<ApiResponse<Void>> response = handler.handleGeneral(ex);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error().code()).isEqualTo("INTERNAL_ERROR");
    }
  }
}
