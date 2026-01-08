package com.example.backend.shared.api;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
    Instant timestamp,
    String error,
    String message,
    String path,
    Map<String, Object> details
) {
  public static ErrorResponse of(String error, String message, String path, Map<String, Object> details) {
    return new ErrorResponse(Instant.now(), error, message, path, details == null ? Map.of() : Map.copyOf(details));
  }
}
