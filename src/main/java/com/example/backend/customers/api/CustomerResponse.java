package com.example.backend.customers.api;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class CustomerResponse {
  String id;
  String name;
  String email;
  Instant createdAt;
  Instant updatedAt;
}
