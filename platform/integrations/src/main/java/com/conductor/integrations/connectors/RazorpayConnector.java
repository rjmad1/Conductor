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

@Component("razorpayConnector")
public class RazorpayConnector implements ConnectorAdapter {

  private static final Logger log = LoggerFactory.getLogger(RazorpayConnector.class);

  private final ProxyHttpClient proxyHttpClient;
  private final EventPublisher eventPublisher;
  private final AuditLogger auditLogger;
  private final IntegrationMetrics metrics;
  private final String healthCheckUrl;
  private final int connectTimeoutMs;
  private final int readTimeoutMs;

  public RazorpayConnector(
      ProxyHttpClient proxyHttpClient,
      EventPublisher eventPublisher,
      AuditLogger auditLogger,
      IntegrationMetrics metrics,
      @Value("${connector.razorpay.health-url:https://api.razorpay.com/}") String healthCheckUrl,
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
    return "razorpay";
  }

  @Override
  public String getVersion() {
    return "v1";
  }

  @Override
  public void connect(UUID tenantId, Map<String, Object> params) {
    log.info("Connecting Razorpay connector for tenant {}", tenantId);
    auditLogger.logEvent(
        "CONNECTOR_CONNECTED", "razorpay", "SUCCESS", "Razorpay connected for tenant " + tenantId);
  }

  @Override
  public void disconnect(UUID tenantId) {
    log.info("Disconnecting Razorpay connector for tenant {}", tenantId);
    auditLogger.logEvent(
        "CONNECTOR_DISCONNECTED",
        "razorpay",
        "SUCCESS",
        "Razorpay disconnected for tenant " + tenantId);
  }

  @Override
  public boolean testConnection(UUID tenantId, Map<String, Object> credentials) {
    log.info("Testing connection to Razorpay for tenant {}", tenantId);
    try {
      proxyHttpClient.getRestTemplate();
      return true;
    } catch (Exception e) {
      log.error("Razorpay test connection failed", e);
      return false;
    }
  }

  @Override
  public Object execute(UUID tenantId, String action, Map<String, Object> payload) {
    log.info("Executing Razorpay action {} for tenant {}", action, tenantId);
    auditLogger.logEvent(
        "CONNECTOR_EXECUTED", "razorpay:" + action, "SUCCESS", "Razorpay action executed");

    if ("create-payment-link".equalsIgnoreCase(action)) {
      Map<String, Object> paymentLink = new HashMap<>();
      paymentLink.put("paymentLinkId", "plink_xyz123");
      paymentLink.put("amount", payload.getOrDefault("amount", 1000));
      paymentLink.put("currency", payload.getOrDefault("currency", "INR"));
      paymentLink.put("shortUrl", "https://rzp.io/i/mockLink");
      paymentLink.put("status", "created");
      eventPublisher.publish("integration", "razorpay", "payment_created", "v1", paymentLink);
      return paymentLink;
    } else if ("lookup-payment-status".equalsIgnoreCase(action)) {
      Map<String, Object> payment = new HashMap<>();
      payment.put("paymentId", payload.getOrDefault("paymentId", "pay_xyz987"));
      payment.put("status", "captured");
      payment.put("amount", 1000);
      return payment;
    } else if ("track-refund".equalsIgnoreCase(action)) {
      Map<String, Object> refund = new HashMap<>();
      refund.put("refundId", "rfnd_abc111");
      refund.put("paymentId", payload.getOrDefault("paymentId", "pay_xyz987"));
      refund.put("status", "processed");
      return refund;
    } else if ("create-invoice".equalsIgnoreCase(action)) {
      Map<String, Object> invoice = new HashMap<>();
      invoice.put("invoiceId", "inv_abc555");
      invoice.put("status", "issued");
      return invoice;
    }
    throw new UnsupportedOperationException("Unknown Razorpay action: " + action);
  }

  @Override
  public void subscribe(UUID tenantId, String eventName, String webhookUrl) {
    log.info("Subscribing to Razorpay webhook {} for tenant {}", eventName, tenantId);
    auditLogger.logEvent(
        "WEBHOOK_SUBSCRIBED", "razorpay:" + eventName, "SUCCESS", "Subscribed: " + webhookUrl);
  }

  @Override
  public void unsubscribe(UUID tenantId, String eventName) {
    log.info("Unsubscribing from Razorpay webhook event {} for tenant {}", eventName, tenantId);
    auditLogger.logEvent(
        "WEBHOOK_UNSUBSCRIBED", "razorpay:" + eventName, "SUCCESS", "Unsubscribed from webhook");
  }

  @Override
  public void refreshToken(UUID tenantId) {
    log.info("Refreshing Razorpay token for tenant {}", tenantId);
  }

  @Override
  public ConnectorHealthResult healthCheck(UUID tenantId) {
    RestTemplate rt = proxyHttpClient.getRestTemplate(connectTimeoutMs, readTimeoutMs);
    ConnectorHealthResult result = ConnectorHealthProbe.probe("razorpay", healthCheckUrl, rt);
    metrics.recordConnectorHealth("razorpay", result.status() == ConnectorStatus.HEALTHY);
    metrics.recordConnectorHealthLatency("razorpay", result.latencyMs());
    return result;
  }
}
