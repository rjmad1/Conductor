package com.conductor.integrations.api;

import com.conductor.integrations.domain.*;
import com.conductor.integrations.framework.ConnectorAdapter;
import com.conductor.integrations.framework.ConnectorHealthResult;
import com.conductor.integrations.framework.ConnectorRegistry;
import com.conductor.integrations.framework.CredentialEncryptor;
import com.conductor.integrations.framework.OAuthStateStore;
import com.conductor.integrations.repository.*;
import com.conductor.integrations.service.CredentialService;
import com.conductor.integrations.service.OAuthTokenExchangeService;
import com.conductor.shared.messaging.EventPublisher;
import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.middleware.tenant.TenantContext;
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
    private final OAuthStateStore oauthStateStore;
    private final CredentialEncryptor credentialEncryptor;
    private final OAuthTokenExchangeService oauthTokenExchangeService;

    public IntegrationController(
            ConnectorRepository connectorRepository,
            IntegrationRepository integrationRepository,
            ConnectionRepository connectionRepository,
            WebhookSubscriptionRepository webhookSubscriptionRepository,
            ExecutionHistoryRepository executionHistoryRepository,
            CredentialService credentialService,
            ConnectorRegistry connectorRegistry,
            AuditLogger auditLogger,
            EventPublisher eventPublisher,
            OAuthStateStore oauthStateStore,
            CredentialEncryptor credentialEncryptor,
            OAuthTokenExchangeService oauthTokenExchangeService) {
        this.connectorRepository = connectorRepository;
        this.integrationRepository = integrationRepository;
        this.connectionRepository = connectionRepository;
        this.webhookSubscriptionRepository = webhookSubscriptionRepository;
        this.executionHistoryRepository = executionHistoryRepository;
        this.credentialService = credentialService;
        this.connectorRegistry = connectorRegistry;
        this.auditLogger = auditLogger;
        this.eventPublisher = eventPublisher;
        this.oauthStateStore = oauthStateStore;
        this.credentialEncryptor = credentialEncryptor;
        this.oauthTokenExchangeService = oauthTokenExchangeService;
    }

    @GetMapping("/connectors")
    public ResponseEntity<List<Connector>> listConnectors() {
        return ResponseEntity.ok(connectorRepository.findAll());
    }

    @PostMapping("/credentials")
    public ResponseEntity<Credential> saveCredential(@RequestParam UUID integrationId,
            @RequestParam String authType, @RequestBody String rawPayload) {
        Credential cred = credentialService.saveCredential(integrationId, authType, rawPayload);
        eventPublisher.publish("integration", "credential", "updated", "v1",
                Map.of("integrationId", integrationId, "authType", authType));
        return ResponseEntity.status(HttpStatus.CREATED).body(cred);
    }

    /**
     * Generates a random server-side state token (CSRF protection) and returns the
     * authorization URL. The integrationId never round-trips through the browser.
     */
    @PostMapping("/oauth/authorize")
    public ResponseEntity<Map<String, String>> authorizeOAuth(@RequestParam UUID integrationId,
            @RequestParam String authUrl) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        integrationRepository.findByIdAndTenantId(integrationId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));
        String stateToken = oauthStateStore.generate(integrationId, tenantId);
        auditLogger.logEvent("OAUTH_AUTH_STARTED", "integration:" + integrationId, "SUCCESS",
                "OAuth authorization initiated");
        return ResponseEntity.ok(Map.of("authorizationUrl", authUrl + "?state=" + stateToken));
    }

    /**
     * Validates the opaque state token (single-use, TTL-bounded), derives
     * integrationId/tenantId from the server-side store, and exchanges the
     * authorization code for real tokens. Mock tokens are never stored.
     */
    @GetMapping("/oauth/callback")
    public ResponseEntity<String> oauthCallback(@RequestParam String code,
            @RequestParam("state") String stateToken,
            @RequestParam(value = "redirect_uri", required = false,
                    defaultValue = "/api/v1/integrations/oauth/callback") String redirectUri) {
        OAuthStateStore.PendingState pendingState = oauthStateStore.consume(stateToken).orElse(null);
        if (pendingState == null) {
            auditLogger.logEvent("OAUTH_CALLBACK_REJECTED", "oauth:callback", "FAILURE",
                    "Invalid or expired state token");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OAuth state");
        }
        UUID integrationId = pendingState.integrationId();
        UUID tenantId = pendingState.tenantId();
        Integration integration = integrationRepository.findByIdAndTenantId(integrationId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));
        TenantContext.setCurrentTenantId(tenantId);
        try {
            oauthTokenExchangeService.exchange(integrationId, tenantId, code, redirectUri);
            Optional<Connection> existingConn = connectionRepository.findByIntegrationId(integrationId);
            Connection conn = existingConn.orElse(new Connection());
            conn.setIntegration(integration);
            conn.setStatus("CONNECTED");
            conn.setLastConnectedAt(Instant.now());
            connectionRepository.save(conn);
            auditLogger.logEvent("OAUTH_CALLBACK_SUCCESS", "integration:" + integrationId, "SUCCESS",
                    "OAuth authorization succeeded");
            eventPublisher.publish("integration", "integration", "connected", "v1",
                    Map.of("integrationId", integrationId));
            return ResponseEntity.ok("Successfully authorized " + integration.getName());
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Encrypts the webhook shared secret at rest before persisting.
     * Decryption happens inside WebhookIngressController during signature validation.
     */
    @PostMapping("/webhooks")
    public ResponseEntity<WebhookSubscription> subscribeWebhook(@RequestParam UUID integrationId,
            @RequestParam String eventName, @RequestParam String targetUrl) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        Integration integration = integrationRepository.findByIdAndTenantId(integrationId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));
        String rawSecret = "secret-" + UUID.randomUUID();
        WebhookSubscription sub = new WebhookSubscription();
        sub.setIntegration(integration);
        sub.setEventName(eventName);
        sub.setTargetUrl(targetUrl);
        sub.setSecret(credentialEncryptor.encrypt(rawSecret));
        sub.setStatus("ACTIVE");
        WebhookSubscription saved = webhookSubscriptionRepository.save(sub);
        auditLogger.logEvent("WEBHOOK_REGISTERED", "integration:" + integrationId, "SUCCESS",
                "Registered webhook: " + eventName);
        connectorRegistry.getAdapter(integration.getConnector().getType())
                .ifPresent(adapter -> adapter.subscribe(tenantId, eventName, targetUrl));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<Object> executeAction(@PathVariable UUID id, @RequestParam String action,
            @RequestBody Map<String, Object> payload) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        Integration integration = integrationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));
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
        } catch (UnsupportedOperationException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            errorMessage = e.getMessage();
            throw e;
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
        Integration integration = integrationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));
        ConnectorAdapter adapter = connectorRegistry.getAdapter(integration.getConnector().getType())
                .orElseThrow(() -> new IllegalArgumentException("Connector adapter not found"));
        ConnectorHealthResult result = adapter.healthCheck(tenantId);
        return ResponseEntity.ok(Map.of(
                "status", result.status().name(),
                "message", result.message(),
                "latencyMs", result.latencyMs(),
                "timestamp", Instant.now()));
    }
}
