package com.conductor.integrations.webhooks;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;

@Component
public class WebhookReplayProtector {

    private final Map<String, Instant> processedWebhooks = new ConcurrentHashMap<>();

    public boolean isDuplicate(String eventId) {
        if (eventId == null) {
            return false;
        }
        if (processedWebhooks.containsKey(eventId)) {
            return true;
        }
        processedWebhooks.put(eventId, Instant.now());
        if (processedWebhooks.size() > 10000) {
            processedWebhooks.clear();
        }
        return false;
    }
}
