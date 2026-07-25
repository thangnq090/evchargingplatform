package com.evcharging.billing.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evcharging.billing.application.dto.IncomeReportResponse;
import com.evcharging.billing.application.dto.InvoiceResponse;
import com.evcharging.billing.application.dto.InvoiceResponse.LineItemDto;
import com.evcharging.billing.domain.event.InvoiceGeneratedEvent;
import com.evcharging.billing.domain.model.BillingAccount;
import com.evcharging.billing.domain.model.Invoice;
import com.evcharging.billing.domain.model.InvoiceLineItem;
import com.evcharging.billing.domain.repository.BillingAccountRepository;
import com.evcharging.billing.domain.repository.InvoiceRepository;
import com.evcharging.billing.domain.service.CostCalculator;
import com.evcharging.identity.VendorMarkupApi;
import com.evcharging.session.SessionApi;
import com.evcharging.session.SessionApi.SessionDetails;
import com.evcharging.shared.kernel.MarkupPercentage;
import com.evcharging.shared.kernel.Money;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.station.StationApi;
import com.evcharging.station.StationApi.StationDetails;

@Service
@Transactional
public class BillingApplicationService {

  private final InvoiceRepository invoiceRepository;
  private final BillingAccountRepository billingAccountRepository;
  private final SessionApi sessionApi;
  private final StationApi stationApi;
  private final VendorMarkupApi vendorMarkupApi;
  private final ApplicationEventPublisher eventPublisher;
  private final CostCalculator costCalculator = new CostCalculator();

  public BillingApplicationService(
      InvoiceRepository invoiceRepository,
      BillingAccountRepository billingAccountRepository,
      SessionApi sessionApi,
      StationApi stationApi,
      VendorMarkupApi vendorMarkupApi,
      ApplicationEventPublisher eventPublisher) {
    this.invoiceRepository = invoiceRepository;
    this.billingAccountRepository = billingAccountRepository;
    this.sessionApi = sessionApi;
    this.stationApi = stationApi;
    this.vendorMarkupApi = vendorMarkupApi;
    this.eventPublisher = eventPublisher;
  }

  /** Generates an invoice for a completed charging session. Idempotent. */
  public Invoice generateInvoice(UUID sessionId) {
    // 1. Idempotency Check
    Optional<Invoice> existingInvoice = invoiceRepository.findBySessionId(sessionId);
    if (existingInvoice.isPresent()) {
      return existingInvoice.get();
    }

    // 2. Fetch Session Details
    SessionDetails session = sessionApi.getSessionDetails(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

    // 3. Fetch Station Details
    StationDetails station = stationApi.getStationDetails(StationId.of(session.stationId()));
    UUID vendorId = UUID.fromString(station.vendorId());

    // 4. Fetch Markup Percentage
    MarkupPercentage markup = vendorMarkupApi.getMarkup(vendorId).orElse(MarkupPercentage.zero());

    // 5. Calculate Line Items
    Money unitRate = Money.of(session.unitRateAmount(), session.unitRateCurrency());
    List<InvoiceLineItem> lineItems = costCalculator.calculateLineItems(
        session.totalEnergyKwh(),
        unitRate,
        markup
    );

    // 6. Create & Save Invoice
    Instant createdAt = Instant.now();
    Invoice invoice = Invoice.generate(
        sessionId,
        session.customerId(),
        vendorId,
        lineItems,
        createdAt
    );
    Invoice savedInvoice = invoiceRepository.save(invoice);

    // 7. Update/Create Billing Account
    BillingAccount billingAccount = billingAccountRepository.findByCustomerId(session.customerId())
        .orElseGet(() -> BillingAccount.createForCustomer(session.customerId()));
    billingAccount.billInvoice(savedInvoice.getTotalAmount(), createdAt);
    billingAccountRepository.save(billingAccount);

    // 8. Publish Domain Event
    eventPublisher.publishEvent(new InvoiceGeneratedEvent(
        savedInvoice.getId(),
        savedInvoice.getSessionId(),
        savedInvoice.getCustomerId(),
        savedInvoice.getVendorId(),
        savedInvoice.getTotalAmount(),
        savedInvoice.getCreatedAt()
    ));

    return savedInvoice;
  }

  /** Retrieves an invoice by session ID. */
  @Transactional(readOnly = true)
  public Optional<InvoiceResponse> getInvoiceBySessionId(UUID sessionId) {
    return invoiceRepository.findBySessionId(sessionId).map(this::mapToResponse);
  }

  /** Generates income report for administrators. */
  @Transactional(readOnly = true)
  public IncomeReportResponse getAdminIncomeReport(LocalDate startDate, LocalDate endDate, UUID vendorId) {
    Instant start = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
    // Use start of the *next* day as the exclusive upper bound so the full endDate
    // is covered regardless of sub-second precision or timezone drift.
    Instant end = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

    List<Invoice> invoices;
    if (vendorId != null) {
      invoices = invoiceRepository.findByVendorIdAndCreatedAtBetween(vendorId, start, end);
    } else {
      invoices = invoiceRepository.findAllByCreatedAtBetween(start, end);
    }

    BigDecimal totalRevenue = invoices.stream()
        .map(inv -> inv.getTotalAmount().getAmountExact())
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    int sessionCount = invoices.size();

    // Group breakdowns by vendor
    Map<UUID, List<Invoice>> grouped = invoices.stream()
        .collect(Collectors.groupingBy(Invoice::getVendorId));

    List<IncomeReportResponse.VendorBreakdownDto> breakdowns = new ArrayList<>();
    for (Map.Entry<UUID, List<Invoice>> entry : grouped.entrySet()) {
      UUID vId = entry.getKey();
      List<Invoice> vInvoices = entry.getValue();

      String vendorName = vendorMarkupApi.getVendorName(vId).orElse("Vendor " + vId);
      BigDecimal revenue = vInvoices.stream()
          .map(inv -> inv.getTotalAmount().getAmountExact())
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      breakdowns.add(new IncomeReportResponse.VendorBreakdownDto(
          vId,
          vendorName,
          revenue,
          vInvoices.size()
      ));
    }

    return new IncomeReportResponse(totalRevenue, sessionCount, breakdowns);
  }

  private InvoiceResponse mapToResponse(Invoice invoice) {
    List<LineItemDto> lineItems = invoice.getLineItems().stream()
        .map(item -> new LineItemDto(
            item.getDescription(),
            item.getUnitPrice().getAmountExact(),
            item.getUnitPrice().getCurrency().getCurrencyCode(),
            item.getQuantity(),
            item.getTotalAmount().getAmountExact()
        ))
        .collect(Collectors.toList());

    return new InvoiceResponse(
        invoice.getId().getValue(),
        invoice.getSessionId(),
        invoice.getCustomerId(),
        invoice.getVendorId(),
        invoice.getStatus().name(),
        invoice.getTotalAmount().getAmountExact(),
        invoice.getTotalAmount().getCurrency().getCurrencyCode(),
        invoice.getCreatedAt(),
        lineItems
    );
  }
}
