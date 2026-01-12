package com.example.backend.customers.domain.ports;

import com.example.backend.customers.domain.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
  Customer save(Customer customer);
  Optional<Customer> findById(String id);
  List<Customer> findAll();
  boolean existsByEmail(String email);
  boolean deleteById(String id);
}
