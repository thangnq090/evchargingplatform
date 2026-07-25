package com.evcharging.billing.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.evcharging.billing.domain.model.Invoice;
import com.evcharging.billing.domain.model.InvoiceId;
import com.evcharging.billing.domain.repository.InvoiceRepository;
import com.evcharging.billing.infrastructure.persistence.entity.InvoiceEntity;

@Repository
public class InvoiceRepositoryAdapter implements InvoiceRepository {

  private final JpaInvoiceRepository jpaInvoiceRepository;

  public InvoiceRepositoryAdapter(JpaInvoiceRepository jpaInvoiceRepository) {
    this.jpaInvoiceRepository = jpaInvoiceRepository;
  }

  @Override
  public Invoice save(Invoice invoice) {
    InvoiceEntity entity = InvoiceEntity.fromDomain(invoice);
    InvoiceEntity saved = jpaInvoiceRepository.save(entity);
    return saved.toDomain();
  }

  @Override
  public Optional<Invoice> findById(InvoiceId id) {
    return jpaInvoiceRepository.findById(id.getValue()).map(InvoiceEntity::toDomain);
  }

  @Override
  public Optional<Invoice> findBySessionId(UUID sessionId) {
    return jpaInvoiceRepository.findBySessionId(sessionId).map(InvoiceEntity::toDomain);
  }

  @Override
  public List<Invoice> findByVendorIdAndCreatedAtBetween(UUID vendorId, Instant start, Instant end) {
    return jpaInvoiceRepository.findByVendorIdAndCreatedAtBetween(vendorId, start, end).stream()
        .map(InvoiceEntity::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Invoice> findAllByCreatedAtBetween(Instant start, Instant end) {
    return jpaInvoiceRepository.findAllByCreatedAtBetween(start, end).stream()
        .map(InvoiceEntity::toDomain)
        .collect(Collectors.toList());
  }
}
