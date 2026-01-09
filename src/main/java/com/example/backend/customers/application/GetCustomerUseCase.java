package com.example.backend.customers.application;

import com.example.backend.customers.domain.Customer;
import com.example.backend.customers.domain.ports.CustomerRepository;
import com.example.backend.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GetCustomerUseCase {

  private final CustomerRepository repo;

  public GetCustomerUseCase(CustomerRepository repo) {
    this.repo = repo;
  }

  public CustomerView handle(String id) {
    Customer c = repo.findById(id).orElseThrow(() -> new NotFoundException("Customer not found", Map.of("id", id)));
    return CustomerView.builder()
        .id(c.getId())
        .name(c.getName())
        .email(c.getEmail())
        .createdAt(c.getCreatedAt())
        .updatedAt(c.getUpdatedAt())
        .build();
  }
}
