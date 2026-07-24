package com.evcharging.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.zalando.jackson.datatype.money.MoneyModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Jackson ObjectMapper configuration for the EV Charging Platform.
 *
 * <p>Configures:
 *
 * <ul>
 *   <li>JSR 310 (Java Time) module for date/time serialization
 *   <li>JDK 8 module for Optional, Stream, etc.
 *   <li>JSR 354 Money module for MonetaryAmount serialization
 *   <li>ProblemDetail (RFC 7807) module for error responses
 * </ul>
 */
@Configuration
public class JacksonConfig {

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();

    // Register modules
    mapper.registerModule(new JavaTimeModule());
    mapper.registerModule(new Jdk8Module());
    mapper.registerModule(new MoneyModule());
    mapper.registerModule(new ConstraintViolationProblemModule());

    // Serialization features
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    return mapper;
  }
}
