package com.conductor.integrations;

import com.conductor.integrations.framework.ConnectorAdapter;
import com.conductor.integrations.framework.ConnectorHealthResult;
import com.conductor.integrations.framework.ConnectorStatus;
import com.conductor.integrations.service.ConnectorHealthIndicator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConnectorHealthIndicatorTest {

    private ConnectorAdapter mockAdapter(String type, ConnectorHealthResult result) {
        ConnectorAdapter adapter = mock(ConnectorAdapter.class);
        when(adapter.getConnectorType()).thenReturn(type);
        when(adapter.healthCheck(any())).thenReturn(result);
        return adapter;
    }

    @Test
    void allHealthy_aggregateIsUP() {
        ConnectorHealthIndicator indicator = new ConnectorHealthIndicator(List.of(
                mockAdapter("shopify", ConnectorHealthResult.healthy(10)),
                mockAdapter("razorpay", ConnectorHealthResult.healthy(15)),
                mockAdapter("zoho", ConnectorHealthResult.healthy(12))
        ));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsKey("shopify");
        assertThat(health.getDetails()).containsKey("razorpay");
        assertThat(health.getDetails()).containsKey("zoho");
    }

    @Test
    void oneUnavailable_aggregateIsDOWN() {
        ConnectorHealthIndicator indicator = new ConnectorHealthIndicator(List.of(
                mockAdapter("shopify", ConnectorHealthResult.healthy(10)),
                mockAdapter("razorpay", ConnectorHealthResult.unavailable("Unreachable", 3010)),
                mockAdapter("zoho", ConnectorHealthResult.healthy(12))
        ));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void oneDegraded_aggregateIsOUT_OF_SERVICE() {
        ConnectorHealthIndicator indicator = new ConnectorHealthIndicator(List.of(
                mockAdapter("shopify", ConnectorHealthResult.healthy(10)),
                mockAdapter("zoho", ConnectorHealthResult.degraded("Rate limited", 200))
        ));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
    }

    @Test
    void unavailableTakesPrecedenceOverDegraded() {
        ConnectorHealthIndicator indicator = new ConnectorHealthIndicator(List.of(
                mockAdapter("shopify", ConnectorHealthResult.degraded("Rate limited", 200)),
                mockAdapter("razorpay", ConnectorHealthResult.unavailable("Unreachable", 3000)),
                mockAdapter("zoho", ConnectorHealthResult.healthy(12))
        ));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void healthDetailsContainStatusAndLatency() {
        ConnectorHealthIndicator indicator = new ConnectorHealthIndicator(List.of(
                mockAdapter("shopify", new ConnectorHealthResult(ConnectorStatus.HEALTHY, "OK", 42L))
        ));

        Health health = indicator.health();

        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> shopifyDetail =
                (java.util.Map<String, Object>) health.getDetails().get("shopify");
        assertThat(shopifyDetail.get("status")).isEqualTo("HEALTHY");
        assertThat(shopifyDetail.get("latencyMs")).isEqualTo(42L);
    }
}
