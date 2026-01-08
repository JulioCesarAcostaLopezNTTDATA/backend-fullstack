package com.example.backend.customers.application;

import com.example.backend.customers.domain.Customer;
import com.example.backend.customers.domain.CustomerPolicies;
import com.example.backend.customers.domain.ports.CustomerRepository;
import com.example.backend.shared.domain.DomainException;
import com.example.backend.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

@Service
public class UpdateCustomerUseCase {

  private final CustomerRepository repo;
  private final CustomerPolicies policies;
  private final Clock clock = Clock.systemUTC();

  public UpdateCustomerUseCase(CustomerRepository repo, CustomerPolicies policies) {
    this.repo = repo;
    this.policies = policies;
  }

  public CustomerView handle(UpdateCustomerCommand cmd) {
    policies.validateUpdateCustomer(cmd.getId(), cmd.getName(), cmd.getEmail());

    Customer existing = repo.findById(cmd.getId())
        .orElseThrow(() -> new NotFoundException("Customer not found", Map.of("id", cmd.getId())));

    if (!existing.getEmail().equalsIgnoreCase(cmd.getEmail()) && repo.existsByEmail(cmd.getEmail())) {
      throw new DomainException("DUPLICATE_EMAIL", "email already exists", Map.of("email", cmd.getEmail()));
    }

    Instant now = Instant.now(clock);
    Customer updated = existing.update(cmd.getName(), cmd.getEmail(), now);
    Customer saved = repo.save(updated);

    return CustomerView.builder()
        .id(saved.getId())
        .name(saved.getName())
        .email(saved.getEmail())
        .createdAt(saved.getCreatedAt())
        .updatedAt(saved.getUpdatedAt())
        .build();
  }
}
