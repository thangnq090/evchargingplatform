package com.evcharging.billing.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evcharging.billing.domain.model.*;
import com.evcharging.billing.infrastructure.persistence.entity.InvoiceEntity;
import com.evcharging.shared.kernel.Money;

@DisplayName("InvoiceRepositoryAdapter")
@ExtendWith(MockitoExtension.class)
class InvoiceRepositoryAdapterTest {

  @Mock private JpaInvoiceRepository jpa;

  private InvoiceRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new InvoiceRepositoryAdapter(jpa);
  }

  private Invoice createInvoice() {
    InvoiceLineItem item = new InvoiceLineItem("Fee", Money.of(new BigDecimal("0.25"), "EUR"), new BigDecimal("10.0"));
    return Invoice.generate(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), List.of(item), Instant.now());
  }

  @Nested
  @DisplayName("save")
  class Save {

    @Test
    @DisplayName("saves invoice")
    void shouldSaveInvoice() {
      Invoice invoice = createInvoice();
      given(jpa.save(any(InvoiceEntity.class))).willAnswer(inv -> inv.getArgument(0));

      Invoice result = adapter.save(invoice);

      assertThat(result).isNotNull();
      assertThat(result.getSessionId()).isEqualTo(invoice.getSessionId());
    }
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("returns invoice when found")
    void shouldReturnInvoice() {
      Invoice invoice = createInvoice();
      InvoiceEntity entity = InvoiceEntity.fromDomain(invoice);
      given(jpa.findById(invoice.getId().getValue())).willReturn(Optional.of(entity));

      Optional<Invoice> result = adapter.findById(invoice.getId());
      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(invoice.getId());
    }

    @Test
    @DisplayName("returns empty when not found")
    void shouldReturnEmpty() {
      given(jpa.findById(any(UUID.class))).willReturn(Optional.empty());
      assertThat(adapter.findById(InvoiceId.generate())).isEmpty();
    }
  }

  @Nested
  @DisplayName("findBySessionId")
  class FindBySessionId {

    @Test
    @DisplayName("returns invoice")
    void shouldReturnInvoice() {
      Invoice invoice = createInvoice();
      InvoiceEntity entity = InvoiceEntity.fromDomain(invoice);
      given(jpa.findBySessionId(invoice.getSessionId())).willReturn(Optional.of(entity));

      Optional<Invoice> result = adapter.findBySessionId(invoice.getSessionId());
      assertThat(result).isPresent();
    }

    @Test
    @DisplayName("returns empty")
    void shouldReturnEmpty() {
      given(jpa.findBySessionId(any(UUID.class))).willReturn(Optional.empty());
      assertThat(adapter.findBySessionId(UUID.randomUUID())).isEmpty();
    }
  }

  @Nested
  @DisplayName("findByVendorIdAndCreatedAtBetween")
  class FindByVendorIdAndCreatedAtBetween {

    @Test
    @DisplayName("returns invoices")
    void shouldReturnInvoices() {
      Invoice invoice = createInvoice();
      InvoiceEntity entity = InvoiceEntity.fromDomain(invoice);
      given(jpa.findByVendorIdAndCreatedAtBetween(any(), any(), any()))
          .willReturn(List.of(entity));

      List<Invoice> result = adapter.findByVendorIdAndCreatedAtBetween(
          UUID.randomUUID(), Instant.now().minusSeconds(3600), Instant.now());
      assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("returns empty list")
    void shouldReturnEmptyList() {
      given(jpa.findByVendorIdAndCreatedAtBetween(any(), any(), any())).willReturn(List.of());

      List<Invoice> result = adapter.findByVendorIdAndCreatedAtBetween(
          UUID.randomUUID(), Instant.now().minusSeconds(3600), Instant.now());
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("findAllByCreatedAtBetween")
  class FindAllByCreatedAtBetween {

    @Test
    @DisplayName("returns invoices")
    void shouldReturnInvoices() {
      Invoice invoice = createInvoice();
      InvoiceEntity entity = InvoiceEntity.fromDomain(invoice);
      given(jpa.findAllByCreatedAtBetween(any(), any())).willReturn(List.of(entity));

      List<Invoice> result = adapter.findAllByCreatedAtBetween(
          Instant.now().minusSeconds(3600), Instant.now());
      assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("returns empty list")
    void shouldReturnEmptyList() {
      given(jpa.findAllByCreatedAtBetween(any(), any())).willReturn(List.of());

      List<Invoice> result = adapter.findAllByCreatedAtBetween(
          Instant.now().minusSeconds(3600), Instant.now());
      assertThat(result).isEmpty();
    }
  }
}
