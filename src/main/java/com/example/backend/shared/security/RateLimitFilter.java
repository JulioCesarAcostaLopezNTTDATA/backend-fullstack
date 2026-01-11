package com.example.backend.shared.security;

import com.example.backend.shared.api.CorrelationIdFilter;
import com.example.backend.shared.api.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limit básico por IP para prevenir abuso.
 * Recomendado: mover a Gateway/WAF en prod.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

  private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
  private final ObjectMapper objectMapper;

  public RateLimitFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // No limitar actuator
    String path = request.getRequestURI();
    if (path.startsWith("/actuator")) {
      filterChain.doFilter(request, response);
      return;
    }

    String ip = clientIp(request);
    Bucket bucket = buckets.computeIfAbsent(ip, k -> newBucket());

    if (bucket.tryConsume(1)) {
      filterChain.doFilter(request, response);
      return;
    }

    // 429
    response.setStatus(429);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), ErrorResponse.of(
        "RATE_LIMIT",
        "Too many requests. Please try again later.",
        cid(),
        Map.of()
    ));
  }

  private Bucket newBucket() {
    Bandwidth limit = Bandwidth.classic(60, Refill.greedy(60, Duration.ofMinutes(1))); // 60 req/min
    return Bucket.builder().addLimit(limit).build();
  }

  private static String clientIp(HttpServletRequest req) {
    String xff = req.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
    return req.getRemoteAddr();
  }

  private static String cid() {
    String v = MDC.get(CorrelationIdFilter.MDC_KEY);
    return v == null ? "" : v;
  }
}
