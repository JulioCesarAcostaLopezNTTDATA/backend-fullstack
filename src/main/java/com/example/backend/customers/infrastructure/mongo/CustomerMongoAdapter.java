package com.example.backend.customers.infrastructure.mongo;

import com.example.backend.customers.domain.Customer;
import com.example.backend.customers.domain.ports.CustomerRepository;
import com.example.backend.shared.domain.DomainException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@Repository
public class CustomerMongoAdapter implements CustomerRepository {

  private final SpringDataCustomerMongoRepository mongo;

  public CustomerMongoAdapter(SpringDataCustomerMongoRepository mongo) {
    this.mongo = mongo;
  }

  @Override
  public Customer save(Customer customer) {
    try {
      CustomerDocument saved = mongo.save(toDoc(customer));
      return toDomain(saved);
    } catch (DuplicateKeyException e) {
      throw new DomainException("DUPLICATE_EMAIL", "email already exists", Map.of("email", customer.getEmail()));
    }
  }

  @Override
  public Optional<Customer> findById(String id) {
    return mongo.findById(id).map(CustomerMongoAdapter::toDomain);
  }

  @Override
  public List<Customer> findAll() {
    return mongo.findAll().stream().map(CustomerMongoAdapter::toDomain).toList();
  }

  @Override
  public boolean existsByEmail(String email) {
    return mongo.existsByEmail(email);
  }

  @Override
  public boolean deleteById(String id) {
    if (!mongo.existsById(id)) return false;
    mongo.deleteById(id);
    return true;
  }

  private static CustomerDocument toDoc(Customer c) {
    return CustomerDocument.builder()
        .id(c.getId())
        .name(c.getName())
        .email(c.getEmail())
        .createdAt(c.getCreatedAt())
        .updatedAt(c.getUpdatedAt())
        .build();
  }

  private static Customer toDomain(CustomerDocument d) {
    return new Customer(d.getId(), d.getName(), d.getEmail(), d.getCreatedAt(), d.getUpdatedAt());
  }
}
