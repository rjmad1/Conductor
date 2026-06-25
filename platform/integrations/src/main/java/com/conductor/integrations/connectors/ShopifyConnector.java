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

@Component("shopifyConnector")
public class ShopifyConnector implements ConnectorAdapter {

  private static final Logger log = LoggerFactory.getLogger(ShopifyConnector.class);

  private final ProxyHttpClient proxyHttpClient;
  private final EventPublisher eventPublisher;
  private final AuditLogger auditLogger;
  private final IntegrationMetrics metrics;
  private final String healthCheckUrl;
  private final int connectTimeoutMs;
  private final int readTimeoutMs;

  public ShopifyConnector(
      ProxyHttpClient proxyHttpClient,
      EventPublisher eventPublisher,
      AuditLogger auditLogger,
      IntegrationMetrics metrics,
      @Value("${connector.shopify.health-url:https://status.shopify.com/api/v2/status.json}")
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
    return "shopify";
  }

  @Override
  public String getVersion() {
    return "v1";
  }

  @Override
  public void connect(UUID tenantId, Map<String, Object> params) {
    log.info("Connecting Shopify connector for tenant {}", tenantId);
    auditLogger.logEvent(
        "CONNECTOR_CONNECTED", "shopify", "SUCCESS", "Shopify connected for tenant " + tenantId);
  }

  @Override
  public void disconnect(UUID tenantId) {
    log.info("Disconnecting Shopify connector for tenant {}", tenantId);
    auditLogger.logEvent(
        "CONNECTOR_DISCONNECTED",
        "shopify",
        "SUCCESS",
        "Shopify disconnected for tenant " + tenantId);
  }

  @Override
  public boolean testConnection(UUID tenantId, Map<String, Object> credentials) {
    log.info("Testing connection to Shopify for tenant {}", tenantId);
    try {
      proxyHttpClient.getRestTemplate();
      return true;
    } catch (Exception e) {
      log.error("Shopify test connection failed", e);
      return false;
    }
  }

  @Override
  public Object execute(UUID tenantId, String action, Map<String, Object> payload) {
    log.info("Executing Shopify action {} for tenant {}", action, tenantId);
    auditLogger.logEvent(
        "CONNECTOR_EXECUTED", "shopify:" + action, "SUCCESS", "Shopify action executed");

    if ("sync-customers".equalsIgnoreCase(action)) {
      Map<String, Object> customerData = new HashMap<>();
      customerData.put("customerId", "shopify-cust-123");
      customerData.put("email", "john.doe@example.com");
      customerData.put("firstName", "John");
      customerData.put("lastName", "Doe");
      customerData.put("syncedAt", System.currentTimeMillis());
      eventPublisher.publish("integration", "shopify", "customer_created", "v1", customerData);
      return customerData;
    } else if ("sync-orders".equalsIgnoreCase(action)) {
      Map<String, Object> orderData = new HashMap<>();
      orderData.put("orderId", "shopify-ord-999");
      orderData.put("totalPrice", 299.99);
      orderData.put("currency", "USD");
      orderData.put("status", "paid");
      eventPublisher.publish("integration", "shopify", "order_created", "v1", orderData);
      return orderData;
    } else if ("lookup-product".equalsIgnoreCase(action)) {
      Map<String, Object> product = new HashMap<>();
      product.put("productId", payload.getOrDefault("productId", "prod-001"));
      product.put("title", "Shopify Mock Product");
      product.put("price", 49.99);
      return product;
    } else if ("lookup-inventory".equalsIgnoreCase(action)) {
      Map<String, Object> inventory = new HashMap<>();
      inventory.put("productId", payload.getOrDefault("productId", "prod-001"));
      inventory.put("stockLevel", 150);
      return inventory;
    }
    throw new UnsupportedOperationException("Unknown Shopify action: " + action);
  }

  @Override
  public void subscribe(UUID tenantId, String eventName, String webhookUrl) {
    log.info("Subscribing to Shopify webhook {} for tenant {}", eventName, tenantId);
    auditLogger.logEvent(
        "WEBHOOK_SUBSCRIBED", "shopify:" + eventName, "SUCCESS", "Subscribed: " + webhookUrl);
  }

  @Override
  public void unsubscribe(UUID tenantId, String eventName) {
    log.info("Unsubscribing from Shopify webhook event {} for tenant {}", eventName, tenantId);
    auditLogger.logEvent(
        "WEBHOOK_UNSUBSCRIBED", "shopify:" + eventName, "SUCCESS", "Unsubscribed from webhook");
  }

  @Override
  public void refreshToken(UUID tenantId) {
    log.info("Refreshing Shopify token for tenant {}", tenantId);
  }

  @Override
  public ConnectorHealthResult healthCheck(UUID tenantId) {
    RestTemplate rt = proxyHttpClient.getRestTemplate(connectTimeoutMs, readTimeoutMs);
    ConnectorHealthResult result = ConnectorHealthProbe.probe("shopify", healthCheckUrl, rt);
    metrics.recordConnectorHealth("shopify", result.status() == ConnectorStatus.HEALTHY);
    metrics.recordConnectorHealthLatency("shopify", result.latencyMs());
    return result;
  }
}
