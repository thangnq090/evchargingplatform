package com.evcharging.billing.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.evcharging.billing.domain.model.InvoiceLineItem;
import com.evcharging.shared.kernel.Money;

@Entity
@Table(name = "invoice_line_items", schema = "billing")
public class InvoiceLineItemEntity {

  @Id
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "invoice_id", nullable = false)
  private InvoiceEntity invoice;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "unit_price_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal unitPriceAmount;

  @Column(name = "unit_price_currency", nullable = false, length = 3)
  private String unitPriceCurrency;

  @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
  private BigDecimal quantity;

  @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal totalAmount;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  public InvoiceLineItemEntity() {}

  public static InvoiceLineItemEntity fromDomain(InvoiceLineItem item, InvoiceEntity invoiceEntity) {
    InvoiceLineItemEntity entity = new InvoiceLineItemEntity();
    entity.id = UUID.randomUUID();
    entity.invoice = invoiceEntity;
    entity.description = item.getDescription();
    entity.unitPriceAmount = item.getUnitPrice().getAmountExact();
    entity.unitPriceCurrency = item.getUnitPrice().getCurrency().getCurrencyCode();
    entity.quantity = item.getQuantity();
    entity.totalAmount = item.getTotalAmount().getAmountExact();
    entity.currency = item.getTotalAmount().getCurrency().getCurrencyCode();
    return entity;
  }

  public InvoiceLineItem toDomain() {
    return new InvoiceLineItem(
        description,
        Money.of(unitPriceAmount, unitPriceCurrency),
        quantity
    );
  }

  // Getters and Setters
  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public InvoiceEntity getInvoice() { return invoice; }
  public void setInvoice(InvoiceEntity invoice) { this.invoice = invoice; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public BigDecimal getUnitPriceAmount() { return unitPriceAmount; }
  public void setUnitPriceAmount(BigDecimal unitPriceAmount) { this.unitPriceAmount = unitPriceAmount; }

  public String getUnitPriceCurrency() { return unitPriceCurrency; }
  public void setUnitPriceCurrency(String unitPriceCurrency) { this.unitPriceCurrency = unitPriceCurrency; }

  public BigDecimal getQuantity() { return quantity; }
  public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

  public BigDecimal getTotalAmount() { return totalAmount; }
  public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

  public String getCurrency() { return currency; }
  public void setCurrency(String currency) { this.currency = currency; }
}
