package com.example.backend.customers.domain;

import com.example.backend.shared.domain.DomainException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CustomerPolicies {

  public void validateNewCustomer(String name, String email) {
    validateName(name);
    validateEmail(email);
  }

  public void validateUpdateCustomer(String id, String name, String email) {
    if (id == null || id.isBlank()) {
      throw new DomainException("ID_REQUIRED", "id is required", Map.of());
    }
    validateName(name);
    validateEmail(email);
  }

  private void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new DomainException("INVALID_NAME", "name is required", Map.of());
    }
    if (name.length() < 2 || name.length() > 80) {
      throw new DomainException("INVALID_NAME", "name length must be 2..80", Map.of("length", name.length()));
    }
  }

  private void validateEmail(String email) {
    if (email == null || email.isBlank()) {
      throw new DomainException("INVALID_EMAIL", "email is required", Map.of());
    }
    if (!email.matches("^[^@\s]+@[^@\s]+.[^@\s]+$")) {
      throw new DomainException("INVALID_EMAIL", "email format is invalid", Map.of("email", email));
    }
  }
}
