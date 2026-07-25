package com.evcharging.billing.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.evcharging.billing.domain.model.BillingAccount;
import com.evcharging.billing.domain.repository.BillingAccountRepository;
import com.evcharging.billing.infrastructure.persistence.entity.BillingAccountEntity;

@Repository
public class BillingAccountRepositoryAdapter implements BillingAccountRepository {

  private final JpaBillingAccountRepository jpaBillingAccountRepository;

  public BillingAccountRepositoryAdapter(JpaBillingAccountRepository jpaBillingAccountRepository) {
    this.jpaBillingAccountRepository = jpaBillingAccountRepository;
  }

  @Override
  public BillingAccount save(BillingAccount account) {
    BillingAccountEntity entity = BillingAccountEntity.fromDomain(account);
    BillingAccountEntity saved = jpaBillingAccountRepository.save(entity);
    return saved.toDomain();
  }

  @Override
  public Optional<BillingAccount> findByCustomerId(UUID customerId) {
    return jpaBillingAccountRepository.findByCustomerId(customerId).map(BillingAccountEntity::toDomain);
  }
}
