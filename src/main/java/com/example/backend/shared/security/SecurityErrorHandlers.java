package com.example.backend.shared.security;

import com.example.backend.shared.api.CorrelationIdFilter;
import com.example.backend.shared.api.ErrorResponse;
import com.example.backend.shared.observability.ObservabilityMeters;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public final class SecurityErrorHandlers {

  private SecurityErrorHandlers() {}

  public static AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper om, ObservabilityMeters meters) {
    return (HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) -> {
      meters.incUnauthorized();
      MDC.put("errorCode", "UNAUTHENTICATED");
      MDC.put("errorType", "security");
      write(response, om, 401, ErrorResponse.of(
          "UNAUTHENTICATED",
          "Authentication required",
          cid(),
          Map.of()
      ));
    };
  }

  public static AccessDeniedHandler accessDeniedHandler(ObjectMapper om, ObservabilityMeters meters) {
    return (HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) -> {
      meters.incForbidden();
      MDC.put("errorCode", "FORBIDDEN");
      MDC.put("errorType", "security");
      write(response, om, 403, ErrorResponse.of(
          "FORBIDDEN",
          "You don't have permission to perform this action",
          cid(),
          Map.of()
      ));
    };
  }

  private static String cid() {
    String v = MDC.get(CorrelationIdFilter.MDC_KEY);
    return v == null ? "" : v;
  }

  private static void write(HttpServletResponse resp, ObjectMapper om, int status, ErrorResponse body) throws IOException {
    resp.setStatus(status);
    resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
    om.writeValue(resp.getOutputStream(), body);
  }
}
