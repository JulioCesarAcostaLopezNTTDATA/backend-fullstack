package com.example.backend.shared.api;

import com.example.backend.shared.domain.DomainException;
import com.example.backend.shared.domain.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> handleBodyValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    Map<String, String> fields = new HashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      fields.put(fe.getField(), fe.getDefaultMessage());
    }
    return ResponseEntity.badRequest()
        .body(ErrorResponse.of("VALIDATION_ERROR", "Invalid request body", req.getRequestURI(), Map.of("fields", fields)));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<ErrorResponse> handleParamValidation(ConstraintViolationException ex, HttpServletRequest req) {
    return ResponseEntity.badRequest()
        .body(ErrorResponse.of("VALIDATION_ERROR", ex.getMessage(), req.getRequestURI(), Map.of()));
  }

  @ExceptionHandler(DomainException.class)
  ResponseEntity<ErrorResponse> handleDomain(DomainException ex, HttpServletRequest req) {
    return ResponseEntity.unprocessableEntity()
        .body(ErrorResponse.of(ex.code(), ex.getMessage(), req.getRequestURI(), ex.details()));
  }

  @ExceptionHandler(NotFoundException.class)
  ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest req) {
    return ResponseEntity.status(404)
        .body(ErrorResponse.of(ex.code(), ex.getMessage(), req.getRequestURI(), ex.details()));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
    return ResponseEntity.status(500)
        .body(ErrorResponse.of("UNEXPECTED_ERROR", "Unexpected error", req.getRequestURI(), Map.of("exception", ex.getClass().getSimpleName())));
  }
}
