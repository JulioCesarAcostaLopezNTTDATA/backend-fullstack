package com.example.backend.customers.api;

import com.example.backend.customers.application.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomersController {

  private final CreateCustomerUseCase createCustomer;
  private final GetCustomerUseCase getCustomer;
  private final ListCustomersUseCase listCustomers;
  private final UpdateCustomerUseCase updateCustomer;
  private final DeleteCustomerUseCase deleteCustomer;

  public CustomersController(
      CreateCustomerUseCase createCustomer,
      GetCustomerUseCase getCustomer,
      ListCustomersUseCase listCustomers,
      UpdateCustomerUseCase updateCustomer,
      DeleteCustomerUseCase deleteCustomer
  ) {
    this.createCustomer = createCustomer;
    this.getCustomer = getCustomer;
    this.listCustomers = listCustomers;
    this.updateCustomer = updateCustomer;
    this.deleteCustomer = deleteCustomer;
  }

  @PostMapping
  public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest req) {
    var result = createCustomer.handle(new CreateCustomerCommand(req.getName(), req.getEmail()));
    return ResponseEntity.created(URI.create("/customers/" + result.getId()))
        .body(CustomerMappers.toResponse(result));
  }

  @GetMapping("/{id}")
  public CustomerResponse get(@PathVariable String id) {
    return CustomerMappers.toResponse(getCustomer.handle(id));
  }

  @GetMapping
  public List<CustomerResponse> list() {
    return listCustomers.handle().stream().map(CustomerMappers::toResponse).toList();
  }

  @PutMapping("/{id}")
  public CustomerResponse update(@PathVariable String id, @Valid @RequestBody UpdateCustomerRequest req) {
    return CustomerMappers.toResponse(updateCustomer.handle(new UpdateCustomerCommand(id, req.getName(), req.getEmail())));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    deleteCustomer.handle(id);
    return ResponseEntity.noContent().build();
  }
}
