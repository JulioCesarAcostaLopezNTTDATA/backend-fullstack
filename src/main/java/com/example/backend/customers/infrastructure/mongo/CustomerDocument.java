package com.example.backend.customers.infrastructure.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "customers")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDocument {
  @Id
  private String id;
  private String name;

  @Indexed(unique = true)
  private String email;

  private Instant createdAt;
  private Instant updatedAt;
}
