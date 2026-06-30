package com.conductor.workflow.service;

import com.conductor.shared.messaging.EventConsumer;
import com.conductor.shared.middleware.tenant.TenantContext;
import com.conductor.shared.workflow.TriggerType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class WorkflowEventListener {

  private static final Logger log = LoggerFactory.getLogger(WorkflowEventListener.class);

  private final EventConsumer eventConsumer;
  private final TriggerService triggerService;
  private final com.conductor.workflow.repository.WorkflowDefinitionRepository definitionRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public WorkflowEventListener(
      EventConsumer eventConsumer,
      TriggerService triggerService,
      com.conductor.workflow.repository.WorkflowDefinitionRepository definitionRepository) {
    this.eventConsumer = eventConsumer;
    this.triggerService = triggerService;
    this.definitionRepository = definitionRepository;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void startSubscriptions() {
    log.info("Starting NATS subscriptions for workflow domain events...");

    eventConsumer.subscribe(
        "workflow-lead-created-group",
        "customer",
        "lead",
        "created",
        null,
        String.class,
        event -> {
          try {
            log.info("Received customer.lead.created event: {}", event.getEventId());
            Map<String, Object> payload =
                objectMapper.readValue(
                    event.getPayload(), new TypeReference<Map<String, Object>>() {});

            String tenantIdStr = event.getTenantId();
            if (tenantIdStr != null && !tenantIdStr.equals("system")) {
              UUID tenantId = UUID.fromString(tenantIdStr);
              TenantContext.setCurrentTenantId(tenantId);
              try {
                String firstName = (String) payload.getOrDefault("firstName", "");
                String lastName = (String) payload.getOrDefault("lastName", "");
                String name = (firstName + " " + lastName).trim();
                if (name.isEmpty()) {
                  name = "Customer";
                }

                Map<String, Object> triggerData =
                    Map.of(
                        "name", name,
                        "phone", payload.getOrDefault("phone", ""),
                        "email", payload.getOrDefault("email", ""),
                        "customerId", payload.getOrDefault("customerId", ""));

                ensureWelcomeWorkflowPublished(tenantId);
                log.info("Firing Welcome Workflow trigger for tenant: {}", tenantId);
                triggerService.fireTrigger(tenantId, TriggerType.EVENT, triggerData);
              } finally {
                TenantContext.clear();
              }
            }
          } catch (Exception e) {
            log.error("Failed to process customer.lead.created event", e);
          }
        });
  }

  private void ensureWelcomeWorkflowPublished(UUID tenantId) {
    java.util.List<com.conductor.workflow.domain.WorkflowDefinition> definitions =
        definitionRepository.findByTenantIdAndVersionStatusOrderByCreatedAtDesc(
            tenantId,
            com.conductor.shared.workflow.WorkflowVersionStatus.PUBLISHED,
            org.springframework.data.domain.PageRequest.of(0, 100));

    boolean exists =
        definitions.stream()
            .anyMatch(
                d ->
                    d.getTriggerType() == TriggerType.EVENT
                        && d.getName().equals("Welcome Workflow"));

    if (!exists) {
      log.info("Seeding Welcome Workflow definition for tenant: {}", tenantId);
      com.conductor.workflow.domain.WorkflowDefinition definition =
          com.conductor.workflow.domain.WorkflowDefinition.builder()
              .name("Welcome Workflow")
              .description("Automatically send a welcome message to new leads")
              .triggerType(TriggerType.EVENT)
              .triggerConfig("{}")
              .steps(
                  "["
                      + "  {"
                      + "    \"name\": \"send-welcome-whatsapp\","
                      + "    \"type\": \"WELCOME_MESSAGE\","
                      + "    \"config\": {"
                      + "      \"recipient\": \"{phone}\","
                      + "      \"message\": \"Welcome to Conductor, {name}!\""
                      + "    }"
                      + "  }"
                      + "]")
              .variables("{\"name\":\"\",\"phone\":\"\"}")
              .versionStatus(com.conductor.shared.workflow.WorkflowVersionStatus.PUBLISHED)
              .version(1)
              .createdBy("system")
              .build();
      definition.setTenantId(tenantId);
      definitionRepository.save(definition);
    }
  }
}
