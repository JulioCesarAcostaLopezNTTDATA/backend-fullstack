package com.example.backend.shared.idempotency.infrastructure.mongo;

import com.example.backend.shared.idempotency.IdempotencyStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "idempotency_keys")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyDocument {
  @Id
  private String key;

  private String requestHash;

  private IdempotencyStatus status;

  private String resultId;

  private Instant createdAt;
  private Instant updatedAt;

  @Indexed(expireAfterSeconds = 0) // TTL index: expires at expireAt value
  private Instant expireAt;
}
