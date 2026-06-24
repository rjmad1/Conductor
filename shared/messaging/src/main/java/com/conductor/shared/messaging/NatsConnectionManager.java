package com.conductor.shared.messaging;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

@Component
public class NatsConnectionManager implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(NatsConnectionManager.class);
    private Connection connection;

    public NatsConnectionManager(@Value("${nats.server-url:nats://localhost:4222}") String natsUrl) {
        try {
            Options options = new Options.Builder()
                    .server(natsUrl)
                    .maxReconnects(-1) // reconnect infinitely
                    .connectionListener((conn, type) -> log.info("NATS Connection Status change: {}", type))
                    .build();
            this.connection = Nats.connect(options);
            log.info("Successfully connected to NATS server at {}", natsUrl);
        } catch (Exception e) {
            log.error("Failed to connect to NATS server at {}", natsUrl, e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    @PreDestroy
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                log.info("NATS Connection closed successfully");
            } catch (Exception e) {
                log.error("Error closing NATS Connection", e);
            }
        }
    }
}
