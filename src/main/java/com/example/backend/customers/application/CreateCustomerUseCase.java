package com.example.backend.customers.application;

import com.example.backend.customers.domain.Customer;
import com.example.backend.customers.domain.CustomerPolicies;
import com.example.backend.customers.domain.ports.CustomerRepository;
import com.example.backend.shared.domain.DomainException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

@Service
public class CreateCustomerUseCase {

  private final CustomerRepository repo;
  private final CustomerPolicies policies;
  private final Clock clock = Clock.systemUTC();

  public CreateCustomerUseCase(CustomerRepository repo, CustomerPolicies policies) {
    this.repo = repo;
    this.policies = policies;
  }

  public CustomerView handle(CreateCustomerCommand cmd) {
    policies.validateNewCustomer(cmd.getName(), cmd.getEmail());

    if (repo.existsByEmail(cmd.getEmail())) {
      throw new DomainException("DUPLICATE_EMAIL", "email already exists", Map.of("email", cmd.getEmail()));
    }

    Instant now = Instant.now(clock);
    Customer customer = Customer.newCustomer(cmd.getName(), cmd.getEmail(), now);
    Customer saved = repo.save(customer);

    return CustomerView.builder()
        .id(saved.getId())
        .name(saved.getName())
        .email(saved.getEmail())
        .createdAt(saved.getCreatedAt())
        .updatedAt(saved.getUpdatedAt())
        .build();
  }
}
