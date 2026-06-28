package com.conductor.workflow.acceptance;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.conductor.shared.execution.provider.ProviderClient;
import com.conductor.shared.messaging.EventPublisher;
import com.conductor.shared.messaging.NatsConnectionManager;
import com.conductor.shared.middleware.tenant.NatsEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.WorkerFactory;
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
 * Base class for all workflow module acceptance tests.
 *
 * <p>Uses a real PostgreSQL container via Testcontainers so Flyway migrations run against the
 * actual database engine. Only true external/infrastructure dependencies (Temporal cluster,
 * Keycloak, NATS, Meta WhatsApp API) are replaced with test doubles. All business logic, JPA
 * repositories, and Spring Security filters run in the real application context.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("acceptance")
@SuppressWarnings("resource")
public abstract class BaseAcceptanceTest {

  // Singleton container — started once per JVM. Not managed by @Testcontainers so it is never
  // stopped between test classes, preventing "Connection refused" when the Spring context is
  // reused.
  static final PostgreSQLContainer<?> POSTGRES;

  static {
    POSTGRES =
        new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("conductor_acceptance")
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
    // Let Flyway run real migrations against PostgreSQL — not H2.
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    registry.add("spring.flyway.enabled", () -> "true");
    // Prevent Temporal worker startup during context load.
    registry.add("temporal.workers.enabled", () -> "false");
  }

  // ── Infrastructure mocks (external dependencies only) ─────────────────────

  /** Temporal cluster — replaced with TestWorkflowEnvironment in execution tests. */
  @MockBean protected WorkflowServiceStubs workflowServiceStubs;

  @MockBean protected WorkflowClient workflowClient;

  // Two WorkerFactory beans exist: workerFactory (base) and startWorkerFactory
  // (initMethod="start").
  // Both must be mocked by name — @MockBean on the type alone is ambiguous.
  @MockBean(name = "workerFactory")
  protected WorkerFactory workerFactory;

  @MockBean(name = "startWorkerFactory")
  protected WorkerFactory startWorkerFactory;

  /** Keycloak OIDC — JWT validation delegated to Spring Security Test's jwt() helper. */
  @MockBean protected JwtDecoder jwtDecoder;

  /** NATS JetStream — not available in unit-isolated acceptance runs. */
  @MockBean protected NatsConnectionManager natsConnectionManager;

  @MockBean protected NatsEventPublisher natsEventPublisher;
  @MockBean protected EventPublisher eventPublisher;
  @MockBean protected io.nats.client.Connection natsConnection;

  /** Meta WhatsApp Cloud API — the sole external commercial API in the journey. */
  @MockBean protected ProviderClient providerClient;

  // ── Shared test infrastructure ─────────────────────────────────────────────

  @Autowired protected MockMvc mockMvc;
  @Autowired protected ObjectMapper objectMapper;

  // ── Auth helpers ───────────────────────────────────────────────────────────

  protected SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor platformAdminJwt(
      UUID tenantId) {
    return jwt()
        .jwt(
            j ->
                j.subject("platform-admin")
                    .claim("email", "admin@conductor.test")
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
                    .claim("email", "admin@tenant.test")
                    .issuer("http://localhost:8080/realms/conductor-" + tenantId))
        .authorities(new SimpleGrantedAuthority("ROLE_TENANT_ADMIN"));
  }

  protected SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor tenantAdminJwt() {
    return tenantAdminJwt(UUID.randomUUID());
  }

  // ── Request builder helpers ────────────────────────────────────────────────

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
    // The issuer format mirrors the Keycloak realm convention: conductor-{tenantUUID}.
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

  protected UUID randomTenantId() {
    return UUID.randomUUID();
  }
}
