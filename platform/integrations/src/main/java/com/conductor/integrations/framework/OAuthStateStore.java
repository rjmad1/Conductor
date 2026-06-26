package com.conductor.integrations.framework;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Short-lived server-side store for OAuth CSRF state tokens.
 *
 * <p>When an OAuth authorization is initiated, a cryptographically-random state token is generated
 * and stored here along with the integrationId and tenantId it belongs to. The callback endpoint
 * validates the state token exists in this store and consumes it (one-time use). Tokens that are
 * never redeemed expire after {@code oauth.state.ttl-seconds}.
 *
 * <p>LIMITATION: this is process-local. In a horizontally-scaled deployment, sticky sessions or a
 * shared store (e.g. Redis) should be used instead. Flag: oauth.state.distributed=true.
 */
@Component
public class OAuthStateStore {

  private static final Logger log = LoggerFactory.getLogger(OAuthStateStore.class);

  private final long ttlSeconds;
  private final Map<String, PendingState> store = new ConcurrentHashMap<>();

  public OAuthStateStore(@Value("${oauth.state.ttl-seconds:600}") long ttlSeconds) {
    this.ttlSeconds = ttlSeconds;
  }

  /**
   * Generates a random state token, stores it, and returns the token for inclusion in the
   * authorization redirect URL.
   */
  public String generate(UUID integrationId, UUID tenantId) {
    String token = UUID.randomUUID().toString();
    Instant expiry = Instant.now().plusSeconds(ttlSeconds);
    store.put(token, new PendingState(integrationId, tenantId, expiry));
    pruneExpired();
    log.debug("OAuth state token created for integration={}", integrationId);
    return token;
  }

  /**
   * Validates the token and, if valid and unexpired, removes it and returns the associated state.
   * Returns empty if the token is unknown, expired, or already used.
   */
  public Optional<PendingState> consume(String token) {
    if (token == null) {
      return Optional.empty();
    }
    PendingState state = store.remove(token);
    if (state == null) {
      log.warn("OAuth state token not found or already consumed");
      return Optional.empty();
    }
    if (state.expiresAt().isBefore(Instant.now())) {
      log.warn("OAuth state token expired for integration={}", state.integrationId());
      return Optional.empty();
    }
    return Optional.of(state);
  }

  private void pruneExpired() {
    Instant now = Instant.now();
    store.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
  }

  public record PendingState(UUID integrationId, UUID tenantId, Instant expiresAt) {}
}
