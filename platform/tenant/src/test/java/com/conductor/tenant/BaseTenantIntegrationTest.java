package com.conductor.tenant;

import com.conductor.shared.messaging.NatsConnectionManager;
import com.conductor.shared.middleware.tenant.NatsEventPublisher;
import com.conductor.tenant.service.KeycloakAdminService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(
    io.micrometer.core.instrument.simple.SimpleMeterRegistry.class)
public abstract class BaseTenantIntegrationTest {

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.datasource.url",
        () ->
            "jdbc:h2:mem:tenanttest;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
    registry.add("spring.datasource.username", () -> "sa");
    registry.add("spring.datasource.password", () -> "");
    registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    registry.add("spring.flyway.enabled", () -> "false");
  }

  @MockBean protected KeycloakAdminService keycloakAdminService;
  @MockBean protected NatsConnectionManager natsConnectionManager;
  @MockBean protected NatsEventPublisher natsEventPublisher;
  @MockBean protected JwtDecoder jwtDecoder;
}
