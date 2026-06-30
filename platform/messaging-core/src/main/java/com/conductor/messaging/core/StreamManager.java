package com.conductor.messaging.core;

import com.conductor.shared.messaging.NatsConnectionManager;
import io.nats.client.Connection;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Service
public class StreamManager implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(StreamManager.class);
  private final NatsConnectionManager connectionManager;

  public StreamManager(NatsConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
  }

  @Override
  public void afterPropertiesSet() {
    initializeStreams();
  }

  public void initializeStreams() {
    Connection conn = connectionManager.getConnection();
    if (conn == null) {
      log.error("Cannot initialize NATS streams: connection is offline");
      return;
    }

    try {
      JetStreamManagement jsm = conn.jetStreamManagement();

      // Define stream configurations
      List<StreamDefinition> streamDefinitions =
          Arrays.asList(
              new StreamDefinition(
                  "TENANT_STREAM", Arrays.asList("conductor.*.tenant.>"), Duration.ofDays(30)),
              new StreamDefinition(
                  "CUSTOMER_STREAM", Arrays.asList("conductor.*.customer.>"), Duration.ofDays(30)),
              new StreamDefinition(
                  "MESSAGING_STREAM",
                  Arrays.asList("conductor.*.messaging.>", "conductor.*.message.>"),
                  Duration.ofDays(7)),
              new StreamDefinition(
                  "WORKFLOW_STREAM", Arrays.asList("conductor.*.workflow.>"), Duration.ofDays(14)),
              new StreamDefinition(
                  "INTEGRATION_STREAM",
                  Arrays.asList("conductor.*.integration.>"),
                  Duration.ofDays(7)),
              new StreamDefinition(
                  "ANALYTICS_STREAM", Arrays.asList("conductor.*.analytics.>"), Duration.ofDays(7)),
              new StreamDefinition(
                  "AUDIT_STREAM", Arrays.asList("conductor.*.audit.>"), Duration.ofDays(90)),
              new StreamDefinition(
                  "AI_STREAM", Arrays.asList("conductor.*.ai.>"), Duration.ofDays(7)),
              new StreamDefinition("DLQ_STREAM", Arrays.asList("dlq.>"), Duration.ofDays(30)));

      for (StreamDefinition def : streamDefinitions) {
        createOrUpdateStream(jsm, def);
      }
    } catch (Exception e) {
      log.error("Failed to initialize NATS streams", e);
    }
  }

  private void createOrUpdateStream(JetStreamManagement jsm, StreamDefinition def) {
    try {
      StreamConfiguration config =
          StreamConfiguration.builder()
              .name(def.name)
              .subjects(def.subjects)
              .storageType(StorageType.File)
              .retentionPolicy(RetentionPolicy.Limits)
              .maxAge(def.maxAge)
              .replicas(1) // Local environment default, use 3 in production
              .build();

      boolean exists = false;
      try {
        StreamInfo info = jsm.getStreamInfo(def.name);
        exists = (info != null);
      } catch (Exception e) {
        // Ignore: stream doesn't exist
      }

      if (exists) {
        log.info("Updating existing NATS stream: {}", def.name);
        jsm.updateStream(config);
      } else {
        log.info("Creating new NATS stream: {} with subjects: {}", def.name, def.subjects);
        jsm.addStream(config);
      }
    } catch (Exception e) {
      log.error("Error creating/updating NATS stream: {}", def.name, e);
    }
  }

  private static class StreamDefinition {
    final String name;
    final List<String> subjects;
    final Duration maxAge;

    StreamDefinition(String name, List<String> subjects, Duration maxAge) {
      this.name = name;
      this.subjects = subjects;
      this.maxAge = maxAge;
    }
  }
}
