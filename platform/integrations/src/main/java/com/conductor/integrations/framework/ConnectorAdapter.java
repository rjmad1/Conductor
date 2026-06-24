package com.conductor.integrations.framework;

import java.util.Map;
import java.util.UUID;

public interface ConnectorAdapter {
    String getConnectorType(); // e.g., "shopify", "zoho", "razorpay"
    String getVersion();

    void connect(UUID tenantId, Map<String, Object> params);
    void disconnect(UUID tenantId);
    boolean testConnection(UUID tenantId, Map<String, Object> credentials);
    Object execute(UUID tenantId, String action, Map<String, Object> payload);
    void subscribe(UUID tenantId, String eventName, String webhookUrl);
    void unsubscribe(UUID tenantId, String eventName);
    void refreshToken(UUID tenantId);
    boolean healthCheck(UUID tenantId);
}
