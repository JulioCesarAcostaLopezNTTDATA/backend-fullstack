package com.example.backend.shared.observability;

import com.example.backend.shared.api.CorrelationIdFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Observabilidad por request:
 * - Logs estructurados JSON (sin body / sin secrets)
 * - Eventos: request_received, response_sent
 * - MDC: endpoint, httpMethod, statusCode, latencyMs, userId (enmascarado)
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class HttpObservabilityFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(HttpObservabilityFilter.class);

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (request.getRequestURI().startsWith("/actuator")) {
      filterChain.doFilter(request, response);
      return;
    }

    long start = System.currentTimeMillis();
    try {
      MDC.put("endpoint", request.getRequestURI());
      MDC.put("httpMethod", request.getMethod());

      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
        MDC.put("userId", mask(auth.getName()));
      }

      log.info("event=request_received");
      filterChain.doFilter(request, response);
    } finally {
      MDC.put("statusCode", String.valueOf(response.getStatus()));
      MDC.put("latencyMs", String.valueOf(System.currentTimeMillis() - start));
      log.info("event=response_sent");

      MDC.remove("endpoint");
      MDC.remove("httpMethod");
      MDC.remove("statusCode");
      MDC.remove("latencyMs");
      MDC.remove("userId");
    }
  }

  private static String mask(String v) {
    if (v == null || v.isBlank()) return "";
    if (v.length() <= 5) return "***";
    return v.substring(0, 3) + "***" + v.substring(v.length() - 2);
  }
}
