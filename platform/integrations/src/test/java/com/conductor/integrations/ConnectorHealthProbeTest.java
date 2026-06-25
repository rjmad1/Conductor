package com.conductor.integrations;

import com.conductor.integrations.connectors.ShopifyConnector;
import com.conductor.integrations.connectors.RazorpayConnector;
import com.conductor.integrations.connectors.ZohoConnector;
import com.conductor.integrations.framework.ConnectorHealthResult;
import com.conductor.integrations.framework.ConnectorStatus;
import com.conductor.integrations.framework.ProxyHttpClient;
import com.conductor.integrations.service.IntegrationMetrics;
import com.conductor.shared.messaging.EventPublisher;
import com.conductor.shared.middleware.tenant.AuditLogger;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ConnectorHealthProbeTest {

    private ProxyHttpClient proxyHttpClient;
    private RestTemplate restTemplate;
    private IntegrationMetrics metrics;
    private ShopifyConnector shopifyConnector;
    private RazorpayConnector razorpayConnector;
    private ZohoConnector zohoConnector;

    @BeforeEach
    void setUp() {
        proxyHttpClient = mock(ProxyHttpClient.class);
        restTemplate = mock(RestTemplate.class);
        metrics = new IntegrationMetrics(new SimpleMeterRegistry());
        EventPublisher publisher = mock(EventPublisher.class);
        AuditLogger logger = mock(AuditLogger.class);

        when(proxyHttpClient.getRestTemplate(anyInt(), anyInt())).thenReturn(restTemplate);

        shopifyConnector = new ShopifyConnector(proxyHttpClient, publisher, logger, metrics,
                "http://localhost/shopify-health", 500, 500);
        razorpayConnector = new RazorpayConnector(proxyHttpClient, publisher, logger, metrics,
                "http://localhost/razorpay-health", 500, 500);
        zohoConnector = new ZohoConnector(proxyHttpClient, publisher, logger, metrics,
                "http://localhost/zoho-health", 500, 500);
    }

    @Test
    void shopify_200Response_returnsHEALTHY() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(org.springframework.http.ResponseEntity.ok("{}"));

        ConnectorHealthResult result = shopifyConnector.healthCheck(UUID.randomUUID());

        assertThat(result.status()).isEqualTo(ConnectorStatus.HEALTHY);
        assertThat(result.latencyMs()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void razorpay_401AuthError_returnsHEALTHY_providerIsReachable() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized",
                        org.springframework.http.HttpHeaders.EMPTY, null, null));

        ConnectorHealthResult result = razorpayConnector.healthCheck(UUID.randomUUID());

        assertThat(result.status()).isEqualTo(ConnectorStatus.HEALTHY);
    }

    @Test
    void zoho_429RateLimit_returnsDEGRADED() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests",
                        org.springframework.http.HttpHeaders.EMPTY, null, null));

        ConnectorHealthResult result = zohoConnector.healthCheck(UUID.randomUUID());

        assertThat(result.status()).isEqualTo(ConnectorStatus.DEGRADED);
    }

    @Test
    void shopify_503ServerError_returnsUNAVAILABLE() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(HttpServerErrorException.create(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable",
                        org.springframework.http.HttpHeaders.EMPTY, null, null));

        ConnectorHealthResult result = shopifyConnector.healthCheck(UUID.randomUUID());

        assertThat(result.status()).isEqualTo(ConnectorStatus.UNAVAILABLE);
    }

    @Test
    void razorpay_connectionTimeout_retriesOnce_thenUNAVAILABLE() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection timed out"));

        ConnectorHealthResult result = razorpayConnector.healthCheck(UUID.randomUUID());

        assertThat(result.status()).isEqualTo(ConnectorStatus.UNAVAILABLE);
        assertThat(result.message()).contains("Unreachable after retry");
        // getForEntity called twice: initial attempt + 1 retry
        verify(restTemplate, times(2)).getForEntity(anyString(), eq(String.class));
    }

    @Test
    void zoho_transientFailureThenRecovery_returnsHEALTHY_afterRetry() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection reset"))
                .thenReturn(org.springframework.http.ResponseEntity.ok("OK"));

        ConnectorHealthResult result = zohoConnector.healthCheck(UUID.randomUUID());

        assertThat(result.status()).isEqualTo(ConnectorStatus.HEALTHY);
        assertThat(result.message()).contains("retry");
        verify(restTemplate, times(2)).getForEntity(anyString(), eq(String.class));
    }

    @Test
    void allConnectors_useTimed_restTemplate() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(org.springframework.http.ResponseEntity.ok("{}"));

        shopifyConnector.healthCheck(UUID.randomUUID());
        razorpayConnector.healthCheck(UUID.randomUUID());
        zohoConnector.healthCheck(UUID.randomUUID());

        // Verify timeout-aware RestTemplate is always used for health checks
        verify(proxyHttpClient, times(3)).getRestTemplate(500, 500);
        verify(proxyHttpClient, never()).getRestTemplate();
    }
}
