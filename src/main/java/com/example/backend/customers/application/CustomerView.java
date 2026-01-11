package com.example.backend.customers.application;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class CustomerView {
  String id;
  String name;
  String email;
  Instant createdAt;
  Instant updatedAt;
}
