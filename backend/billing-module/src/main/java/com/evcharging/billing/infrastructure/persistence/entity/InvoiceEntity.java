package com.evcharging.billing.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.evcharging.billing.domain.model.Invoice;
import com.evcharging.billing.domain.model.InvoiceId;
import com.evcharging.billing.domain.model.InvoiceStatus;
import com.evcharging.shared.kernel.Money;

@Entity
@Table(name = "invoices", schema = "billing")
public class InvoiceEntity {

  @Id
  private UUID id;

  @Column(name = "session_id", nullable = false, unique = true)
  private UUID sessionId;

  @Column(name = "customer_id", nullable = false)
  private UUID customerId;

  @Column(name = "vendor_id", nullable = false)
  private UUID vendorId;

  @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal totalAmount;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  private List<InvoiceLineItemEntity> lineItems = new ArrayList<>();

  @Version
  private int version;

  public InvoiceEntity() {}

  public static InvoiceEntity fromDomain(Invoice invoice) {
    InvoiceEntity entity = new InvoiceEntity();
    entity.id = invoice.getId().getValue();
    entity.sessionId = invoice.getSessionId();
    entity.customerId = invoice.getCustomerId();
    entity.vendorId = invoice.getVendorId();
    entity.totalAmount = invoice.getTotalAmount().getAmountExact();
    entity.currency = invoice.getTotalAmount().getCurrency().getCurrencyCode();
    entity.status = invoice.getStatus().name();
    entity.createdAt = invoice.getCreatedAt();
    entity.lineItems = invoice.getLineItems().stream()
        .map(item -> InvoiceLineItemEntity.fromDomain(item, entity))
        .collect(Collectors.toList());
    return entity;
  }

  public Invoice toDomain() {
    return new Invoice(
        InvoiceId.of(id),
        sessionId,
        customerId,
        vendorId,
        InvoiceStatus.valueOf(status),
        Money.of(totalAmount, currency),
        createdAt,
        lineItems.stream().map(InvoiceLineItemEntity::toDomain).collect(Collectors.toList())
    );
  }

  // Getters and Setters
  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public UUID getSessionId() { return sessionId; }
  public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

  public UUID getCustomerId() { return customerId; }
  public void setCustomerId(UUID customerId) { this.customerId = customerId; }

  public UUID getVendorId() { return vendorId; }
  public void setVendorId(UUID vendorId) { this.vendorId = vendorId; }

  public BigDecimal getTotalAmount() { return totalAmount; }
  public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

  public String getCurrency() { return currency; }
  public void setCurrency(String currency) { this.currency = currency; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

  public List<InvoiceLineItemEntity> getLineItems() { return lineItems; }
  public void setLineItems(List<InvoiceLineItemEntity> lineItems) { this.lineItems = lineItems; }

  public int getVersion() { return version; }
}
