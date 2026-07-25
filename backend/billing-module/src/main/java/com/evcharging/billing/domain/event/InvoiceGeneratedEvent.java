package com.evcharging.billing.domain.event;

import java.time.Instant;
import java.util.UUID;
import com.evcharging.billing.domain.model.InvoiceId;
import com.evcharging.shared.kernel.Money;

/** Event published when an invoice is successfully generated. */
public record InvoiceGeneratedEvent(
    InvoiceId invoiceId,
    UUID sessionId,
    UUID customerId,
    UUID vendorId,
    Money totalAmount,
    Instant createdAt
) {}
