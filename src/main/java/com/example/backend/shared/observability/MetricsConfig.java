package com.example.backend.shared.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

  @Bean
  ObservabilityMeters observabilityMeters(MeterRegistry registry) {
    return new ObservabilityMeters(
        Counter.builder("api.rate_limit.triggered").description("Rate limit triggered count").register(registry),
        Counter.builder("api.auth.unauthorized").description("401 count").register(registry),
        Counter.builder("api.auth.forbidden").description("403 count").register(registry)
    );
  }
}
