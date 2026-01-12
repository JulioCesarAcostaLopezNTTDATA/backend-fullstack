package com.example.backend.customers.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class Customer {

  private final String id;
  private final String name;
  private final String email;
  private final Instant createdAt;
  private final Instant updatedAt;

  public static Customer newCustomer(String name, String email, Instant now) {
    return new Customer(UUID.randomUUID().toString(), name, email, now, now);
  }

  public Customer update(String name, String email, Instant now) {
    return new Customer(this.id, name, email, this.createdAt, now);
  }
}
