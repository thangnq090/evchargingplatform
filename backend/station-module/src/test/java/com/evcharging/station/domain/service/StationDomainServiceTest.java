package com.evcharging.station.domain.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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

import com.evcharging.shared.kernel.Location;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.shared.kernel.VendorId;
import com.evcharging.station.domain.model.Connector;
import com.evcharging.station.domain.model.ConnectorType;
import com.evcharging.station.domain.model.Station;
import com.evcharging.station.domain.model.StationStatus;
import com.evcharging.station.domain.model.VendorView;
import com.evcharging.shared.kernel.MarkupPercentage;
import com.evcharging.station.domain.repository.ConnectorRepository;
import com.evcharging.station.domain.repository.StationRepository;
import com.evcharging.station.domain.repository.VendorRepository;

@DisplayName("StationDomainService")
@ExtendWith(MockitoExtension.class)
class StationDomainServiceTest {

  @Mock private StationRepository stationRepository;
  @Mock private ConnectorRepository connectorRepository;
  @Mock private VendorRepository vendorRepository;

  private StationDomainService service;

  private static final UUID VENDOR_UUID = UUID.randomUUID();
  private static final UUID STATION_UUID = UUID.randomUUID();
  private static final Location LOCATION = Location.of(52.52, 13.405);

  @BeforeEach
  void setUp() {
    service = new StationDomainService(stationRepository, connectorRepository, vendorRepository);
  }

  private Station createTestStation() {
    Connector connector = Connector.create(STATION_UUID, ConnectorType.CCS, 150);
    return Station.reconstitute(
        STATION_UUID, VENDOR_UUID, "Test Station", "Downtown", 350,
        StationStatus.AVAILABLE, LOCATION, List.of(connector),
        java.time.Instant.now(), java.time.Instant.now(), null);
  }

  @Nested
  @DisplayName("createStation")
  class CreateStation {

    @Test
    @DisplayName("creates station when vendor exists and name is unique")
    void shouldCreateStation() {
      given(vendorRepository.findById(VENDOR_UUID))
          .willReturn(Optional.of(VendorView.reconstitute(VENDOR_UUID, "GreenCharge", MarkupPercentage.ofBasisPoints(1500))));
      given(stationRepository.existsByVendorIdAndName(VENDOR_UUID, "Test Station")).willReturn(false);

      List<Connector> connectors = List.of(Connector.create(UUID.randomUUID(), ConnectorType.CCS, 150));
      Station station = service.createStation(
          VendorId.of(VENDOR_UUID), "Test Station", "Downtown", 350, LOCATION, connectors);

      assertThat(station).isNotNull();
      assertThat(station.getName()).isEqualTo("Test Station");
      assertThat(station.getVendorId()).isEqualTo(VENDOR_UUID);
    }

    @Test
    @DisplayName("throws when vendor not found")
    void shouldThrowWhenVendorNotFound() {
      given(vendorRepository.findById(VENDOR_UUID)).willReturn(Optional.empty());

      List<Connector> connectors = List.of(Connector.create(UUID.randomUUID(), ConnectorType.CCS, 150));
      assertThatThrownBy(() -> service.createStation(
          VendorId.of(VENDOR_UUID), "Test", null, 350, LOCATION, connectors))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Vendor not found");
    }

    @Test
    @DisplayName("throws when station name already exists for vendor")
    void shouldThrowWhenNameExists() {
      given(vendorRepository.findById(VENDOR_UUID))
          .willReturn(Optional.of(VendorView.reconstitute(VENDOR_UUID, "VC", MarkupPercentage.zero())));
      given(stationRepository.existsByVendorIdAndName(VENDOR_UUID, "Duplicate")).willReturn(true);

      List<Connector> connectors = List.of(Connector.create(UUID.randomUUID(), ConnectorType.CCS, 150));
      assertThatThrownBy(() -> service.createStation(
          VendorId.of(VENDOR_UUID), "Duplicate", null, 350, LOCATION, connectors))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("already exists");
    }
  }

  @Nested
  @DisplayName("updateStation")
  class UpdateStation {

    @Test
    @DisplayName("updates station successfully")
    void shouldUpdateStation() {
      Station station = createTestStation();
      given(stationRepository.findById(StationId.of(STATION_UUID))).willReturn(Optional.of(station));
      given(stationRepository.save(any())).willReturn(station);

      Station result = service.updateStation(StationId.of(STATION_UUID), "New Name", "New Group", 500);

      assertThat(result.getName()).isEqualTo("New Name");
    }

    @Test
    @DisplayName("throws when station not found")
    void shouldThrowWhenNotFound() {
      given(stationRepository.findById(StationId.of(STATION_UUID))).willReturn(Optional.empty());

      assertThatThrownBy(() -> service.updateStation(StationId.of(STATION_UUID), "N", "G", 100))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("throws when new name already exists for vendor")
    void shouldThrowWhenNameConflict() {
      Station station = createTestStation();
      given(stationRepository.findById(StationId.of(STATION_UUID))).willReturn(Optional.of(station));
      given(stationRepository.existsByVendorIdAndName(VENDOR_UUID, "Taken")).willReturn(true);

      assertThatThrownBy(() -> service.updateStation(StationId.of(STATION_UUID), "Taken", null, 100))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("allows same name without conflict")
    void shouldAllowSameName() {
      Station station = createTestStation();
      given(stationRepository.findById(StationId.of(STATION_UUID))).willReturn(Optional.of(station));
      given(stationRepository.save(any())).willReturn(station);

      Station result = service.updateStation(StationId.of(STATION_UUID), "Test Station", null, 100);
      assertThat(result).isNotNull();
    }
  }

  @Nested
  @DisplayName("changeStatus")
  class ChangeStatus {

    @Test
    @DisplayName("changes station status")
    void shouldChangeStatus() {
      Station station = createTestStation();
      given(stationRepository.findById(StationId.of(STATION_UUID))).willReturn(Optional.of(station));
      given(stationRepository.save(any())).willReturn(station);

      Station result = service.changeStatus(StationId.of(STATION_UUID), StationStatus.MAINTENANCE);
      assertThat(result.getStatus()).isEqualTo(StationStatus.MAINTENANCE);
    }

    @Test
    @DisplayName("throws when station not found")
    void shouldThrowWhenNotFound() {
      given(stationRepository.findById(StationId.of(STATION_UUID))).willReturn(Optional.empty());

      assertThatThrownBy(() -> service.changeStatus(StationId.of(STATION_UUID), StationStatus.MAINTENANCE))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("deleteStation")
  class DeleteStation {

    @Test
    @DisplayName("soft-deletes station")
    void shouldDeleteStation() {
      Station station = createTestStation();
      given(stationRepository.findById(StationId.of(STATION_UUID))).willReturn(Optional.of(station));

      service.deleteStation(StationId.of(STATION_UUID));

      then(stationRepository).should().save(station);
      assertThat(station.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("throws when station not found")
    void shouldThrowWhenNotFound() {
      given(stationRepository.findById(StationId.of(STATION_UUID))).willReturn(Optional.empty());

      assertThatThrownBy(() -> service.deleteStation(StationId.of(STATION_UUID)))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("findNearby")
  class FindNearby {

    @Test
    @DisplayName("returns stations near location")
    void shouldFindNearby() {
      Station station = createTestStation();
      given(stationRepository.findNearby(LOCATION, 10.0)).willReturn(List.of(station));

      List<Station> result = service.findNearby(LOCATION, 10.0);
      assertThat(result).hasSize(1);
    }
  }
}
