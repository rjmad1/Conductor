package com.conductor.workflow.action;

import com.conductor.shared.execution.action.ActionContext;
import com.conductor.shared.execution.action.ActionHandler;
import com.conductor.shared.execution.action.ActionMetadata;
import com.conductor.shared.execution.action.ActionResult;
import com.conductor.shared.messaging.EventPublisher;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Built-in action to publish business domain events internally via EventPublisher. */
@Component
public class PublishInternalDomainEventActionHandler implements ActionHandler {

  private final EventPublisher eventPublisher;

  public PublishInternalDomainEventActionHandler(EventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @Override
  public String getActionType() {
    return "PUBLISH_INTERNAL_DOMAIN_EVENT";
  }

  @Override
  public ActionResult execute(ActionContext context) {
    Map<String, Object> config = context.getConfiguration();
    String domain = (String) config.get("domain");
    String entity = (String) config.get("entity");
    String action = (String) config.get("action");
    Object payload = config.getOrDefault("payload", Map.of());

    eventPublisher.publish(domain, entity, action, "1.0.0", payload);

    return ActionResult.success(
        Map.of(
            "domain", domain,
            "entity", entity,
            "action", action,
            "status", "event_published"));
  }

  @Override
  public ActionMetadata getMetadata() {
    return ActionMetadata.builder()
        .actionType(getActionType())
        .name("Publish Internal Domain Event")
        .description("Publishes an event to the internal messaging system for loose coupling.")
        .requiredConfigurationKeys(List.of("domain", "entity", "action"))
        .supportedParameters(
            Map.of(
                "domain", "Event domain category",
                "entity", "Entity subject name",
                "action", "Action operation name",
                "payload", "Additional JSON event payload data"))
        .build();
  }
}
