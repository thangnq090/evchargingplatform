package com.evcharging.vehicle.infrastructure.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.vehicle.domain.model.OwnershipRecord;

@DisplayName("OwnershipRecordEntity")
class OwnershipRecordEntityTest {

  @Nested
  @DisplayName("fromDomain")
  class FromDomain {

    @Test
    @DisplayName("converts active record (null endDate)")
    void shouldConvertActiveRecord() {
      UUID vehicleId = UUID.randomUUID();
      UUID customerId = UUID.randomUUID();
      Instant start = Instant.now();
      OwnershipRecord record = OwnershipRecord.createActive(vehicleId, customerId, start);

      OwnershipRecordEntity entity = OwnershipRecordEntity.fromDomain(record);

      assertThat(entity.getId()).isEqualTo(record.getId());
      assertThat(entity.getVehicleId()).isEqualTo(vehicleId);
      assertThat(entity.getCustomerId()).isEqualTo(customerId);
      assertThat(entity.getStartDate()).isEqualTo(start);
      assertThat(entity.getEndDate()).isNull();
    }

    @Test
    @DisplayName("converts closed record")
    void shouldConvertClosedRecord() {
      UUID vehicleId = UUID.randomUUID();
      UUID customerId = UUID.randomUUID();
      Instant start = Instant.now().minusSeconds(86400);
      Instant end = Instant.now();
      OwnershipRecord record =
          new OwnershipRecord(UUID.randomUUID(), vehicleId, customerId, start, end);

      OwnershipRecordEntity entity = OwnershipRecordEntity.fromDomain(record);

      assertThat(entity.getEndDate()).isEqualTo(end);
    }
  }

  @Nested
  @DisplayName("toDomain")
  class ToDomain {

    @Test
    @DisplayName("round-trips domain to entity and back")
    void shouldRoundTrip() {
      UUID id = UUID.randomUUID();
      UUID vehicleId = UUID.randomUUID();
      UUID customerId = UUID.randomUUID();
      Instant start = Instant.now().minusSeconds(86400);
      Instant end = Instant.now();
      OwnershipRecord record = new OwnershipRecord(id, vehicleId, customerId, start, end);

      OwnershipRecordEntity entity = OwnershipRecordEntity.fromDomain(record);
      OwnershipRecord domain = entity.toDomain();

      assertThat(domain.getId()).isEqualTo(id);
      assertThat(domain.getVehicleId()).isEqualTo(vehicleId);
      assertThat(domain.getCustomerId()).isEqualTo(customerId);
      assertThat(domain.getStartDate()).isEqualTo(start);
      assertThat(domain.getEndDate()).isEqualTo(end);
    }
  }

  @Nested
  @DisplayName("setters")
  class Setters {

    @Test
    @DisplayName("sets all fields via setters")
    void shouldSetAllFields() {
      OwnershipRecordEntity entity = new OwnershipRecordEntity();
      UUID id = UUID.randomUUID();
      entity.setId(id);
      entity.setVehicleId(UUID.randomUUID());
      entity.setCustomerId(UUID.randomUUID());
      entity.setStartDate(Instant.now());
      entity.setEndDate(Instant.now());

      assertThat(entity.getId()).isEqualTo(id);
      assertThat(entity.getVehicleId()).isNotNull();
      assertThat(entity.getCustomerId()).isNotNull();
      assertThat(entity.getStartDate()).isNotNull();
      assertThat(entity.getEndDate()).isNotNull();
    }
  }
}
