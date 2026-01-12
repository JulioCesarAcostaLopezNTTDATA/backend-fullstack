package com.example.backend.customers.application;

import com.example.backend.customers.domain.Customer;
import com.example.backend.customers.domain.CustomerPolicies;
import com.example.backend.customers.domain.ports.CustomerRepository;
import com.example.backend.shared.domain.DomainException;
import com.example.backend.shared.idempotency.IdempotencyService;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

@Service
public class CreateCustomerUseCase {

  private final CustomerRepository repo;
  private final CustomerPolicies policies;
  private final IdempotencyService idempotency;
  private final Clock clock = Clock.systemUTC();

  public CreateCustomerUseCase(CustomerRepository repo, CustomerPolicies policies, IdempotencyService idempotency) {
    this.repo = repo;
    this.policies = policies;
    this.idempotency = idempotency;
  }

  public CreateCustomerResult handle(CreateCustomerCommand cmd) {
    // Domain validations (rules)
    policies.validateNewCustomer(cmd.getName(), cmd.getEmail());

    // Optional idempotency
    if (cmd.getIdempotencyKey() != null && !cmd.getIdempotencyKey().isBlank()) {
      var decision = idempotency.begin(cmd.getIdempotencyKey(), cmd.getRequestHash());

      if (decision instanceof IdempotencyService.InProgress) {
        return CreateCustomerResult.inProgress();
      }

      if (decision instanceof IdempotencyService.ReplaySuccess rs) {
        var existing = repo.findById(rs.resultId())
            .orElseThrow(() -> new DomainException("IDEMPOTENCY_INCONSISTENT", "saved result not found", Map.of("id", rs.resultId())));
        return CreateCustomerResult.replaySuccess(toView(existing));
      }

      // Created: continue, but ensure we mark success/failed
      try {
        CustomerView view = doCreate(cmd);
        idempotency.markSuccess(cmd.getIdempotencyKey(), view.getId());
        return CreateCustomerResult.created(view);
      } catch (RuntimeException ex) {
        idempotency.markFailed(cmd.getIdempotencyKey());
        throw ex;
      }
    }

    // Non-idempotent flow
    return CreateCustomerResult.created(doCreate(cmd));
  }

  private CustomerView doCreate(CreateCustomerCommand cmd) {
    if (repo.existsByEmail(cmd.getEmail())) {
      throw new DomainException("DUPLICATE_EMAIL", "email already exists", Map.of("email", cmd.getEmail()));
    }

    Instant now = Instant.now(clock);
    Customer customer = Customer.newCustomer(cmd.getName(), cmd.getEmail(), now);
    Customer saved = repo.save(customer);
    return toView(saved);
  }

  private static CustomerView toView(Customer saved) {
    return CustomerView.builder()
        .id(saved.getId())
        .name(saved.getName())
        .email(saved.getEmail())
        .createdAt(saved.getCreatedAt())
        .updatedAt(saved.getUpdatedAt())
        .build();
  }
}
