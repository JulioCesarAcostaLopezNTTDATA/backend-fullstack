package com.example.backend.shared.domain;

import java.util.Map;

/**
 * Dependencias externas (proveedor, API externa, etc.)
 * Se mapea típicamente a 502/503/504.
 */
public class IntegrationException extends RuntimeException {
  private final String code;
  private final Map<String, Object> details;

  public IntegrationException(String code, String message, Map<String, Object> details) {
    super(message);
    this.code = code;
    this.details = details == null ? Map.of() : Map.copyOf(details);
  }

  public String code() { return code; }
  public Map<String, Object> details() { return details; }
}
