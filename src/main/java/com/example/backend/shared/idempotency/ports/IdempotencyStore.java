package com.example.backend.shared.idempotency.ports;

import com.example.backend.shared.idempotency.IdempotencyRecord;

import java.time.Instant;
import java.util.Optional;

public interface IdempotencyStore {

  /**
   * Intenta crear un registro en estado PROCESSING de forma atómica.
   * @return true si se creó; false si ya existía.
   */
  boolean tryCreateProcessing(String key, String requestHash, Instant expireAt);

  Optional<IdempotencyRecord> findByKey(String key);

  void markSuccess(String key, String resultId);

  void markFailed(String key);
}
