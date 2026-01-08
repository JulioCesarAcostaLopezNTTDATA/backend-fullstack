package com.example.backend.customers.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCustomerRequest {
  @NotBlank @Size(min = 2, max = 80)
  private String name;

  @NotBlank @Email
  private String email;
}
