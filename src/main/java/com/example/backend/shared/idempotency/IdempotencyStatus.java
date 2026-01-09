package com.example.backend.shared.idempotency;

public enum IdempotencyStatus {
  PROCESSING,
  SUCCESS,
  FAILED
}
