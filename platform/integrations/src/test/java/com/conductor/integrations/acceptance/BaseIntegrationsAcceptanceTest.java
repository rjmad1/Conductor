package com.conductor.integrations.acceptance;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.conductor.shared.messaging.EventPublisher;
import com.conductor.shared.messaging.NatsConnectionManager;
import com.conductor.shared.middleware.tenant.NatsEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integrations module acceptance tests.
 *
 * <p>Uses PostgreSQL Testcontainer for real Flyway migrations. NATS and Keycloak are stubbed.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("acceptance")
public abstract class BaseIntegrationsAcceptanceTest {

  @Container
  @SuppressWarnings("resource")
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:15-alpine")
          .withDatabaseName("conductor_integrations_acceptance")
          .withUsername("conductor")
          .withPassword("conductor_test");

  @DynamicPropertySource
  static void configurePostgres(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    registry.add("spring.flyway.enabled", () -> "true");
  }

  @MockBean protected JwtDecoder jwtDecoder;
  @MockBean protected NatsConnectionManager natsConnectionManager;
  @MockBean protected NatsEventPublisher natsEventPublisher;
  @MockBean protected EventPublisher eventPublisher;

  @Autowired protected MockMvc mockMvc;
  @Autowired protected ObjectMapper objectMapper;

  protected SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor tenantAdminJwt() {
    return jwt()
        .jwt(j -> j.subject("tenant-admin"))
        .authorities(new SimpleGrantedAuthority("ROLE_TENANT_ADMIN"));
  }

  protected String json(Object value) throws Exception {
    return objectMapper.writeValueAsString(value);
  }
}
