package com.evcharging.session.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.evcharging.session.api.dto.SessionSearchResponse;
import com.evcharging.session.application.service.SessionApplicationService;
import com.evcharging.shared.api.ApiResponse;
import com.evcharging.shared.kernel.Money;

@ExtendWith(MockitoExtension.class)
class SessionSearchControllerTest {

  @Mock private SessionApplicationService service;

  private SessionController controller;

  @BeforeEach
  void setUp() {
    controller = new SessionController(service);
  }

  @Test
  @DisplayName("searchSessions should return search results successfully")
  void searchSessions_success() {
    UUID sessionId = UUID.randomUUID();
    UUID stationId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    UUID vehicleId = UUID.randomUUID();

    SessionSearchResponse searchResult =
        new SessionSearchResponse(
            sessionId,
            stationId,
            1,
            customerId,
            "CUST-1001",
            vehicleId,
            "AUD186",
            "COMPLETED",
            Instant.now(),
            Instant.now(),
            Money.of(new BigDecimal("0.3500"), "EUR"),
            new BigDecimal("25.5000"),
            Money.of(new BigDecimal("8.9250"), "EUR"),
            null,
            Instant.now());

    when(service.searchSessions(anyString())).thenReturn(List.of(searchResult));

    ResponseEntity<ApiResponse<List<SessionSearchResponse>>> response =
        controller.searchSessions("AUD").block();

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().data());
    assertEquals(1, response.getBody().data().size());
    assertEquals("AUD186", response.getBody().data().get(0).registrationPlate());
    assertEquals("CUST-1001", response.getBody().data().get(0).customerAccountNumber());
  }
}
