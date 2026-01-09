package com.example.backend.shared.idempotency;

import com.example.backend.shared.domain.DomainException;
import com.example.backend.shared.idempotency.ports.IdempotencyStore;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
public class IdempotencyService {

  private final IdempotencyStore store;
  private final Clock clock = Clock.systemUTC();

  public IdempotencyService(IdempotencyStore store) {
    this.store = store;
  }

  public IdempotencyDecision begin(String key, String requestHash) {
    Instant now = Instant.now(clock);
    Instant expireAt = now.plus(24, ChronoUnit.HOURS); // TTL 24h

    boolean created = store.tryCreateProcessing(key, requestHash, expireAt);
    if (created) return IdempotencyDecision.created();

    IdempotencyRecord existing = store.findByKey(key)
        .orElseThrow(() -> new DomainException("IDEMPOTENCY_INCONSISTENT", "idempotency record not found", Map.of("key", key)));

    if (!existing.getRequestHash().equals(requestHash)) {
      throw new DomainException("IDEMPOTENCY_CONFLICT", "idempotency key already used with different request", Map.of("key", key));
    }

    return switch (existing.getStatus()) {
      case PROCESSING -> IdempotencyDecision.inProgress();
      case SUCCESS -> IdempotencyDecision.replaySuccess(existing.getResultId());
      case FAILED -> IdempotencyDecision.failed();
    };
  }

  public void markSuccess(String key, String resultId) {
    store.markSuccess(key, resultId);
  }

  public void markFailed(String key) {
    store.markFailed(key);
  }

  public sealed interface IdempotencyDecision permits Created, InProgress, ReplaySuccess, Failed {
    static IdempotencyDecision created() { return new Created(); }
    static IdempotencyDecision inProgress() { return new InProgress(); }
    static IdempotencyDecision replaySuccess(String resultId) { return new ReplaySuccess(resultId); }
    static IdempotencyDecision failed() { return new Failed(); }
  }

  public static final class Created implements IdempotencyDecision {}
  public static final class InProgress implements IdempotencyDecision {}
  public static final class Failed implements IdempotencyDecision {}
  public record ReplaySuccess(String resultId) implements IdempotencyDecision {}
}
