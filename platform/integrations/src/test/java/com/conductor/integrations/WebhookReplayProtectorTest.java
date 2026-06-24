package com.conductor.integrations;

import com.conductor.integrations.webhooks.WebhookReplayProtector;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WebhookReplayProtectorTest {

    @Test
    public void testReplayProtection() {
        WebhookReplayProtector protector = new WebhookReplayProtector();
        String eventId = "evt_99999999";

        assertFalse(protector.isDuplicate(eventId));
        assertTrue(protector.isDuplicate(eventId));
    }
}
