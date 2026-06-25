package com.conductor.shared.messaging;

import io.nats.client.Connection;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class NatsHealthIndicatorTest {

    @Test
    void whenConnectionIsNull_reportsDown() {
        NatsConnectionManager manager = mock(NatsConnectionManager.class);
        when(manager.getConnection()).thenReturn(null);

        NatsHealthIndicator indicator = new NatsHealthIndicator(manager);
        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    void whenConnectionIsConnected_reportsUp() {
        Connection conn = mock(Connection.class);
        when(conn.getStatus()).thenReturn(Connection.Status.CONNECTED);
        NatsConnectionManager manager = mock(NatsConnectionManager.class);
        when(manager.getConnection()).thenReturn(conn);

        NatsHealthIndicator indicator = new NatsHealthIndicator(manager);
        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
    }

    @Test
    void whenConnectionIsReconnecting_reportsDown() {
        Connection conn = mock(Connection.class);
        when(conn.getStatus()).thenReturn(Connection.Status.RECONNECTING);
        NatsConnectionManager manager = mock(NatsConnectionManager.class);
        when(manager.getConnection()).thenReturn(conn);

        NatsHealthIndicator indicator = new NatsHealthIndicator(manager);
        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    void whenConnectionIsClosed_reportsDown() {
        Connection conn = mock(Connection.class);
        when(conn.getStatus()).thenReturn(Connection.Status.CLOSED);
        NatsConnectionManager manager = mock(NatsConnectionManager.class);
        when(manager.getConnection()).thenReturn(conn);

        NatsHealthIndicator indicator = new NatsHealthIndicator(manager);
        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
    }
}
