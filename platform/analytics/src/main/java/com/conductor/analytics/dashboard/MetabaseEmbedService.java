package com.conductor.analytics.dashboard;

import com.conductor.shared.middleware.tenant.TenantContext;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Generates signed JWT tokens for Metabase interactive iframe embedding.
 * Per ADR-008, the JWT locks the tenant_id parameter to prevent cross-tenant data access.
 *
 * Security: The METABASE_EMBEDDING_SECRET must be rotated regularly per SECRETS_MANAGEMENT_STANDARD.
 */
@Service
public class MetabaseEmbedService {

    private static final Logger log = LoggerFactory.getLogger(MetabaseEmbedService.class);

    private final String metabaseUrl;
    private final String embeddingSecret;

    public MetabaseEmbedService(
            @Value("${analytics.metabase.url:http://localhost:3000}") String metabaseUrl,
            @Value("${analytics.metabase.embedding-secret:}") String embeddingSecret) {
        this.metabaseUrl = metabaseUrl;
        this.embeddingSecret = embeddingSecret;
    }

    /**
     * Generate a signed embed URL for a Metabase dashboard.
     *
     * @param metabaseDashboardId The numeric ID of the Metabase dashboard
     * @return Full embed URL with signed JWT token
     */
    public String generateEmbedUrl(int metabaseDashboardId) {
        if (embeddingSecret == null || embeddingSecret.isBlank()) {
            throw new IllegalStateException("METABASE_EMBEDDING_SECRET is not configured");
        }

        UUID tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new SecurityException("Tenant context required for Metabase embedding");
        }

        String token = generateToken(metabaseDashboardId, tenantId.toString());
        return metabaseUrl + "/embed/dashboard/" + token + "#bordered=true&titled=true";
    }

    /**
     * Generate a signed JWT for Metabase embedding with locked tenant_id parameter.
     */
    String generateToken(int dashboardId, String tenantId) {
        SecretKey key = Keys.hmacShaKeyFor(embeddingSecret.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("dashboard", dashboardId);

        // Lock tenant_id parameter — prevents frontend URL manipulation (ADR-008 security control)
        Map<String, String> params = new LinkedHashMap<>();
        params.put("tenant_id", tenantId);

        return Jwts.builder()
                .claim("resource", resource)
                .claim("params", params)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(10, ChronoUnit.MINUTES)))
                .claim("jti", UUID.randomUUID().toString())
                .signWith(key)
                .compact();
    }
}
