package com.evcharging.billing.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BillingSecurityConfig")
class BillingSecurityConfigTest {

  @Test
  @DisplayName("can be instantiated")
  void shouldInstantiate() {
    BillingSecurityConfig config = new BillingSecurityConfig();
    assertThat(config).isNotNull();
  }
}
