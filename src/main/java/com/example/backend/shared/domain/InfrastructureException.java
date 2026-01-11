package com.example.backend.shared.domain;

import java.util.Map;

/**
 * Infraestructura/plataforma (DB caida, DNS, config, etc.)
 * Se mapea típicamente a 503.
 */
public class InfrastructureException extends RuntimeException {
  private final String code;
  private final Map<String, Object> details;

  public InfrastructureException(String code, String message, Map<String, Object> details) {
    super(message);
    this.code = code;
    this.details = details == null ? Map.of() : Map.copyOf(details);
  }

  public String code() { return code; }
  public Map<String, Object> details() { return details; }
}
