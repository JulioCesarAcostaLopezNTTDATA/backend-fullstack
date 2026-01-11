package com.example.backend.customers.application;

import lombok.Value;

@Value
public class CreateCustomerCommand {
  String name;
  String email;

  String idempotencyKey; // optional
  String requestHash;    // optional
}
