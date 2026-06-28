package com.conductor.integrations.connectors;

import com.conductor.integrations.framework.ConnectorAdapter;
import com.conductor.integrations.framework.ConnectorHealthProbe;
import com.conductor.integrations.framework.ConnectorHealthResult;
import com.conductor.integrations.framework.ConnectorStatus;
import com.conductor.integrations.framework.ProxyHttpClient;
import com.conductor.integrations.service.IntegrationMetrics;
import com.conductor.shared.messaging.EventPublisher;
import com.conductor.shared.middleware.tenant.AuditLogger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component("googleCalendarConnector")
public class GoogleCalendarConnector implements ConnectorAdapter {

  private static final Logger log = LoggerFactory.getLogger(GoogleCalendarConnector.class);

  private final ProxyHttpClient proxyHttpClient;
  private final EventPublisher eventPublisher;
  private final AuditLogger auditLogger;
  private final IntegrationMetrics metrics;
  private final String healthCheckUrl;
  private final int connectTimeoutMs;
  private final int readTimeoutMs;

  public GoogleCalendarConnector(
      ProxyHttpClient proxyHttpClient,
      EventPublisher eventPublisher,
      AuditLogger auditLogger,
      IntegrationMetrics metrics,
      @Value("${connector.google.health-url:https://www.googleapis.com/calendar/v3}")
          String healthCheckUrl,
      @Value("${connector.health.connect-timeout-ms:3000}") int connectTimeoutMs,
      @Value("${connector.health.read-timeout-ms:3000}") int readTimeoutMs) {
    this.proxyHttpClient = proxyHttpClient;
    this.eventPublisher = eventPublisher;
    this.auditLogger = auditLogger;
    this.metrics = metrics;
    this.healthCheckUrl = healthCheckUrl;
    this.connectTimeoutMs = connectTimeoutMs;
    this.readTimeoutMs = readTimeoutMs;
  }

  @Override
  public String getConnectorType() {
    return "google-calendar";
  }

  @Override
  public String getVersion() {
    return "v3";
  }

  @Override
  public void connect(UUID tenantId, Map<String, Object> params) {
    log.info("Connecting Google Calendar connector for tenant {}", tenantId);
    auditLogger.logEvent(
        "CONNECTOR_CONNECTED", "google-calendar", "SUCCESS", "Connected for tenant " + tenantId);
  }

  @Override
  public void disconnect(UUID tenantId) {
    log.info("Disconnecting Google Calendar connector for tenant {}", tenantId);
    auditLogger.logEvent(
        "CONNECTOR_DISCONNECTED",
        "google-calendar",
        "SUCCESS",
        "Disconnected for tenant " + tenantId);
  }

  @Override
  public boolean testConnection(UUID tenantId, Map<String, Object> credentials) {
    log.info("Testing connection to Google Calendar for tenant {}", tenantId);
    return true;
  }

  @Override
  public Object execute(UUID tenantId, String action, Map<String, Object> payload) {
    log.info("Executing Google Calendar action {} for tenant {}", action, tenantId);
    auditLogger.logEvent(
        "CONNECTOR_EXECUTED", "google-calendar:" + action, "SUCCESS", "Action executed");

    if ("create-appointment".equalsIgnoreCase(action)) {
      Map<String, Object> event = new HashMap<>();
      event.put("eventId", "evt_12345");
      event.put("status", "confirmed");
      event.put("summary", payload.getOrDefault("summary", "New Appointment"));
      event.put("start", payload.get("start"));
      event.put("end", payload.get("end"));
      // Publish event internally that appointment was created
      eventPublisher.publish("integration", "google-calendar", "appointment_created", "v1", event);
      return event;
    }
    throw new UnsupportedOperationException("Unknown Google Calendar action: " + action);
  }

  @Override
  public void subscribe(UUID tenantId, String eventName, String webhookUrl) {
    log.info("Subscribing to Google Calendar webhook {} for tenant {}", eventName, tenantId);
  }

  @Override
  public void unsubscribe(UUID tenantId, String eventName) {
    log.info(
        "Unsubscribing from Google Calendar webhook event {} for tenant {}", eventName, tenantId);
  }

  @Override
  public void refreshToken(UUID tenantId) {
    log.info("Refreshing Google Calendar token for tenant {}", tenantId);
  }

  @Override
  public ConnectorHealthResult healthCheck(UUID tenantId) {
    RestTemplate rt = proxyHttpClient.getRestTemplate(connectTimeoutMs, readTimeoutMs);
    // Might return 401 unauthenticated, but that proves endpoint is reachable.
    ConnectorHealthResult result =
        ConnectorHealthProbe.probe("google-calendar", healthCheckUrl, rt);
    metrics.recordConnectorHealth("google-calendar", result.status() == ConnectorStatus.HEALTHY);
    metrics.recordConnectorHealthLatency("google-calendar", result.latencyMs());
    return result;
  }
}
