package com.evcharging.vehicle.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;

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

import com.evcharging.vehicle.domain.model.OwnershipRecord;
import com.evcharging.vehicle.infrastructure.persistence.entity.OwnershipRecordEntity;

@DisplayName("OwnershipRecordRepositoryAdapter")
@ExtendWith(MockitoExtension.class)
class OwnershipRecordRepositoryAdapterTest {

  @Mock private JpaOwnershipRecordRepository jpa;

  private OwnershipRecordRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new OwnershipRecordRepositoryAdapter(jpa);
  }

  private OwnershipRecord createRecord() {
    return OwnershipRecord.createActive(UUID.randomUUID(), UUID.randomUUID(), Instant.now());
  }

  private OwnershipRecordEntity createEntity() {
    return OwnershipRecordEntity.fromDomain(createRecord());
  }

  @Nested
  @DisplayName("save")
  class Save {

    @Test
    @DisplayName("saves record")
    void shouldSaveRecord() {
      OwnershipRecord record = createRecord();
      given(jpa.save(any(OwnershipRecordEntity.class))).willAnswer(inv -> inv.getArgument(0));

      OwnershipRecord result = adapter.save(record);

      assertThat(result).isNotNull();
      assertThat(result.getVehicleId()).isEqualTo(record.getVehicleId());
    }
  }

  @Nested
  @DisplayName("findActiveByVehicleId")
  class FindActiveByVehicleId {

    @Test
    @DisplayName("returns active record")
    void shouldReturnActiveRecord() {
      UUID vehicleId = UUID.randomUUID();
      given(jpa.findActiveByVehicleId(vehicleId)).willReturn(Optional.of(createEntity()));

      Optional<OwnershipRecord> result = adapter.findActiveByVehicleId(vehicleId);
      assertThat(result).isPresent();
    }

    @Test
    @DisplayName("returns empty when not found")
    void shouldReturnEmpty() {
      given(jpa.findActiveByVehicleId(any(UUID.class))).willReturn(Optional.empty());
      assertThat(adapter.findActiveByVehicleId(UUID.randomUUID())).isEmpty();
    }
  }

  @Nested
  @DisplayName("findAllByVehicleId")
  class FindAllByVehicleId {

    @Test
    @DisplayName("returns all records")
    void shouldReturnAllRecords() {
      UUID vehicleId = UUID.randomUUID();
      given(jpa.findAllByVehicleIdOrderByStartDateDesc(vehicleId))
          .willReturn(List.of(createEntity(), createEntity()));

      List<OwnershipRecord> result = adapter.findAllByVehicleId(vehicleId);
      assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("returns empty list")
    void shouldReturnEmptyList() {
      UUID vehicleId = UUID.randomUUID();
      given(jpa.findAllByVehicleIdOrderByStartDateDesc(vehicleId)).willReturn(List.of());

      List<OwnershipRecord> result = adapter.findAllByVehicleId(vehicleId);
      assertThat(result).isEmpty();
    }
  }
}
