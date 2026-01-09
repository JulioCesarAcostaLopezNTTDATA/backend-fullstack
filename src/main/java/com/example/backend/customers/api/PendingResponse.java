package com.example.backend.customers.api;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PendingResponse {
  String status; // PENDING
}
