package com.evcharging.station.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.evcharging.station.domain.port.MarkupCachePort;
import com.evcharging.station.domain.repository.ConnectorRepository;
import com.evcharging.station.domain.repository.StationRepository;
import com.evcharging.station.domain.repository.VendorRepository;
import com.evcharging.station.domain.service.MarkupDomainService;
import com.evcharging.station.domain.service.StationDomainService;
import com.evcharging.station.infrastructure.cache.InMemoryMarkupCacheAdapter;
import com.github.benmanes.caffeine.cache.Caffeine;

/** Station module infrastructure and domain service configuration. */
@Configuration
public class StationInfrastructureConfig {

  @Bean
  public MarkupCachePort markupCachePort() {
    return new InMemoryMarkupCacheAdapter(
        Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(5)).maximumSize(10_000).build());
  }

  @Bean
  public StationDomainService stationDomainService(
      StationRepository stationRepository,
      ConnectorRepository connectorRepository,
      VendorRepository vendorRepository) {
    return new StationDomainService(stationRepository, connectorRepository, vendorRepository);
  }

  @Bean
  public MarkupDomainService markupDomainService(VendorRepository vendorRepository) {
    return new MarkupDomainService(vendorRepository);
  }
}
