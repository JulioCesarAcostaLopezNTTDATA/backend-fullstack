package com.example.backend.shared.security;

import com.example.backend.shared.api.CorrelationIdFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Auditoría mínima por request: quién, qué, cuándo, desde dónde.
 * No loguea body/PII.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AuditLogFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(AuditLogFilter.class);

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    long start = System.currentTimeMillis();
    try {
      filterChain.doFilter(request, response);
    } finally {
      long ms = System.currentTimeMillis() - start;

      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      String subject = (auth == null || !auth.isAuthenticated()) ? "anonymous" : auth.getName();

      String cid = MDC.get(CorrelationIdFilter.MDC_KEY);
      if (cid == null) cid = "";

      String ip = request.getHeader("X-Forwarded-For");
      if (ip != null && !ip.isBlank()) ip = ip.split(",")[0].trim();
      else ip = request.getRemoteAddr();

      log.info("AUDIT cid={} sub={} method={} path={} status={} ip={} ms={}",
          cid, subject, request.getMethod(), request.getRequestURI(), response.getStatus(), ip, ms);
    }
  }
}
