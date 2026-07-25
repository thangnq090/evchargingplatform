package com.evcharging.billing.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import com.evcharging.shared.kernel.Money;

/** Aggregate root representing a customer invoice for a completed session. */
public class Invoice {

  private final InvoiceId id;
  private final UUID sessionId;
  private final UUID customerId;
  private final UUID vendorId;
  private InvoiceStatus status;
  private final Money totalAmount;
  private final Instant createdAt;
  private final List<InvoiceLineItem> lineItems;

  public Invoice(
      InvoiceId id,
      UUID sessionId,
      UUID customerId,
      UUID vendorId,
      InvoiceStatus status,
      Money totalAmount,
      Instant createdAt,
      List<InvoiceLineItem> lineItems) {
    this.id = Objects.requireNonNull(id, "Invoice ID cannot be null");
    this.sessionId = Objects.requireNonNull(sessionId, "Session ID cannot be null");
    this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
    this.vendorId = Objects.requireNonNull(vendorId, "Vendor ID cannot be null");
    this.status = Objects.requireNonNull(status, "Status cannot be null");
    this.totalAmount = Objects.requireNonNull(totalAmount, "Total amount cannot be null");
    this.createdAt = Objects.requireNonNull(createdAt, "Created timestamp cannot be null");
    this.lineItems = new ArrayList<>(Objects.requireNonNull(lineItems, "Line items list cannot be null"));
    if (lineItems.isEmpty()) {
      throw new IllegalArgumentException("Invoice must have at least one line item");
    }
  }

  public static Invoice generate(
      UUID sessionId,
      UUID customerId,
      UUID vendorId,
      List<InvoiceLineItem> lineItems,
      Instant createdAt) {
    InvoiceId id = InvoiceId.generate();
    Money total = lineItems.stream()
        .map(InvoiceLineItem::getTotalAmount)
        .reduce(Money.zeroEur(), Money::add);

    return new Invoice(id, sessionId, customerId, vendorId, InvoiceStatus.PENDING, total, createdAt, lineItems);
  }

  public void markPaid() {
    if (this.status == InvoiceStatus.VOIDED) {
      throw new IllegalStateException("Cannot pay a voided invoice");
    }
    this.status = InvoiceStatus.PAID;
  }

  public void markVoided() {
    if (this.status == InvoiceStatus.PAID) {
      throw new IllegalStateException("Cannot void a paid invoice");
    }
    this.status = InvoiceStatus.VOIDED;
  }

  public InvoiceId getId() {
    return id;
  }

  public UUID getSessionId() {
    return sessionId;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public UUID getVendorId() {
    return vendorId;
  }

  public InvoiceStatus getStatus() {
    return status;
  }

  public Money getTotalAmount() {
    return totalAmount;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public List<InvoiceLineItem> getLineItems() {
    return Collections.unmodifiableList(lineItems);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Invoice invoice = (Invoice) o;
    return Objects.equals(id, invoice.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
