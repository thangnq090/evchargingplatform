package com.evcharging.billing.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.evcharging.billing.domain.model.BillingAccount;

/** Port/Repository interface for BillingAccount persistence. */
public interface BillingAccountRepository {

  BillingAccount save(BillingAccount account);

  Optional<BillingAccount> findByCustomerId(UUID customerId);
}
