package com.example.backend.customers.infrastructure.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringDataCustomerMongoRepository extends MongoRepository<CustomerDocument, String> {
  boolean existsByEmail(String email);
}
