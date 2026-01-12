package com.example.backend.shared.api;

import com.example.backend.shared.domain.DomainException;
import com.example.backend.shared.domain.IntegrationException;
import com.example.backend.shared.domain.InfrastructureException;
import com.example.backend.shared.domain.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private static String cid() {
    String v = MDC.get(CorrelationIdFilter.MDC_KEY);
    return v == null ? "" : v;
  }

  // 400 - validación cliente
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> handleBodyValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    Map<String, String> fields = new HashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      fields.put(fe.getField(), fe.getDefaultMessage());
    }
    return ResponseEntity.badRequest()
        .body(ErrorResponse.of("VALIDATION_ERROR", "Invalid request", cid(), Map.of("fields", fields)));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<ErrorResponse> handleParamValidation(ConstraintViolationException ex, HttpServletRequest req) {
    return ResponseEntity.badRequest()
        .body(ErrorResponse.of("VALIDATION_ERROR", "Invalid request", cid(), Map.of()));
  }

  // 404 - recurso no existe
  @ExceptionHandler(NotFoundException.class)
  ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest req) {
    return ResponseEntity.status(404)
        .body(ErrorResponse.of(ex.code(), "Resource not found", cid(), ex.details()));
  }

  // 409/422 - reglas de negocio
  @ExceptionHandler(DomainException.class)
  ResponseEntity<ErrorResponse> handleDomain(DomainException ex, HttpServletRequest req) {
    int status = switch (ex.code()) {
      case "DUPLICATE_EMAIL", "IDEMPOTENCY_CONFLICT" -> 409;
      default -> 422;
    };
    return ResponseEntity.status(status)
        .body(ErrorResponse.of(ex.code(), ex.getMessage(), cid(), ex.details()));
  }

  // 502/504 - integración externa (placeholder)
  @ExceptionHandler(IntegrationException.class)
  ResponseEntity<ErrorResponse> handleIntegration(IntegrationException ex, HttpServletRequest req) {
    // no revelar detalle técnico
    log.warn("Integration error (cid={}): {}", cid(), ex.getMessage(), ex);
    return ResponseEntity.status(502)
        .body(ErrorResponse.of(ex.code(), "Upstream dependency failed", cid(), Map.of()));
  }

  // 503 - infraestructura (DB, red, config)
  @ExceptionHandler({InfrastructureException.class, DataAccessResourceFailureException.class})
  ResponseEntity<ErrorResponse> handleInfrastructure(Exception ex, HttpServletRequest req) {
    MDC.put("errorType","technical");
    MDC.put("errorCode", (ex instanceof InfrastructureException ie) ? ie.code() : "INFRA_DB_UNAVAILABLE");
    log.warn("event=infrastructure_failed cid={} msg={}", cid(), ex.getMessage(), ex);
    String code = (ex instanceof InfrastructureException ie) ? ie.code() : "INFRA_DB_UNAVAILABLE";
    return ResponseEntity.status(503)
        .body(ErrorResponse.of(code, "Service temporarily unavailable", cid(), Map.of()));
  }

  // 500 - desconocido
  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
    MDC.put("errorType","technical");
    MDC.put("errorCode","UNEXPECTED_ERROR");
    log.error("event=unexpected_error cid={} msg={}", cid(), ex.getMessage(), ex);
    return ResponseEntity.status(500)
        .body(ErrorResponse.of("UNEXPECTED_ERROR", "Unexpected error. Try again later.", cid(), Map.of()));
  }
}
