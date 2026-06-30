package com.conductor.workflow.action;

import com.conductor.shared.execution.action.ActionContext;
import com.conductor.shared.execution.action.ActionHandler;
import com.conductor.shared.execution.action.ActionMetadata;
import com.conductor.shared.execution.action.ActionResult;
import com.conductor.shared.execution.provider.Provider;
import com.conductor.shared.execution.provider.ProviderRegistry;
import com.conductor.shared.execution.provider.ProviderRequest;
import com.conductor.shared.execution.provider.ProviderResponse;
import com.conductor.shared.messaging.EventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WelcomeMessageAction implements ActionHandler {

  private static final Logger log = LoggerFactory.getLogger(WelcomeMessageAction.class);

  private final ProviderRegistry providerRegistry;
  private final EventPublisher eventPublisher;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public WelcomeMessageAction(ProviderRegistry providerRegistry, EventPublisher eventPublisher) {
    this.providerRegistry = providerRegistry;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public String getActionType() {
    return "WELCOME_MESSAGE";
  }

  @Override
  @SuppressWarnings("unchecked")
  public ActionResult execute(ActionContext context) {
    Map<String, Object> config = context.getConfiguration();
    if (config == null) {
      return ActionResult.failure("CONFIGURATION_MISSING", "Configuration is missing");
    }

    String recipient = (String) config.get("recipient");
    if (recipient != null && recipient.contains("{phone}")) {
      recipient =
          recipient.replace("{phone}", (String) context.getVariables().getOrDefault("phone", ""));
    }

    String rawMessage = (String) config.get("message");

    if (recipient == null || recipient.trim().isEmpty()) {
      return ActionResult.failure("MISSING_RECIPIENT", "Recipient phone number is missing");
    }
    if (rawMessage == null || rawMessage.trim().isEmpty()) {
      return ActionResult.failure("MISSING_MESSAGE", "Message template is missing");
    }

    String formattedMessage = formatMessage(rawMessage, context.getVariables());

    Optional<Provider> providerOpt = providerRegistry.getProvider("WHATSAPP");
    if (providerOpt.isEmpty()) {
      return ActionResult.failure("PROVIDER_NOT_REGISTERED", "WhatsApp provider not registered");
    }
    Provider provider = providerOpt.get();

    try {
      provider.connect(null, config);

      Map<String, Object> bodyMap =
          Map.of(
              "messaging_product", "whatsapp",
              "recipient_type", "individual",
              "to", recipient,
              "type", "text",
              "text", Map.of("body", formattedMessage));

      ProviderRequest request =
          ProviderRequest.builder()
              .method("POST")
              .path("")
              .body(bodyMap)
              .correlationId(context.getCorrelationId())
              .build();

      log.info("Sending WhatsApp message to: {}", recipient);
      ProviderResponse response = provider.execute(request);

      provider.disconnect();

      if (response.isSuccess()) {
        String messageId = "msg_" + UUID.randomUUID().toString();
        try {
          Map<String, Object> responseBody =
              objectMapper.readValue(
                  response.getBody(),
                  new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
          if (responseBody != null) {
            java.util.List<Map<String, Object>> messages =
                (java.util.List<Map<String, Object>>) responseBody.get("messages");
            if (messages != null && !messages.isEmpty()) {
              messageId = (String) messages.get(0).get("id");
            }
          }
        } catch (Exception e) {
          log.warn("Failed to parse message ID from provider response body", e);
        }

        String customerId = null;
        if (context.getVariables().containsKey("customerId")) {
          customerId = context.getVariables().get("customerId").toString();
        } else if (context.getVariables().containsKey("id")) {
          customerId = context.getVariables().get("id").toString();
        }

        Map<String, Object> sentPayload =
            Map.of(
                "customerId",
                customerId != null ? customerId : "",
                "messageId",
                messageId,
                "recipient",
                recipient,
                "status",
                "SENT",
                "channel",
                "WHATSAPP",
                "content",
                formattedMessage);
        eventPublisher.publish(
            "messaging", "message", "sent", "v1", objectMapper.writeValueAsString(sentPayload));
        log.info("Successfully sent WhatsApp welcome message, published sent event");

        return ActionResult.success(
            Map.of(
                "messageId", messageId,
                "status", "SENT",
                "recipient", recipient));
      } else {
        return ActionResult.builder()
            .success(false)
            .errorCode("WHATSAPP_SEND_FAILED")
            .errorMessage("WhatsApp provider returned status: " + response.getStatusCode())
            .build();
      }

    } catch (Exception e) {
      log.error("Failed to execute WhatsApp Welcome message action", e);
      return ActionResult.failure("EXECUTION_ERROR", e.getMessage());
    }
  }

  @Override
  public ActionMetadata getMetadata() {
    return ActionMetadata.builder()
        .actionType(getActionType())
        .name("Send Welcome WhatsApp Message")
        .description("Sends a welcome message via WhatsApp provider using custom variables.")
        .requiredConfigurationKeys(List.of("recipient", "message"))
        .supportedParameters(
            Map.of(
                "recipient", "Recipient's phone number",
                "message", "Welcome message content template"))
        .build();
  }

  private String formatMessage(String message, Map<String, Object> variables) {
    if (message == null || variables == null || variables.isEmpty()) {
      return message;
    }
    String result = message;
    String name = "";
    if (variables.containsKey("displayName") && variables.get("displayName") != null) {
      name = variables.get("displayName").toString();
    } else if (variables.containsKey("name") && variables.get("name") != null) {
      name = variables.get("name").toString();
    } else if (variables.containsKey("firstName") && variables.get("firstName") != null) {
      name = variables.get("firstName").toString();
      if (variables.containsKey("lastName") && variables.get("lastName") != null) {
        name += " " + variables.get("lastName").toString();
      }
    }
    if (name.isEmpty()) {
      name = "Customer";
    }

    result = result.replace("{name}", name);
    for (Map.Entry<String, Object> entry : variables.entrySet()) {
      String placeholder = "{" + entry.getKey() + "}";
      if (result.contains(placeholder) && entry.getValue() != null) {
        result = result.replace(placeholder, entry.getValue().toString());
      }
    }
    return result;
  }
}
