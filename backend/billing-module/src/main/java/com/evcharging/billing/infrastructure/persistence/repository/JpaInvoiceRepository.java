package com.evcharging.billing.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.evcharging.billing.infrastructure.persistence.entity.InvoiceEntity;

@Repository
interface JpaInvoiceRepository extends JpaRepository<InvoiceEntity, UUID> {

  Optional<InvoiceEntity> findBySessionId(UUID sessionId);

  List<InvoiceEntity> findByVendorIdAndCreatedAtBetween(UUID vendorId, Instant start, Instant end);

  List<InvoiceEntity> findAllByCreatedAtBetween(Instant start, Instant end);
}
