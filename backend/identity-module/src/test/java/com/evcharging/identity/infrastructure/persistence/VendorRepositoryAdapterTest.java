package com.evcharging.identity.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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
import org.springframework.data.domain.PageRequest;

import com.evcharging.identity.domain.model.Vendor;
import com.evcharging.shared.pagination.PaginatedList;

@DisplayName("VendorRepositoryAdapter")
@ExtendWith(MockitoExtension.class)
class VendorRepositoryAdapterTest {

  @Mock private SpringDataVendorRepository jpa;

  private VendorRepositoryAdapter adapter;

  private static final UUID VENDOR_UUID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    adapter = new VendorRepositoryAdapter(jpa);
  }

  private Vendor createVendor() {
    return Vendor.reconstitute(
        VENDOR_UUID, "ACME Charging",
        com.evcharging.identity.domain.model.VendorStatus.ACTIVE,
        com.evcharging.shared.kernel.MarkupPercentage.zero(),
        Instant.now(), Instant.now());
  }

  private VendorDbEntity createEntity() {
    return VendorDbEntity.from(createVendor(), true);
  }

  @Nested
  @DisplayName("save")
  class Save {

    @Test
    @DisplayName("saves new vendor")
    void shouldSaveNewVendor() {
      given(jpa.existsById(VENDOR_UUID)).willReturn(false);
      given(jpa.save(any(VendorDbEntity.class))).willAnswer(inv -> inv.getArgument(0));

      Vendor result = adapter.save(createVendor());

      assertThat(result).isNotNull();
      then(jpa).should().save(any(VendorDbEntity.class));
    }

    @Test
    @DisplayName("updates existing vendor")
    void shouldUpdateExistingVendor() {
      given(jpa.existsById(VENDOR_UUID)).willReturn(true);
      given(jpa.save(any(VendorDbEntity.class))).willAnswer(inv -> inv.getArgument(0));

      Vendor result = adapter.save(createVendor());

      assertThat(result).isNotNull();
    }
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("returns vendor when found")
    void shouldReturnVendor() {
      given(jpa.findById(VENDOR_UUID)).willReturn(Optional.of(createEntity()));

      Optional<Vendor> result = adapter.findById(VENDOR_UUID);
      assertThat(result).isPresent();
      assertThat(result.get().getName()).isEqualTo("ACME Charging");
    }

    @Test
    @DisplayName("returns empty when not found")
    void shouldReturnEmpty() {
      given(jpa.findById(VENDOR_UUID)).willReturn(Optional.empty());

      assertThat(adapter.findById(VENDOR_UUID)).isEmpty();
    }
  }

  @Nested
  @DisplayName("findByName")
  class FindByName {

    @Test
    @DisplayName("delegates to JPA")
    void shouldDelegate() {
      given(jpa.findByName("ACME Charging")).willReturn(Optional.of(createEntity()));

      Optional<Vendor> result = adapter.findByName("ACME Charging");
      assertThat(result).isPresent();
    }
  }

  @Nested
  @DisplayName("findAll (list)")
  class FindAll {

    @Test
    @DisplayName("returns all vendors")
    void shouldReturnAll() {
      given(jpa.findAll()).willReturn(List.of(createEntity()));

      List<Vendor> result = adapter.findAll();
      assertThat(result).hasSize(1);
    }
  }

  @Nested
  @DisplayName("findAll (paginated)")
  class FindAllPaginated {

    @Test
    @DisplayName("returns first page without cursor")
    void shouldReturnFirstPage() {
      given(jpa.findByOrderByCreatedAtDesc(any(PageRequest.class)))
          .willReturn(List.of(createEntity()));

      PaginatedList<Vendor> result = adapter.findAll(20, null);

      assertThat(result.items()).hasSize(1);
      assertThat(result.pagination().hasMore()).isFalse();
    }

    @Test
    @DisplayName("returns page with cursor")
    void shouldReturnPageWithCursor() {
      UUID cursor = UUID.randomUUID();
      given(jpa.findByIdLessThanOrderByCreatedAtDesc(eq(cursor), any(PageRequest.class)))
          .willReturn(List.of(createEntity()));

      PaginatedList<Vendor> result = adapter.findAll(20, cursor);

      assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("sets hasMore when results exceed limit")
    void shouldSetHasMore() {
      List<VendorDbEntity> many =
          java.util.stream.IntStream.range(0, 21)
              .mapToObj(i -> createEntity())
              .toList();
      given(jpa.findByOrderByCreatedAtDesc(any(PageRequest.class))).willReturn(many);

      PaginatedList<Vendor> result = adapter.findAll(20, null);

      assertThat(result.pagination().hasMore()).isTrue();
      assertThat(result.items()).hasSize(20);
    }

    @Test
    @DisplayName("returns empty page")
    void shouldReturnEmptyPage() {
      given(jpa.findByOrderByCreatedAtDesc(any(PageRequest.class)))
          .willReturn(List.of());

      PaginatedList<Vendor> result = adapter.findAll(20, null);

      assertThat(result.items()).isEmpty();
      assertThat(result.pagination().hasMore()).isFalse();
    }
  }

  @Nested
  @DisplayName("existsByName")
  class ExistsByName {

    @Test
    @DisplayName("delegates to JPA")
    void shouldDelegate() {
      given(jpa.existsByName("ACME Charging")).willReturn(true);

      assertThat(adapter.existsByName("ACME Charging")).isTrue();
    }
  }
}
