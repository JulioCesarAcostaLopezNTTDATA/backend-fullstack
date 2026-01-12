package com.example.backend.shared.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Convierte roles + scopes desde claims comunes.
 *
 * Scopes:
 *  - "scope" (string con espacios) o "scp" (array)
 *  - Authorities: SCOPE_{scope}
 *
 * Roles:
 *  - "roles" (array) o "role" (string)
 *  - "realm_access.roles" (Keycloak)
 *  - Authorities: ROLE_{role}
 */
public class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    Set<String> scopes = extractScopes(jwt);
    Set<String> roles = extractRoles(jwt);

    List<GrantedAuthority> authorities = new ArrayList<>();
    for (String s : scopes) authorities.add(new SimpleGrantedAuthority("SCOPE_" + s));
    for (String r : roles) authorities.add(new SimpleGrantedAuthority("ROLE_" + r));

    return authorities;
  }

  private Set<String> extractScopes(Jwt jwt) {
    Object scope = jwt.getClaims().get("scope");
    Object scp = jwt.getClaims().get("scp");

    Set<String> result = new HashSet<>();
    if (scope instanceof String s) {
      result.addAll(Arrays.stream(s.split("\s+")).filter(x -> !x.isBlank()).collect(Collectors.toSet()));
    }
    if (scp instanceof Collection<?> c) {
      for (Object o : c) if (o != null) result.add(o.toString());
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private Set<String> extractRoles(Jwt jwt) {
    Set<String> result = new HashSet<>();

    Object roles = jwt.getClaims().get("roles");
    if (roles instanceof Collection<?> c) {
      for (Object o : c) if (o != null) result.add(o.toString());
    }
    Object role = jwt.getClaims().get("role");
    if (role instanceof String s && !s.isBlank()) result.add(s);

    Object realmAccess = jwt.getClaims().get("realm_access");
    if (realmAccess instanceof Map<?,?> m) {
      Object rr = m.get("roles");
      if (rr instanceof Collection<?> c2) {
        for (Object o : c2) if (o != null) result.add(o.toString());
      }
    }

    // normaliza: uppercase
    return result.stream().map(r -> r.toUpperCase(Locale.ROOT)).collect(Collectors.toSet());
  }
}
