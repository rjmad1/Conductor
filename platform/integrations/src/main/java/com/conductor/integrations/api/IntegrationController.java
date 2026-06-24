package com.conductor.integrations.api;

import com.conductor.integrations.domain.*;
import com.conductor.integrations.repository.*;
import com.conductor.integrations.framework.ConnectorRegistry;
import com.conductor.integrations.framework.ConnectorAdapter;
import com.conductor.integrations.service.CredentialService;
import com.conductor.shared.middleware.tenant.TenantContext;
import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.messaging.EventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/v1/integrations")
public class IntegrationController {

    private final ConnectorRepository connectorRepository;
    private final IntegrationRepository integrationRepository;
    private final ConnectionRepository connectionRepository;
    private final WebhookSubscriptionRepository webhookSubscriptionRepository;
    private final ExecutionHistoryRepository executionHistoryRepository;
    private final CredentialService credentialService;
    private final ConnectorRegistry connectorRegistry;
    private final AuditLogger auditLogger;
    private final EventPublisher eventPublisher;

    public IntegrationController(
            ConnectorRepository connectorRepository,
            IntegrationRepository integrationRepository,
            ConnectionRepository connectionRepository,
            WebhookSubscriptionRepository webhookSubscriptionRepository,
            ExecutionHistoryRepository executionHistoryRepository,
            CredentialService credentialService,
            ConnectorRegistry connectorRegistry,
            AuditLogger auditLogger,
            EventPublisher eventPublisher) {
        this.connectorRepository = connectorRepository;
        this.integrationRepository = integrationRepository;
        this.connectionRepository = connectionRepository;
        this.webhookSubscriptionRepository = webhookSubscriptionRepository;
        this.executionHistoryRepository = executionHistoryRepository;
        this.credentialService = credentialService;
        this.connectorRegistry = connectorRegistry;
        this.auditLogger = auditLogger;
        this.eventPublisher = eventPublisher;
    }

    @GetMapping("/connectors")
    public ResponseEntity<List<Connector>> listConnectors() {
        return ResponseEntity.ok(connectorRepository.findAll());
    }

    @PostMapping("/credentials")
    public ResponseEntity<Credential> saveCredential(@RequestParam UUID integrationId, @RequestParam String authType, @RequestBody String rawPayload) {
        Credential cred = credentialService.saveCredential(integrationId, authType, rawPayload);
        eventPublisher.publish("integration", "credential", "updated", "v1", Map.of("integrationId", integrationId, "authType", authType));
        return ResponseEntity.status(HttpStatus.CREATED).body(cred);
    }

    @PostMapping("/oauth/authorize")
    public ResponseEntity<Map<String, String>> authorizeOAuth(@RequestParam UUID integrationId, @RequestParam String authUrl) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        Integration integration = integrationRepository.findByIdAndTenantId(integrationId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));

        auditLogger.logEvent("OAUTH_AUTH_STARTED", "integration:" + integrationId, "SUCCESS", "Redirecting to auth url");
        return ResponseEntity.ok(Map.of("authorizationUrl", authUrl + "?state=" + integrationId));
    }

    @GetMapping("/oauth/callback")
    public ResponseEntity<String> oauthCallback(@RequestParam String code, @RequestParam("state") UUID integrationId) {
        Optional<Integration> integrationOpt = integrationRepository.findById(integrationId);
        if (integrationOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Integration not found");
        }
        Integration integration = integrationOpt.get();
        TenantContext.setCurrentTenantId(integration.getTenantId());

        try {
            credentialService.saveOAuthConnection(integrationId, "mock-access-token-" + UUID.randomUUID(), "mock-refresh-token", Instant.now().plusSeconds(3600), "all");

            Optional<Connection> existingConn = connectionRepository.findByIntegrationId(integrationId);
            Connection conn = existingConn.orElse(new Connection());
            conn.setIntegration(integration);
            conn.setStatus("CONNECTED");
            conn.setLastConnectedAt(Instant.now());
            connectionRepository.save(conn);

            auditLogger.logEvent("OAUTH_CALLBACK_SUCCESS", "integration:" + integrationId, "SUCCESS", "OAuth authorize succeeded");
            eventPublisher.publish("integration", "integration", "connected", "v1", Map.of("integrationId", integrationId));

            return ResponseEntity.ok("Successfully authorized " + integration.getName());
        } finally {
            TenantContext.clear();
        }
    }

    @PostMapping("/webhooks")
    public ResponseEntity<WebhookSubscription> subscribeWebhook(@RequestParam UUID integrationId, @RequestParam String eventName, @RequestParam String targetUrl) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        Integration integration = integrationRepository.findByIdAndTenantId(integrationId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));

        WebhookSubscription sub = new WebhookSubscription();
        sub.setIntegration(integration);
        sub.setEventName(eventName);
        sub.setTargetUrl(targetUrl);
        sub.setSecret("secret-" + UUID.randomUUID());
        sub.setStatus("ACTIVE");

        WebhookSubscription saved = webhookSubscriptionRepository.save(sub);
        auditLogger.logEvent("WEBHOOK_REGISTERED", "integration:" + integrationId, "SUCCESS", "Registered webhook subscription: " + eventName);

        connectorRegistry.getAdapter(integration.getConnector().getType())
                .ifPresent(adapter -> adapter.subscribe(tenantId, eventName, targetUrl));

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<Object> executeAction(@PathVariable UUID id, @RequestParam String action, @RequestBody Map<String, Object> payload) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        Optional<Integration> integrationOpt = integrationRepository.findByIdAndTenantId(id, tenantId);
        if (integrationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Integration integration = integrationOpt.get();

        ConnectorAdapter adapter = connectorRegistry.getAdapter(integration.getConnector().getType())
                .orElseThrow(() -> new IllegalArgumentException("Connector adapter not found"));

        long startTime = System.currentTimeMillis();
        boolean success = false;
        Object response = null;
        String errorMessage = null;

        try {
            response = adapter.execute(tenantId, action, payload);
            success = true;
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            ExecutionHistory history = new ExecutionHistory();
            history.setIntegration(integration);
            history.setAction(action);
            history.setStatus(success ? "SUCCESS" : "FAILURE");
            history.setRequestPayload(payload.toString());
            history.setResponsePayload(response != null ? response.toString() : null);
            history.setErrorMessage(errorMessage);
            history.setDurationMs(duration);
            executionHistoryRepository.save(history);

            eventPublisher.publish("integration", "integration", success ? "executed" : "failed", "v1", 
                    Map.of("integrationId", id, "action", action, "durationMs", duration));
        }
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<ExecutionHistory>> getHistory(@PathVariable UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return ResponseEntity.ok(executionHistoryRepository.findByIntegrationIdAndTenantId(id, tenantId));
    }

    @GetMapping("/{id}/health")
    public ResponseEntity<Map<String, Object>> checkHealth(@PathVariable UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        Optional<Integration> integrationOpt = integrationRepository.findByIdAndTenantId(id, tenantId);
        if (integrationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Integration integration = integrationOpt.get();

        ConnectorAdapter adapter = connectorRegistry.getAdapter(integration.getConnector().getType())
                .orElseThrow(() -> new IllegalArgumentException("Connector adapter not found"));

        boolean isHealthy = adapter.healthCheck(tenantId);
        return ResponseEntity.ok(Map.of("status", isHealthy ? "UP" : "DOWN", "timestamp", Instant.now()));
    }
}
