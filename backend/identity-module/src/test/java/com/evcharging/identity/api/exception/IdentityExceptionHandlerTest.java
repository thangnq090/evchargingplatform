package com.evcharging.identity.api.exception;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

import com.evcharging.shared.api.ApiResponse;

@DisplayName("IdentityExceptionHandler")
class IdentityExceptionHandlerTest {

  private IdentityExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new IdentityExceptionHandler();
  }

  @Nested
  @DisplayName("handleWebExchangeValidation")
  class HandleValidation {

    @Test
    @DisplayName("returns 400 with validation details")
    void shouldReturn400() {
      WebExchangeBindException ex = org.mockito.Mockito.mock(WebExchangeBindException.class);
      BindingResult bindingResult = org.mockito.Mockito.mock(BindingResult.class);
      FieldError fieldError = new FieldError("request", "email", "must not be blank");

      org.mockito.BDDMockito.given(ex.getBindingResult()).willReturn(bindingResult);
      org.mockito.BDDMockito.given(bindingResult.getFieldErrors()).willReturn(List.of(fieldError));

      ResponseEntity<ApiResponse<Void>> response = handler.handleWebExchangeValidation(ex);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().success()).isFalse();
      assertThat(response.getBody().error().code()).isEqualTo("VALIDATION_FAILED");
    }
  }

  @Nested
  @DisplayName("handleBadCredentials")
  class HandleBadCredentials {

    @Test
    @DisplayName("returns 401")
    void shouldReturn401() {
      BadCredentialsException ex = new BadCredentialsException("Invalid credentials");

      ResponseEntity<ApiResponse<Void>> response = handler.handleBadCredentials(ex);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error().code()).isEqualTo("UNAUTHORIZED");
    }
  }

  @Nested
  @DisplayName("handleResponseStatus")
  class HandleResponseStatus {

    @Test
    @DisplayName("returns status from exception")
    void shouldReturnStatus() {
      ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");

      ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatus(ex);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error().message()).isEqualTo("User not found");
    }

    @Test
    @DisplayName("uses message when reason is null")
    void shouldUseMessageWhenReasonNull() {
      ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST);

      ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatus(ex);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
  }

  @Nested
  @DisplayName("handleIllegalArgument")
  class HandleIllegalArgument {

    @Test
    @DisplayName("returns 400")
    void shouldReturn400() {
      IllegalArgumentException ex = new IllegalArgumentException("Invalid input");

      ResponseEntity<ApiResponse<Void>> response = handler.handleIllegalArgument(ex);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error().message()).isEqualTo("Invalid input");
    }
  }

  @Nested
  @DisplayName("handleIllegalState")
  class HandleIllegalState {

    @Test
    @DisplayName("returns 409")
    void shouldReturn409() {
      IllegalStateException ex = new IllegalStateException("Conflict");

      ResponseEntity<ApiResponse<Void>> response = handler.handleIllegalState(ex);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error().code()).isEqualTo("CONFLICT");
    }
  }

  @Nested
  @DisplayName("handleGeneral")
  class HandleGeneral {

    @Test
    @DisplayName("returns 500")
    void shouldReturn500() {
      Exception ex = new RuntimeException("unexpected");

      ResponseEntity<ApiResponse<Void>> response = handler.handleGeneral(ex);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error().code()).isEqualTo("INTERNAL_ERROR");
    }
  }
}
