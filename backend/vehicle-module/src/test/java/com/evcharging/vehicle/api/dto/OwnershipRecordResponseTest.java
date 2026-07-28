package com.evcharging.vehicle.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.evcharging.vehicle.domain.model.OwnershipRecord;

@DisplayName("OwnershipRecordResponse")
class OwnershipRecordResponseTest {

  @Test
  @DisplayName("from creates response from record")
  void shouldCreateFromRecord() {
    UUID vehicleId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    Instant start = Instant.now().minusSeconds(86400);
    Instant end = Instant.now();
    OwnershipRecord record = new OwnershipRecord(UUID.randomUUID(), vehicleId, customerId, start, end);

    OwnershipRecordResponse response = OwnershipRecordResponse.from(record);

    assertThat(response.id()).isEqualTo(record.getId());
    assertThat(response.vehicleId()).isEqualTo(vehicleId);
    assertThat(response.customerId()).isEqualTo(customerId);
    assertThat(response.startDate()).isEqualTo(start);
    assertThat(response.endDate()).isEqualTo(end);
  }

  @Test
  @DisplayName("from creates response for active record")
  void shouldCreateActiveRecord() {
    OwnershipRecord record = OwnershipRecord.createActive(UUID.randomUUID(), UUID.randomUUID(), Instant.now());

    OwnershipRecordResponse response = OwnershipRecordResponse.from(record);

    assertThat(response.endDate()).isNull();
  }
}
