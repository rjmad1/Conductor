package com.conductor.identity;

import static org.junit.jupiter.api.Assertions.*;

import com.conductor.shared.security.AuthorizationEngine;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class AuthorizationEngineTest {

  private final AuthorizationEngine authz = new AuthorizationEngine();

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void givenNoAuthentication_whenChecked_thenReturnFalse() {
    assertFalse(authz.hasPermission("workflows:read"));
  }

  @Test
  void givenExactPermission_whenChecked_thenReturnTrue() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "user", "password", List.of(new SimpleGrantedAuthority("workflows:read"))));
    assertTrue(authz.hasPermission("workflows:read"));
  }

  @Test
  void givenWildcardPermission_whenChecked_thenReturnTrue() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "user", "password", List.of(new SimpleGrantedAuthority("workflows:*"))));
    assertTrue(authz.hasPermission("workflows:read"));
    assertTrue(authz.hasPermission("workflows:write"));
    assertFalse(authz.hasPermission("contacts:read"));
  }

  @Test
  void givenGlobalAdminPermission_whenChecked_thenReturnTrue() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "user", "password", List.of(new SimpleGrantedAuthority("*:*"))));
    assertTrue(authz.hasPermission("workflows:read"));
    assertTrue(authz.hasPermission("contacts:write"));
  }

  @Test
  void givenRolePrefix_whenChecked_thenMatchWithoutPrefix() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "user", "password", List.of(new SimpleGrantedAuthority("ROLE_WORKFLOWS:WRITE"))));
    assertTrue(authz.hasPermission("workflows:write"));
  }

  @Test
  void givenMatchingTenantIds_whenCheckedResourceOwner_thenReturnTrue() {
    assertTrue(authz.isResourceOwner("tenant-123", "tenant-123"));
  }

  @Test
  void givenMismatchingTenantIds_whenCheckedResourceOwner_thenReturnFalse() {
    assertFalse(authz.isResourceOwner("tenant-123", "tenant-456"));
  }
}
