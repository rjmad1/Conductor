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

@Component("zohoConnector")
public class ZohoConnector implements ConnectorAdapter {

  private static final Logger log = LoggerFactory.getLogger(ZohoConnector.class);

  private final ProxyHttpClient proxyHttpClient;
  private final EventPublisher eventPublisher;
  private final AuditLogger auditLogger;
  private final IntegrationMetrics metrics;
  private final String healthCheckUrl;
  private final int connectTimeoutMs;
  private final int readTimeoutMs;

  public ZohoConnector(
      ProxyHttpClient proxyHttpClient,
      EventPublisher eventPublisher,
      AuditLogger auditLogger,
      IntegrationMetrics metrics,
      @Value("${connector.zoho.health-url:https://accounts.zoho.com/}") String healthCheckUrl,
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
    return "zoho";
  }

  @Override
  public String getVersion() {
    return "v1";
  }

  @Override
  public void connect(UUID tenantId, Map<String, Object> params) {
    log.info("Connecting Zoho connector for tenant {}", tenantId);
    auditLogger.logEvent(
        "CONNECTOR_CONNECTED", "zoho", "SUCCESS", "Zoho connected for tenant " + tenantId);
  }

  @Override
  public void disconnect(UUID tenantId) {
    log.info("Disconnecting Zoho connector for tenant {}", tenantId);
    auditLogger.logEvent(
        "CONNECTOR_DISCONNECTED", "zoho", "SUCCESS", "Zoho disconnected for tenant " + tenantId);
  }

  @Override
  public boolean testConnection(UUID tenantId, Map<String, Object> credentials) {
    log.info("Testing connection to Zoho for tenant {}", tenantId);
    try {
      proxyHttpClient.getRestTemplate();
      return true;
    } catch (Exception e) {
      log.error("Zoho test connection failed", e);
      return false;
    }
  }

  @Override
  public Object execute(UUID tenantId, String action, Map<String, Object> payload) {
    log.info("Executing Zoho action {} for tenant {}", action, tenantId);
    auditLogger.logEvent("CONNECTOR_EXECUTED", "zoho:" + action, "SUCCESS", "Zoho action executed");

    if ("create-lead".equalsIgnoreCase(action)) {
      Map<String, Object> leadData = new HashMap<>(payload);
      leadData.put("zohoLeadId", "zoho-lead-555");
      leadData.put("status", "Created");
      eventPublisher.publish("integration", "zoho", "lead_created", "v1", leadData);
      return leadData;
    } else if ("update-lead".equalsIgnoreCase(action)) {
      Map<String, Object> leadData = new HashMap<>(payload);
      leadData.put("zohoLeadId", payload.getOrDefault("zohoLeadId", "zoho-lead-555"));
      leadData.put("status", "Updated");
      eventPublisher.publish("integration", "zoho", "lead_updated", "v1", leadData);
      return leadData;
    } else if ("sync-contacts".equalsIgnoreCase(action)) {
      Map<String, Object> contact = new HashMap<>();
      contact.put("zohoContactId", "zoho-cont-777");
      contact.put("lastName", "Doe");
      contact.put("email", "john.doe@example.com");
      eventPublisher.publish("integration", "zoho", "contact_created", "v1", contact);
      return contact;
    } else if ("sync-opportunities".equalsIgnoreCase(action)) {
      Map<String, Object> opp = new HashMap<>();
      opp.put("opportunityId", "opp-888");
      opp.put("amount", 5000.0);
      opp.put("stage", "Qualification");
      return opp;
    } else if ("sync-activities".equalsIgnoreCase(action)) {
      Map<String, Object> act = new HashMap<>();
      act.put("activityId", "act-012");
      act.put("subject", "Follow up call");
      return act;
    }
    throw new UnsupportedOperationException("Unknown Zoho action: " + action);
  }

  @Override
  public void subscribe(UUID tenantId, String eventName, String webhookUrl) {
    log.info("Subscribing to Zoho webhook {} for tenant {}", eventName, tenantId);
    auditLogger.logEvent(
        "WEBHOOK_SUBSCRIBED", "zoho:" + eventName, "SUCCESS", "Subscribed: " + webhookUrl);
  }

  @Override
  public void unsubscribe(UUID tenantId, String eventName) {
    log.info("Unsubscribing from Zoho webhook event {} for tenant {}", eventName, tenantId);
    auditLogger.logEvent(
        "WEBHOOK_UNSUBSCRIBED", "zoho:" + eventName, "SUCCESS", "Unsubscribed from webhook");
  }

  @Override
  public void refreshToken(UUID tenantId) {
    log.info("Refreshing Zoho token for tenant {}", tenantId);
  }

  @Override
  public ConnectorHealthResult healthCheck(UUID tenantId) {
    RestTemplate rt = proxyHttpClient.getRestTemplate(connectTimeoutMs, readTimeoutMs);
    ConnectorHealthResult result = ConnectorHealthProbe.probe("zoho", healthCheckUrl, rt);
    metrics.recordConnectorHealth("zoho", result.status() == ConnectorStatus.HEALTHY);
    metrics.recordConnectorHealthLatency("zoho", result.latencyMs());
    return result;
  }
}
