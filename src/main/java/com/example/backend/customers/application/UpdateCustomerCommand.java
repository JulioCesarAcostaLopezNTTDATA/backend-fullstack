package com.example.backend.customers.application;

import lombok.Value;

@Value
public class UpdateCustomerCommand {
  String id;
  String name;
  String email;
}
