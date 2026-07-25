package com.evcharging.billing.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
    UUID invoiceId,
    UUID sessionId,
    UUID customerId,
    UUID vendorId,
    String status,
    BigDecimal totalAmount,
    String currency,
    Instant createdAt,
    List<LineItemDto> lineItems
) {
  public record LineItemDto(
      String description,
      BigDecimal unitPrice,
      String currency,
      BigDecimal quantity,
      BigDecimal totalAmount
  ) {}
}
