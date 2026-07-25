package com.evcharging.billing.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.evcharging.billing.infrastructure.persistence.entity.BillingAccountEntity;

@Repository
interface JpaBillingAccountRepository extends JpaRepository<BillingAccountEntity, UUID> {

  Optional<BillingAccountEntity> findByCustomerId(UUID customerId);
}
