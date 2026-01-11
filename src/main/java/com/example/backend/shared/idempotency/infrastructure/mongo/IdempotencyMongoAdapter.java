package com.example.backend.shared.idempotency.infrastructure.mongo;

import com.example.backend.shared.idempotency.IdempotencyRecord;
import com.example.backend.shared.idempotency.IdempotencyStatus;
import com.example.backend.shared.idempotency.ports.IdempotencyStore;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public class IdempotencyMongoAdapter implements IdempotencyStore {

  private final SpringDataIdempotencyMongoRepository repo;

  public IdempotencyMongoAdapter(SpringDataIdempotencyMongoRepository repo) {
    this.repo = repo;
  }

  @Override
  public boolean tryCreateProcessing(String key, String requestHash, Instant expireAt) {
    try {
      Instant now = Instant.now();
      repo.insert(IdempotencyDocument.builder()
          .key(key)
          .requestHash(requestHash)
          .status(IdempotencyStatus.PROCESSING)
          .createdAt(now)
          .updatedAt(now)
          .expireAt(expireAt)
          .build());
      return true;
    } catch (DuplicateKeyException e) {
      return false;
    }
  }

  @Override
  public Optional<IdempotencyRecord> findByKey(String key) {
    return repo.findById(key).map(IdempotencyMongoAdapter::toDomain);
  }

  @Override
  public void markSuccess(String key, String resultId) {
    repo.findById(key).ifPresent(doc -> {
      doc.setStatus(IdempotencyStatus.SUCCESS);
      doc.setResultId(resultId);
      doc.setUpdatedAt(Instant.now());
      repo.save(doc);
    });
  }

  @Override
  public void markFailed(String key) {
    repo.findById(key).ifPresent(doc -> {
      doc.setStatus(IdempotencyStatus.FAILED);
      doc.setUpdatedAt(Instant.now());
      repo.save(doc);
    });
  }

  private static IdempotencyRecord toDomain(IdempotencyDocument d) {
    return IdempotencyRecord.builder()
        .key(d.getKey())
        .requestHash(d.getRequestHash())
        .status(d.getStatus())
        .resultId(d.getResultId())
        .createdAt(d.getCreatedAt())
        .updatedAt(d.getUpdatedAt())
        .expireAt(d.getExpireAt())
        .build();
  }
}
