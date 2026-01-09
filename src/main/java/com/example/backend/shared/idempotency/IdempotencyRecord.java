package com.example.backend.shared.idempotency;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Modelo de dominio para idempotencia (sin dependencias a Mongo/Spring).
 */
@Value
@Builder
public class IdempotencyRecord {
  String key;
  String requestHash;
  IdempotencyStatus status;
  String resultId; // e.g. customerId
  Instant createdAt;
  Instant updatedAt;
  Instant expireAt;
}
