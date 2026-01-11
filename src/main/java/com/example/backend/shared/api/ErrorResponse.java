package com.example.backend.shared.api;

import java.util.Map;

public record ErrorResponse(
    String errorCode,
    String message,
    String correlationId,
    Map<String, Object> details
) {
  public static ErrorResponse of(String errorCode, String message, String correlationId, Map<String, Object> details) {
    return new ErrorResponse(
        errorCode,
        message,
        correlationId,
        details == null ? Map.of() : Map.copyOf(details)
    );
  }
}
