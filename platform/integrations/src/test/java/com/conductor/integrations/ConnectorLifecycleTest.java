package com.conductor.integrations;

import com.conductor.integrations.connectors.ShopifyConnector;
import com.conductor.integrations.connectors.ZohoConnector;
import com.conductor.integrations.connectors.RazorpayConnector;
import com.conductor.integrations.framework.ConnectorRegistry;
import com.conductor.integrations.framework.ProxyHttpClient;
import com.conductor.shared.messaging.EventPublisher;
import com.conductor.shared.middleware.tenant.AuditLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConnectorLifecycleTest {

    private ConnectorRegistry registry;
    private ShopifyConnector shopifyConnector;
    private ZohoConnector zohoConnector;
    private RazorpayConnector razorpayConnector;
    private EventPublisher eventPublisher;
    private AuditLogger auditLogger;

    @BeforeEach
    public void setUp() {
        ProxyHttpClient client = mock(ProxyHttpClient.class);
        eventPublisher = mock(EventPublisher.class);
        auditLogger = mock(AuditLogger.class);

        shopifyConnector = new ShopifyConnector(client, eventPublisher, auditLogger);
        zohoConnector = new ZohoConnector(client, eventPublisher, auditLogger);
        razorpayConnector = new RazorpayConnector(client, eventPublisher, auditLogger);

        registry = new ConnectorRegistry(Arrays.asList(shopifyConnector, zohoConnector, razorpayConnector));
    }

    @Test
    public void testRegistryLookup() {
        assertTrue(registry.getAdapter("shopify").isPresent());
        assertTrue(registry.getAdapter("zoho").isPresent());
        assertTrue(registry.getAdapter("razorpay").isPresent());
        assertFalse(registry.getAdapter("invalid").isPresent());
    }

    @Test
    public void testConnectorExecution() {
        UUID tenantId = UUID.randomUUID();
        Map<String, Object> payload = new HashMap<>();

        Object shopifyRes = shopifyConnector.execute(tenantId, "sync-customers", payload);
        assertNotNull(shopifyRes);
        verify(eventPublisher).publish(eq("integration"), eq("shopify"), eq("customer_created"), eq("v1"), any());

        payload.put("name", "Lead A");
        Object zohoRes = zohoConnector.execute(tenantId, "create-lead", payload);
        assertNotNull(zohoRes);
        verify(eventPublisher).publish(eq("integration"), eq("zoho"), eq("lead_created"), eq("v1"), any());

        Object razorpayRes = razorpayConnector.execute(tenantId, "create-payment-link", payload);
        assertNotNull(razorpayRes);
        verify(eventPublisher).publish(eq("integration"), eq("razorpay"), eq("payment_created"), eq("v1"), any());
    }
}
