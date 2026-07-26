package com.evcharging.payment.api.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.evcharging.payment.domain.model.Payment;
import com.evcharging.payment.domain.port.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

  private MockMvc mockMvc;

  @Mock private PaymentRepository paymentRepository;

  @InjectMocks private PaymentController paymentController;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
  }

  @Test
  void getPaymentBySessionId_found() throws Exception {
    UUID sessionId = UUID.randomUUID();
    Payment payment =
        new Payment(
            UUID.randomUUID(),
            sessionId,
            UUID.randomUUID(),
            null,
            null,
            new BigDecimal("15.00"),
            "USD",
            null,
            "session:" + sessionId + ":charge",
            Instant.now(),
            Instant.now());
    payment.markAuthorized("mock_auth_123");
    payment.markCaptured();

    given(paymentRepository.findBySessionId(sessionId)).willReturn(Optional.of(payment));

    mockMvc
        .perform(
            get("/api/v1/payments/session/{sessionId}", sessionId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.sessionId").value(sessionId.toString()))
        .andExpect(jsonPath("$.data.status").value("CAPTURED"))
        .andExpect(jsonPath("$.data.amount").value("15.00"));
  }

  @Test
  void getPaymentBySessionId_notFound() throws Exception {
    UUID sessionId = UUID.randomUUID();
    given(paymentRepository.findBySessionId(sessionId)).willReturn(Optional.empty());

    mockMvc
        .perform(
            get("/api/v1/payments/session/{sessionId}", sessionId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}
