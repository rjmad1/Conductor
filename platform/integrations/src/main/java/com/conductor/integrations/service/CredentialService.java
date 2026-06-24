package com.conductor.integrations.service;

import com.conductor.integrations.domain.*;
import com.conductor.integrations.repository.*;
import com.conductor.integrations.framework.CredentialEncryptor;
import com.conductor.shared.middleware.tenant.TenantContext;
import com.conductor.shared.middleware.tenant.AuditLogger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class CredentialService {

    private final CredentialRepository credentialRepository;
    private final OAuthConnectionRepository oauthConnectionRepository;
    private final IntegrationRepository integrationRepository;
    private final CredentialEncryptor encryptor;
    private final AuditLogger auditLogger;

    public CredentialService(
            CredentialRepository credentialRepository,
            OAuthConnectionRepository oauthConnectionRepository,
            IntegrationRepository integrationRepository,
            CredentialEncryptor encryptor,
            AuditLogger auditLogger) {
        this.credentialRepository = credentialRepository;
        this.oauthConnectionRepository = oauthConnectionRepository;
        this.integrationRepository = integrationRepository;
        this.encryptor = encryptor;
        this.auditLogger = auditLogger;
    }

    @Transactional
    public Credential saveCredential(UUID integrationId, String authType, String rawPayload) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        Integration integration = integrationRepository.findByIdAndTenantId(integrationId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));

        Optional<Credential> existing = credentialRepository.findByIntegrationIdAndTenantId(integrationId, tenantId);
        Credential credential = existing.orElse(new Credential());
        credential.setIntegration(integration);
        credential.setAuthType(authType);
        credential.setEncryptedPayload(encryptor.encrypt(rawPayload));
        credential.setUpdatedAt(Instant.now());

        Credential saved = credentialRepository.save(credential);
        auditLogger.logEvent("CREDENTIAL_SAVED", "integration:" + integrationId, "SUCCESS", "Credential saved for authType: " + authType);
        return saved;
    }

    @Transactional(readOnly = true)
    public String getDecryptedPayload(UUID integrationId) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return credentialRepository.findByIntegrationIdAndTenantId(integrationId, tenantId)
                .map(c -> encryptor.decrypt(c.getEncryptedPayload()))
                .orElse(null);
    }

    @Transactional
    public OAuthConnection saveOAuthConnection(UUID integrationId, String accessToken, String refreshToken, Instant expiresAt, String scope) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        Integration integration = integrationRepository.findByIdAndTenantId(integrationId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));

        Optional<OAuthConnection> existing = oauthConnectionRepository.findByIntegrationIdAndTenantId(integrationId, tenantId);
        OAuthConnection oauth = existing.orElse(new OAuthConnection());
        oauth.setIntegration(integration);
        oauth.setAccessToken(encryptor.encrypt(accessToken));
        oauth.setRefreshToken(encryptor.encrypt(refreshToken));
        oauth.setExpiresAt(expiresAt);
        oauth.setScope(scope);
        oauth.setUpdatedAt(Instant.now());

        OAuthConnection saved = oauthConnectionRepository.save(oauth);
        auditLogger.logEvent("OAUTH_CONNECTION_SAVED", "integration:" + integrationId, "SUCCESS", "OAuth connection saved");
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<OAuthConnection> getOAuthConnection(UUID integrationId) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return oauthConnectionRepository.findByIntegrationIdAndTenantId(integrationId, tenantId)
                .map(conn -> {
                    OAuthConnection decrypted = new OAuthConnection();
                    decrypted.setId(conn.getId());
                    decrypted.setIntegration(conn.getIntegration());
                    decrypted.setAccessToken(encryptor.decrypt(conn.getAccessToken()));
                    decrypted.setRefreshToken(encryptor.decrypt(conn.getRefreshToken()));
                    decrypted.setExpiresAt(conn.getExpiresAt());
                    decrypted.setScope(conn.getScope());
                    return decrypted;
                });
    }

    @Transactional
    public void rotateCredential(UUID integrationId, String newRawPayload) {
        saveCredential(integrationId, "API_KEY", newRawPayload);
        auditLogger.logEvent("CREDENTIAL_ROTATED", "integration:" + integrationId, "SUCCESS", "Rotated key credential");
    }

    @Transactional(readOnly = true)
    public boolean validateCredential(UUID integrationId) {
        String payload = getDecryptedPayload(integrationId);
        if (payload != null && !payload.trim().isEmpty()) {
            return true;
        }
        Optional<OAuthConnection> oauth = getOAuthConnection(integrationId);
        return oauth.isPresent() && oauth.get().getAccessToken() != null;
    }
}
