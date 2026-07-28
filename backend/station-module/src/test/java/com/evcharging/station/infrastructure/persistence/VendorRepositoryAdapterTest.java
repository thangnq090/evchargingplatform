package com.evcharging.station.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evcharging.identity.VendorMarkupApi;
import com.evcharging.shared.kernel.MarkupPercentage;
import com.evcharging.station.domain.model.VendorView;

@DisplayName("VendorRepositoryAdapter")
@ExtendWith(MockitoExtension.class)
class VendorRepositoryAdapterTest {

  @Mock private VendorMarkupApi identityVendorMarkupApi;

  private VendorRepositoryAdapter adapter;

  private static final UUID VENDOR_UUID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    adapter = new VendorRepositoryAdapter(identityVendorMarkupApi);
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("returns vendor view when found")
    void shouldReturnVendorView() {
      given(identityVendorMarkupApi.getMarkup(VENDOR_UUID))
          .willReturn(Optional.of(MarkupPercentage.ofBasisPoints(1500)));

      Optional<VendorView> result = adapter.findById(VENDOR_UUID);
      assertThat(result).isPresent();
      assertThat(result.get().id()).isEqualTo(VENDOR_UUID);
      assertThat(result.get().markupPercentage().getBasisPoints()).isEqualTo(1500);
    }

    @Test
    @DisplayName("returns empty when not found")
    void shouldReturnEmpty() {
      given(identityVendorMarkupApi.getMarkup(VENDOR_UUID)).willReturn(Optional.empty());

      Optional<VendorView> result = adapter.findById(VENDOR_UUID);
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("findAll")
  class FindAll {

    @Test
    @DisplayName("throws UnsupportedOperationException")
    void shouldThrow() {
      assertThatThrownBy(adapter::findAll)
          .isInstanceOf(UnsupportedOperationException.class);
    }
  }

  @Nested
  @DisplayName("existsByName")
  class ExistsByName {

    @Test
    @DisplayName("throws UnsupportedOperationException")
    void shouldThrow() {
      assertThatThrownBy(() -> adapter.existsByName("test"))
          .isInstanceOf(UnsupportedOperationException.class);
    }
  }

  @Nested
  @DisplayName("updateMarkup")
  class UpdateMarkup {

    @Test
    @DisplayName("updates markup via API")
    void shouldUpdateMarkup() {
      MarkupPercentage newMarkup = MarkupPercentage.ofBasisPoints(2000);
      given(identityVendorMarkupApi.updateMarkup(VENDOR_UUID, 2000, VENDOR_UUID))
          .willReturn(newMarkup);

      VendorView result = adapter.updateMarkup(VENDOR_UUID, newMarkup);
      assertThat(result.markupPercentage().getBasisPoints()).isEqualTo(2000);
    }
  }
}
