package com.conductor.integrations.webhooks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-process replay protection using a time-bounded event-ID cache.
 *
 * LIMITATION: This implementation is process-local. In a horizontally-scaled deployment,
 * a webhook may be replayed to a different instance and bypass this check. If distributed
 * replay protection is required, replace the ConcurrentHashMap with a distributed store
 * (e.g. Redis SETNX with TTL, or NATS KV). Feature-flag: webhook.replay.distributed=true.
 */
@Component
public class WebhookReplayProtector {

    private static final Logger log = LoggerFactory.getLogger(WebhookReplayProtector.class);

    /** Webhook providers typically use a 5-minute replay window; configurable here. */
    private final Duration replayWindow;
    private final int maxCacheSize;

    private final Map<String, Instant> processedWebhooks = new ConcurrentHashMap<>();

    public WebhookReplayProtector(
            @Value("${webhook.replay.window-seconds:300}") int windowSeconds,
            @Value("${webhook.replay.max-cache-size:10000}") int maxCacheSize) {
        this.replayWindow = Duration.ofSeconds(windowSeconds);
        this.maxCacheSize = maxCacheSize;
    }

    /**
     * Returns true if the event has already been processed within the replay window.
     * Uses atomic putIfAbsent to eliminate the TOCTOU race condition in the previous
     * containsKey + put pattern.
     */
    public boolean isDuplicate(String eventId) {
        if (eventId == null) {
            return false;
        }

        Instant now = Instant.now();
        Instant existing = processedWebhooks.putIfAbsent(eventId, now);

        if (existing != null) {
            // Key was already present — check whether it's within the replay window
            if (existing.isAfter(now.minus(replayWindow))) {
                log.warn("Replay detected for event id={}", eventId);
                return true;
            }
            // The original entry is outside the window; update and allow through
            processedWebhooks.put(eventId, now);
            return false;
        }

        // Prune expired entries when approaching capacity to prevent unbounded growth
        if (processedWebhooks.size() >= maxCacheSize) {
            pruneExpired(now);
        }

        return false;
    }

    private void pruneExpired(Instant now) {
        Instant cutoff = now.minus(replayWindow);
        int removed = 0;
        for (Map.Entry<String, Instant> entry : processedWebhooks.entrySet()) {
            if (entry.getValue().isBefore(cutoff)) {
                processedWebhooks.remove(entry.getKey(), entry.getValue());
                removed++;
            }
        }
        log.debug("Pruned {} expired replay-protection entries", removed);
    }

    /** Visible for testing. */
    public int cacheSize() {
        return processedWebhooks.size();
    }
}
