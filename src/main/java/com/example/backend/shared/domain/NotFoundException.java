package com.example.backend.shared.domain;

import java.util.Map;

public class NotFoundException extends RuntimeException {
  private final String code = "NOT_FOUND";
  private final Map<String, Object> details;

  public NotFoundException(String message, Map<String, Object> details) {
    super(message);
    this.details = details == null ? Map.of() : Map.copyOf(details);
  }
  public String code() { return code; }
  public Map<String, Object> details() { return details; }
}
