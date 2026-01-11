package com.example.backend.customers.application;

import com.example.backend.customers.domain.ports.CustomerRepository;
import com.example.backend.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DeleteCustomerUseCase {

  private final CustomerRepository repo;

  public DeleteCustomerUseCase(CustomerRepository repo) {
    this.repo = repo;
  }

  public void handle(String id) {
    if (!repo.deleteById(id)) {
      throw new NotFoundException("Customer not found", Map.of("id", id));
    }
  }
}
