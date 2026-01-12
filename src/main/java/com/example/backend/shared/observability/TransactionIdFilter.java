package com.example.backend.shared.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * transactionId (id de negocio) para trazabilidad.
 *
 * En este CRUD, usamos como transactionId:
 * - header X-Transaction-Id (si viene)
 * - o el {id} cuando el endpoint es /customers/{id}
 *
 * Nota: en recargas/pagos, este sería el rechargeId/paymentId.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 15)
public class TransactionIdFilter extends OncePerRequestFilter {

  private static final Pattern CUSTOMER_ID = Pattern.compile("^/customers/([^/]+)(/.*)?$");

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (request.getRequestURI().startsWith("/actuator")) {
      filterChain.doFilter(request, response);
      return;
    }

    String tx = request.getHeader("X-Transaction-Id");
    if (tx == null || tx.isBlank()) {
      Matcher m = CUSTOMER_ID.matcher(request.getRequestURI());
      if (m.matches()) tx = m.group(1);
    }

    if (tx != null && !tx.isBlank()) MDC.put("transactionId", tx);

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove("transactionId");
    }
  }
}
