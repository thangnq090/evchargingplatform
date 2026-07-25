package com.evcharging.billing.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.evcharging.billing.domain.model.Invoice;
import com.evcharging.billing.domain.model.InvoiceId;

/** Port/Repository interface for Invoice persistence. */
public interface InvoiceRepository {

  Invoice save(Invoice invoice);

  Optional<Invoice> findById(InvoiceId id);

  Optional<Invoice> findBySessionId(UUID sessionId);

  List<Invoice> findByVendorIdAndCreatedAtBetween(UUID vendorId, Instant start, Instant end);

  List<Invoice> findAllByCreatedAtBetween(Instant start, Instant end);
}
