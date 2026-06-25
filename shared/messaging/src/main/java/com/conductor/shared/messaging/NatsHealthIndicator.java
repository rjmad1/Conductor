package com.conductor.shared.messaging;

import io.nats.client.Connection;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Exposes NATS connection status to Spring Boot Actuator health endpoint.
 * Reports DOWN when the connection is null (startup failure) or not CONNECTED.
 * Callers should check /actuator/health for readiness before sending traffic.
 */
@Component
public class NatsHealthIndicator implements HealthIndicator {

    private final NatsConnectionManager connectionManager;

    public NatsHealthIndicator(NatsConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Health health() {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            return Health.down()
                    .withDetail("reason", "NATS connection was never established (startup failure)")
                    .build();
        }
        Connection.Status status = conn.getStatus();
        if (status == Connection.Status.CONNECTED) {
            return Health.up()
                    .withDetail("status", status.name())
                    .build();
        }
        return Health.down()
                .withDetail("status", status.name())
                .build();
    }
}
