package com.conductor.shared.messaging;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.conductor.shared.contracts.SchemaValidator;
import com.conductor.shared.events.ConductorEvent;
import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.middleware.tenant.TenantContext;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.nats.client.*;
import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventPlatformIntegrationTest {

  private NatsConnectionManager connectionManager;
  private SchemaValidator schemaValidator;
  private AuditLogger auditLogger;
  private EventObservability observability;
  private EventPublisher publisher;
  private EventConsumer consumer;

  @BeforeEach
  void setUp() throws Exception {
    connectionManager = new NatsConnectionManager("nats://localhost:4222");
    schemaValidator = new SchemaValidator();
    schemaValidator.setSchemasPath("../../config/schemas");

    auditLogger = mock(AuditLogger.class);
    observability = new EventObservability(new SimpleMeterRegistry());

    publisher =
        new EventPublisher(
            connectionManager, schemaValidator, auditLogger, observability, "test-service");
    consumer = new EventConsumer(connectionManager, schemaValidator, auditLogger, observability);

    // Auto-provision test streams if NATS is online
    Connection conn = connectionManager.getConnection();
    if (conn != null) {
      try {
        JetStreamManagement jsm = conn.jetStreamManagement();

        // Provision TEST_STREAM
        StreamConfiguration config =
            StreamConfiguration.builder()
                .name("TEST_STREAM")
                .subjects("conductor.*.test.>")
                .storageType(StorageType.File)
                .retentionPolicy(RetentionPolicy.Limits)
                .maxAge(Duration.ofDays(1))
                .replicas(1)
                .build();
        try {
          jsm.addStream(config);
        } catch (Exception e) {
          // Update if already exists
          jsm.updateStream(config);
        }

        // Provision TENANT_STREAM
        StreamConfiguration tenantConfig =
            StreamConfiguration.builder()
                .name("TENANT_STREAM")
                .subjects("conductor.*.tenant.>")
                .storageType(StorageType.File)
                .retentionPolicy(RetentionPolicy.Limits)
                .maxAge(Duration.ofDays(1))
                .replicas(1)
                .build();
        try {
          jsm.addStream(tenantConfig);
        } catch (Exception e) {
          // Update if already exists
          jsm.updateStream(tenantConfig);
        }
      } catch (Exception e) {
        System.out.println(
            "Warning: JetStream management failed, NATS might be starting or not in JetStream mode: "
                + e.getMessage());
      }
    }
  }

  @Test
  void testPublishAndSubscribeSuccess() throws Exception {
    Connection conn = connectionManager.getConnection();
    if (conn == null) {
      System.out.println("NATS is offline. Skipping integration test.");
      return;
    }

    UUID tenantId = UUID.randomUUID();
    TenantContext.setCurrentTenantId(tenantId);

    // Set up schema validator mock/implementation
    // The event type is conductor.test.profile.created
    // We wrote tenant.profile.created.v1.json earlier, so let's publish using domain "tenant",
    // entity "profile", action "created".
    String payload =
        "{\"id\":\"" + tenantId + "\",\"name\":\"Test Tenant\",\"domain\":\"example.com\"}";

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<ConductorEvent<String>> receivedEventRef = new AtomicReference<>();

    // Subscribe (Tenant Scoped subscription)
    Subscription sub =
        consumer.subscribe(
            "test-group",
            "tenant",
            "profile",
            "created",
            tenantId.toString(),
            String.class,
            event -> {
              receivedEventRef.set(event);
              latch.countDown();
            });

    assertNotNull(sub, "Subscription should be established");

    // Publish event
    boolean published = publisher.publish("tenant", "profile", "created", "v1", payload);
    assertTrue(published, "Event should be published successfully");

    // Wait for handler execution
    boolean completed = latch.await(10, TimeUnit.SECONDS);
    assertTrue(completed, "Message handler should execute within timeout");

    ConductorEvent<String> receivedEvent = receivedEventRef.get();
    assertNotNull(receivedEvent);
    assertEquals(tenantId.toString(), receivedEvent.getTenantId());
    assertTrue(receivedEvent.getPayload().contains("Test Tenant"));

    consumer.unsubscribe(sub);
    TenantContext.clear();
  }

  @Test
  void testTenantIsolationEnforced() throws Exception {
    Connection conn = connectionManager.getConnection();
    if (conn == null) {
      return;
    }

    UUID tenantA = UUID.randomUUID();
    UUID tenantB = UUID.randomUUID();

    CountDownLatch latchA = new CountDownLatch(1);
    AtomicReference<ConductorEvent<String>> receivedEventRefA = new AtomicReference<>();

    // Subscribe to Tenant A only
    Subscription subA =
        consumer.subscribe(
            "tenant-a-group",
            "tenant",
            "profile",
            "created",
            tenantA.toString(),
            String.class,
            event -> {
              receivedEventRefA.set(event);
              latchA.countDown();
            });

    // Publish event for Tenant B
    TenantContext.setCurrentTenantId(tenantB);
    String payloadB = "{\"id\":\"" + tenantB + "\",\"name\":\"Tenant B\",\"domain\":\"b.com\"}";
    boolean publishedB = publisher.publish("tenant", "profile", "created", "v1", payloadB);
    assertTrue(publishedB);

    // Verify Tenant A subscriber did NOT receive Tenant B's event
    boolean completedA = latchA.await(3, TimeUnit.SECONDS);
    assertFalse(completedA, "Tenant A subscriber should NOT receive Tenant B event");
    assertNull(receivedEventRefA.get());

    // Publish event for Tenant A
    TenantContext.setCurrentTenantId(tenantA);
    String payloadA = "{\"id\":\"" + tenantA + "\",\"name\":\"Tenant A\",\"domain\":\"a.com\"}";
    boolean publishedA = publisher.publish("tenant", "profile", "created", "v1", payloadA);
    assertTrue(publishedA);

    // Verify Tenant A subscriber receives Tenant A's event
    boolean completedA2 = latchA.await(5, TimeUnit.SECONDS);
    assertTrue(completedA2, "Tenant A subscriber should receive Tenant A event");
    assertNotNull(receivedEventRefA.get());
    assertEquals(tenantA.toString(), receivedEventRefA.get().getTenantId());

    consumer.unsubscribe(subA);
    TenantContext.clear();
  }
}
