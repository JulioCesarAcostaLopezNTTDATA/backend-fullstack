package com.example.backend.customers.api;

import com.example.backend.customers.application.CustomerView;

final class CustomerMappers {
  private CustomerMappers() {}

  static CustomerResponse toResponse(CustomerView view) {
    return CustomerResponse.builder()
        .id(view.getId())
        .name(view.getName())
        .email(view.getEmail())
        .createdAt(view.getCreatedAt())
        .updatedAt(view.getUpdatedAt())
        .build();
  }
}
