package com.evcharging.session.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SessionId")
class SessionIdTest {

  @Nested
  @DisplayName("of")
  class Of {

    @Test
    @DisplayName("creates from UUID")
    void shouldCreateFromUuid() {
      UUID uuid = UUID.randomUUID();
      SessionId id = SessionId.of(uuid);
      assertThat(id.getValue()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("creates from String")
    void shouldCreateFromString() {
      UUID uuid = UUID.randomUUID();
      SessionId id = SessionId.of(uuid.toString());
      assertThat(id.getValue()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("throws on null UUID")
    void shouldThrowOnNullUuid() {
      assertThatThrownBy(() -> SessionId.of((UUID) null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("throws on invalid String")
    void shouldThrowOnInvalidString() {
      assertThatThrownBy(() -> SessionId.of("not-a-uuid"))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("generate")
  class Generate {

    @Test
    @DisplayName("generates unique ID")
    void shouldGenerateUnique() {
      SessionId id1 = SessionId.generate();
      SessionId id2 = SessionId.generate();
      assertThat(id1).isNotEqualTo(id2);
    }
  }

  @Nested
  @DisplayName("toString")
  class ToString {

    @Test
    @DisplayName("returns UUID string")
    void shouldReturnUuidString() {
      UUID uuid = UUID.randomUUID();
      SessionId id = SessionId.of(uuid);
      assertThat(id.toString()).isEqualTo(uuid.toString());
    }
  }

  @Nested
  @DisplayName("equals and hashCode")
  class Equality {

    @Test
    @DisplayName("equal when same UUID")
    void shouldBeEqual() {
      UUID uuid = UUID.randomUUID();
      SessionId id1 = SessionId.of(uuid);
      SessionId id2 = SessionId.of(uuid);
      assertThat(id1).isEqualTo(id2);
      assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    @DisplayName("not equal when different UUID")
    void shouldNotBeEqual() {
      SessionId id1 = SessionId.generate();
      SessionId id2 = SessionId.generate();
      assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    @DisplayName("not equal to null")
    void shouldNotBeEqualToNull() {
      assertThat(SessionId.generate()).isNotEqualTo(null);
    }

    @Test
    @DisplayName("not equal to other type")
    void shouldNotBeEqualToOtherType() {
      assertThat(SessionId.generate()).isNotEqualTo("string");
    }

    @Test
    @DisplayName("equal to itself")
    void shouldBeEqualToSelf() {
      SessionId id = SessionId.generate();
      assertThat(id).isEqualTo(id);
    }
  }
}
