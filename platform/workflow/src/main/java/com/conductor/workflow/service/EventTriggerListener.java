package com.conductor.workflow.service;

import com.conductor.shared.messaging.NatsConnectionManager;
import com.conductor.shared.workflow.TriggerType;
import com.conductor.shared.workflow.WorkflowVersionStatus;
import com.conductor.workflow.domain.WorkflowDefinition;
import com.conductor.workflow.repository.WorkflowDefinitionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventTriggerListener {

  private static final Logger log = LoggerFactory.getLogger(EventTriggerListener.class);
  private final NatsConnectionManager natsManager;
  private final WorkflowDefinitionRepository definitionRepository;
  private final TriggerService triggerService;
  private final ObjectMapper objectMapper;

  public EventTriggerListener(
      NatsConnectionManager natsManager,
      WorkflowDefinitionRepository definitionRepository,
      TriggerService triggerService) {
    this.natsManager = natsManager;
    this.definitionRepository = definitionRepository;
    this.triggerService = triggerService;
    this.objectMapper = new ObjectMapper();
  }

  @PostConstruct
  public void init() {
    Connection connection = natsManager.getConnection();
    if (connection != null) {
      Dispatcher dispatcher = connection.createDispatcher(this::onMessage);
      dispatcher.subscribe("conductor.>");
      log.info("Subscribed to NATS subject 'conductor.>' for event triggers.");
    }
  }

  private void onMessage(Message msg) {
    String subject = msg.getSubject();
    String data = new String(msg.getData(), StandardCharsets.UTF_8);
    log.debug("Received NATS event on subject {}: {}", subject, data);

    try {
      Map<String, Object> payload =
          objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {});

      String[] parts = subject.split("\\.");
      UUID tenantId = null;
      if (parts.length >= 2 && !parts[1].equals("global")) {
        try {
          tenantId = UUID.fromString(parts[1]);
        } catch (Exception e) {
          // ignore parsing error for tenant ID
        }
      }

      if (tenantId != null) {
        List<WorkflowDefinition> definitions =
            definitionRepository.findByTriggerTypeAndVersionStatus(
                TriggerType.EVENT, WorkflowVersionStatus.PUBLISHED);

        for (WorkflowDefinition def : definitions) {
          if (def.getTenantId().equals(tenantId)) {
            // Check if trigger config matches the subject routing key
            if (def.getTriggerConfig() != null && def.getTriggerConfig().contains(subject)) {
              triggerService.fireTrigger(tenantId, TriggerType.EVENT, payload);
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("Failed to process event trigger", e);
    }
  }
}
