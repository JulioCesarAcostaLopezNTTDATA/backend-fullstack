package com.example.backend.customers.domain;

import com.example.backend.shared.domain.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerPoliciesTest {

  private final CustomerPolicies policies = new CustomerPolicies();

  @Test
  void rejectsBadEmail() {
    DomainException ex = assertThrows(DomainException.class, () -> policies.validateNewCustomer("Juan", "bad"));
    assertEquals("INVALID_EMAIL", ex.code());
  }

  @Test
  void acceptsValid() {
    assertDoesNotThrow(() -> policies.validateNewCustomer("Juan Perez", "juan@example.com"));
  }
}
