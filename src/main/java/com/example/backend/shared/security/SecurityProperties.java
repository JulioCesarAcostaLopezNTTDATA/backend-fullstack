package com.example.backend.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

  /**
   * Expected audience (aud) for JWTs (optional but recommended).
   */
  private String audience;

  /**
   * If set, uses HS256 validation with this secret (DEV only).
   * If not set, configure issuer-uri or jwk-set-uri via Spring properties.
   */
  private String hmacSecret;

  /**
   * CORS allowed origins for public API.
   */
  private List<String> corsAllowedOrigins = List.of();

  public String getAudience() { return audience; }
  public void setAudience(String audience) { this.audience = audience; }

  public String getHmacSecret() { return hmacSecret; }
  public void setHmacSecret(String hmacSecret) { this.hmacSecret = hmacSecret; }

  public List<String> getCorsAllowedOrigins() { return corsAllowedOrigins; }
  public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) { this.corsAllowedOrigins = corsAllowedOrigins; }
}
