package com.example.backend.customers.application;

import com.example.backend.customers.domain.ports.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListCustomersUseCase {

  private final CustomerRepository repo;

  public ListCustomersUseCase(CustomerRepository repo) {
    this.repo = repo;
  }

  public List<CustomerView> handle() {
    return repo.findAll().stream()
        .map(c -> CustomerView.builder()
            .id(c.getId())
            .name(c.getName())
            .email(c.getEmail())
            .createdAt(c.getCreatedAt())
            .updatedAt(c.getUpdatedAt())
            .build())
        .toList();
  }
}
