package com.example.backend.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.backend.shared.observability.ObservabilityMeters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper om, SecurityProperties props, ObservabilityMeters meters) throws Exception {

    http.csrf(csrf -> csrf.disable());

    // CORS estricto (si aplica). Si no hay origins configurados, se deja deshabilitado.
    http.cors(cors -> cors.configurationSource(req -> {
      if (props.getCorsAllowedOrigins() == null || props.getCorsAllowedOrigins().isEmpty()) return null;
      CorsConfiguration cfg = new CorsConfiguration();
      cfg.setAllowedOrigins(props.getCorsAllowedOrigins());
      cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
      cfg.setAllowedHeaders(List.of("Authorization","Content-Type","Idempotency-Key","X-Correlation-Id"));
      cfg.setExposedHeaders(List.of("X-Correlation-Id"));
      cfg.setAllowCredentials(false);
      cfg.setMaxAge(3600L);
      return cfg;
    }));

    http.exceptionHandling(e -> e
        .authenticationEntryPoint(SecurityErrorHandlers.authenticationEntryPoint(om, meters))
        .accessDeniedHandler(SecurityErrorHandlers.accessDeniedHandler(om, meters))
    );

    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
        .requestMatchers(HttpMethod.GET, "/customers/**").hasAuthority("SCOPE_customers:read")
        .requestMatchers(HttpMethod.POST, "/customers/**").hasAuthority("SCOPE_customers:write")
        .requestMatchers(HttpMethod.PUT, "/customers/**").hasAuthority("SCOPE_customers:write")
        .requestMatchers(HttpMethod.DELETE, "/customers/**").hasAuthority("SCOPE_customers:write")
        .anyRequest().authenticated()
    );

    http.oauth2ResourceServer(oauth -> oauth
        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
    );

    return http.build();
  }

  /**
   * DEV: si configuras `security.hmac-secret`, valida HS256 localmente.
   * PROD: si configuras `spring.security.oauth2.resourceserver.jwt.issuer-uri` o `jwk-set-uri`,
   * Spring Boot auto-configura el JwtDecoder.
   */
  @Bean
  @ConditionalOnProperty(prefix = "security", name = "hmac-secret")
  JwtDecoder hmacJwtDecoder(SecurityProperties props) {
    var key = new SecretKeySpec(props.getHmacSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).build();

    OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
        JwtValidators.createDefault(), new AudienceValidator(props.getAudience())
    );
    decoder.setJwtValidator(validator);
    return decoder;
  }

  /**
   * PROD: issuer-uri (RS256/JWKS) con validación adicional de audience.
   */
  @Bean
  @ConditionalOnProperty(prefix = "spring.security.oauth2.resourceserver.jwt", name = "issuer-uri")
  JwtDecoder issuerJwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
                              SecurityProperties props) {
    NimbusJwtDecoder decoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);

    OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
        JwtValidators.createDefaultWithIssuer(issuerUri), new AudienceValidator(props.getAudience())
    );
    decoder.setJwtValidator(validator);
    return decoder;
  }

  /**
   * PROD: jwk-set-uri (RS256/JWKS) con validación de exp/nbf + aud.
   */
  @Bean
  @ConditionalOnProperty(prefix = "spring.security.oauth2.resourceserver.jwt", name = "jwk-set-uri")
  JwtDecoder jwkSetJwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri,
                              SecurityProperties props) {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

    OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
        JwtValidators.createDefault(), new AudienceValidator(props.getAudience())
    );
    decoder.setJwtValidator(validator);
    return decoder;
  }

  private JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter c = new JwtAuthenticationConverter();
    c.setJwtGrantedAuthoritiesConverter(new JwtAuthoritiesConverter());
    return c;
  }
}
