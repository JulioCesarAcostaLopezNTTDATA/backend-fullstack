package com.example.backend.shared.observability;

import io.micrometer.core.instrument.Counter;

public class ObservabilityMeters {

  private final Counter rateLimitTriggered;
  private final Counter unauthorized;
  private final Counter forbidden;

  public ObservabilityMeters(Counter rateLimitTriggered, Counter unauthorized, Counter forbidden) {
    this.rateLimitTriggered = rateLimitTriggered;
    this.unauthorized = unauthorized;
    this.forbidden = forbidden;
  }

  public void incRateLimit() { rateLimitTriggered.increment(); }
  public void incUnauthorized() { unauthorized.increment(); }
  public void incForbidden() { forbidden.increment(); }
}
