package com.example.backend.shared.idempotency.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringDataIdempotencyMongoRepository extends MongoRepository<IdempotencyDocument, String> {
}
