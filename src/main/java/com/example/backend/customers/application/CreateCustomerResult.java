package com.example.backend.customers.application;

public record CreateCustomerResult(Type type, CustomerView customer) {
  public enum Type { CREATED, REPLAY_SUCCESS, IN_PROGRESS }

  public static CreateCustomerResult created(CustomerView c) {
    return new CreateCustomerResult(Type.CREATED, c);
  }

  public static CreateCustomerResult replaySuccess(CustomerView c) {
    return new CreateCustomerResult(Type.REPLAY_SUCCESS, c);
  }

  public static CreateCustomerResult inProgress() {
    return new CreateCustomerResult(Type.IN_PROGRESS, null);
  }
}
