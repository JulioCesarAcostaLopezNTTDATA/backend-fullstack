package com.example.backend.customers.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

final class RequestHashing {
  private RequestHashing() {}

  static String sha256ForCreate(CreateCustomerRequest req) {
    String canonical = req.getName().trim() + "|" + req.getEmail().trim().toLowerCase();
    return sha256Hex(canonical);
  }

  private static String sha256Hex(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(digest.length * 2);
      for (byte b : digest) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to hash request", e);
    }
  }
}
