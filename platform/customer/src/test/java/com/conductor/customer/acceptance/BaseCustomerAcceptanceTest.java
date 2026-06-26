package com.conductor.customer.acceptance;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.conductor.shared.messaging.EventPublisher;
import com.conductor.shared.messaging.NatsConnectionManager;
import com.conductor.shared.middleware.tenant.NatsEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for all customer module acceptance tests.
 *
 * <p>Uses a real PostgreSQL container. Only NATS, Keycloak/JWT, and other infrastructure
 * dependencies are replaced with test doubles. All business logic runs real.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("acceptance")
public abstract class BaseCustomerAcceptanceTest {

  static final PostgreSQLContainer<?> POSTGRES;

  static {
    POSTGRES =
        new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("conductor_customer_acceptance")
            .withUsername("conductor")
            .withPassword("conductor_test");
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void configurePostgres(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    registry.add("spring.flyway.enabled", () -> "true");
  }

  // ── Infrastructure mocks ───────────────────────────────────────────────────

  @MockBean protected JwtDecoder jwtDecoder;
  @MockBean protected NatsConnectionManager natsConnectionManager;
  @MockBean protected NatsEventPublisher natsEventPublisher;
  @MockBean protected EventPublisher eventPublisher;

  // ── Shared test helpers ────────────────────────────────────────────────────

  @Autowired protected MockMvc mockMvc;
  @Autowired protected ObjectMapper objectMapper;

  protected SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor platformAdminJwt(
      UUID tenantId) {
    return jwt()
        .jwt(
            j ->
                j.subject("platform-admin")
                    .issuer("http://localhost:8080/realms/conductor-" + tenantId))
        .authorities(
            new SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN"),
            new SimpleGrantedAuthority("ROLE_TENANT_ADMIN"));
  }

  protected SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor platformAdminJwt() {
    return platformAdminJwt(UUID.randomUUID());
  }

  protected SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor tenantAdminJwt(
      UUID tenantId) {
    return jwt()
        .jwt(
            j ->
                j.subject("tenant-admin")
                    .issuer("http://localhost:8080/realms/conductor-" + tenantId))
        .authorities(new SimpleGrantedAuthority("ROLE_TENANT_ADMIN"));
  }

  protected SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor tenantAdminJwt() {
    return tenantAdminJwt(UUID.randomUUID());
  }

  protected MockHttpServletRequestBuilder withTenantContext(
      MockHttpServletRequestBuilder builder, UUID tenantId) {
    return builder.header("X-Tenant-ID", tenantId.toString()).header("X-User-ID", "test-user-1");
  }

  protected ResultActions performAs(
      MockHttpServletRequestBuilder builder,
      SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtPostProcessor,
      UUID tenantId)
      throws Exception {
    // Rebuild the JWT with the tenant-scoped issuer so TenantContextFilter can resolve the tenant.
    var tenantJwt =
        jwt()
            .jwt(
                j ->
                    j.subject("test-user")
                        .issuer("http://localhost:8080/realms/conductor-" + tenantId))
            .authorities(
                new SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN"),
                new SimpleGrantedAuthority("ROLE_TENANT_ADMIN"));
    return mockMvc.perform(withTenantContext(builder.with(tenantJwt), tenantId));
  }

  protected String json(Object value) throws Exception {
    return objectMapper.writeValueAsString(value);
  }
}
