package com.evcharging.station.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.shared.kernel.Location;

@DisplayName("Station Domain Tests")
class StationTest {

  private static final Location LOCATION = Location.of(52.5200, 13.4050);
  private static final UUID VENDOR_ID = UUID.randomUUID();

  private Connector createConnector() {
    return Connector.create(UUID.randomUUID(), ConnectorType.CCS, 150);
  }

  @Nested
  @DisplayName("create")
  class Create {

    @Test
    @DisplayName("creates station with AVAILABLE status and generated id")
    void shouldCreateStation() {
      var connectors = List.of(createConnector());
      Station station =
          Station.create(VENDOR_ID, "Downtown Charger", "Downtown", 350, LOCATION, connectors);

      assertThat(station.getId()).isNotNull();
      assertThat(station.getVendorId()).isEqualTo(VENDOR_ID);
      assertThat(station.getName()).isEqualTo("Downtown Charger");
      assertThat(station.getGroupLabel()).isEqualTo("Downtown");
      assertThat(station.getUnitPriceTenthCents()).isEqualTo(350);
      assertThat(station.getStatus()).isEqualTo(StationStatus.AVAILABLE);
      assertThat(station.getLocation()).isEqualTo(LOCATION);
      assertThat(station.getConnectors()).hasSize(1);
      assertThat(station.isDeleted()).isFalse();
      assertThat(station.isOperational()).isTrue();
    }

    @Test
    @DisplayName("rejects blank name")
    void shouldRejectBlankName() {
      assertThatThrownBy(
              () ->
                  Station.create(VENDOR_ID, "  ", null, 350, LOCATION, List.of(createConnector())))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rejects null name")
    void shouldRejectNullName() {
      assertThatThrownBy(
              () ->
                  Station.create(VENDOR_ID, null, null, 350, LOCATION, List.of(createConnector())))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("rejects negative unit price")
    void shouldRejectNegativePrice() {
      assertThatThrownBy(
              () ->
                  Station.create(
                      VENDOR_ID, "Test Station", null, -1, LOCATION, List.of(createConnector())))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rejects empty connectors list")
    void shouldRejectEmptyConnectors() {
      assertThatThrownBy(() -> Station.create(VENDOR_ID, "Test", null, 350, LOCATION, List.of()))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rejects null location")
    void shouldRejectNullLocation() {
      assertThatThrownBy(
              () -> Station.create(VENDOR_ID, "Test", null, 350, null, List.of(createConnector())))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("update")
  class Update {

    @Test
    @DisplayName("updates name and price")
    void shouldUpdateFields() {
      Station station =
          Station.create(
              VENDOR_ID, "Original Name", "Group", 350, LOCATION, List.of(createConnector()));

      station.update("Updated Name", "New Group", 400, null);

      assertThat(station.getName()).isEqualTo("Updated Name");
      assertThat(station.getGroupLabel()).isEqualTo("New Group");
      assertThat(station.getUnitPriceTenthCents()).isEqualTo(400);
    }

    @Test
    @DisplayName("rejects update on deleted station")
    void shouldRejectUpdateOnDeleted() {
      Station station =
          Station.create(VENDOR_ID, "Test", "Group", 350, LOCATION, List.of(createConnector()));
      station.delete();

      assertThatThrownBy(() -> station.update("New", "Group", 400, null))
          .isInstanceOf(IllegalStateException.class);
    }
  }

  @Nested
  @DisplayName("changeStatus")
  class ChangeStatus {

    @Test
    @DisplayName("changes status from AVAILABLE to MAINTENANCE")
    void shouldChangeStatus() {
      Station station =
          Station.create(VENDOR_ID, "Test", null, 350, LOCATION, List.of(createConnector()));

      station.changeStatus(StationStatus.MAINTENANCE);
      assertThat(station.getStatus()).isEqualTo(StationStatus.MAINTENANCE);
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {

    @Test
    @DisplayName("soft-deletes station and sets UNAVAILABLE")
    void shouldSoftDelete() {
      Station station =
          Station.create(VENDOR_ID, "Test", null, 350, LOCATION, List.of(createConnector()));

      station.delete();

      assertThat(station.isDeleted()).isTrue();
      assertThat(station.getDeletedAt()).isNotNull();
      assertThat(station.isOperational()).isFalse();
    }

    @Test
    @DisplayName("prevents double deletion")
    void shouldPreventDoubleDelete() {
      Station station =
          Station.create(VENDOR_ID, "Test", null, 350, LOCATION, List.of(createConnector()));
      station.delete();

      assertThatThrownBy(station::delete).isInstanceOf(IllegalStateException.class);
    }
  }
}
