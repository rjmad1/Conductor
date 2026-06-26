package com.conductor.integrations;

import static org.junit.jupiter.api.Assertions.*;

import com.conductor.integrations.framework.OAuthStateStore;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class OAuthStateStoreTest {

  private OAuthStateStore store() {
    return new OAuthStateStore(600);
  }

  @Test
  void generateAndConsume_validToken_returnsState() {
    OAuthStateStore s = store();
    UUID integrationId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    String token = s.generate(integrationId, tenantId);
    assertNotNull(token);
    Optional<OAuthStateStore.PendingState> result = s.consume(token);
    assertTrue(result.isPresent());
    assertEquals(integrationId, result.get().integrationId());
    assertEquals(tenantId, result.get().tenantId());
  }

  @Test
  void consume_secondTime_returnsEmpty() {
    OAuthStateStore s = store();
    String token = s.generate(UUID.randomUUID(), UUID.randomUUID());
    s.consume(token);
    Optional<OAuthStateStore.PendingState> second = s.consume(token);
    assertTrue(second.isEmpty(), "State token must be single-use");
  }

  @Test
  void consume_unknownToken_returnsEmpty() {
    Optional<OAuthStateStore.PendingState> result = store().consume("totally-unknown-token");
    assertTrue(result.isEmpty());
  }

  @Test
  void consume_nullToken_returnsEmpty() {
    assertTrue(store().consume(null).isEmpty());
  }

  @Test
  void consume_expiredToken_returnsEmpty() throws InterruptedException {
    OAuthStateStore s = new OAuthStateStore(1);
    String token = s.generate(UUID.randomUUID(), UUID.randomUUID());
    Thread.sleep(1100);
    assertTrue(s.consume(token).isEmpty(), "Expired token must be rejected");
  }

  @Test
  void generate_doesNotExposeIntegrationIdInToken() {
    UUID integrationId = UUID.randomUUID();
    String token = store().generate(integrationId, UUID.randomUUID());
    assertFalse(
        token.contains(integrationId.toString()),
        "State token must be opaque and not contain the integrationId");
  }

  @Test
  void distinctCalls_produceDistinctTokens() {
    OAuthStateStore s = store();
    String t1 = s.generate(UUID.randomUUID(), UUID.randomUUID());
    String t2 = s.generate(UUID.randomUUID(), UUID.randomUUID());
    assertNotEquals(t1, t2);
  }
}
