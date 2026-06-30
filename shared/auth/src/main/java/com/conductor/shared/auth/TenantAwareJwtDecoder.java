package com.conductor.shared.auth;

import com.nimbusds.jwt.JWTParser;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

/**
 * Dynamic, tenant-aware, and cached JWT decoder. Enforces security validations such as
 * issuer-prefix SSRF protection, retrieves dynamic JWKS endpoints for each Keycloak realm, and
 * records validation metrics.
 */
@Component
public class TenantAwareJwtDecoder implements JwtDecoder {

  private static final Logger log = LoggerFactory.getLogger(TenantAwareJwtDecoder.class);

  private final Map<String, JwtDecoder> decoders = new ConcurrentHashMap<>();
  private final String allowedIssuerPrefix;
  private final SecurityMetrics securityMetrics;

  public TenantAwareJwtDecoder(
      @Value("${keycloak.server-url:http://localhost:8080}") String serverUrl,
      SecurityMetrics securityMetrics) {
    this.securityMetrics = securityMetrics;

    String normalized = serverUrl.trim();
    if (!normalized.endsWith("/")) {
      normalized += "/";
    }
    this.allowedIssuerPrefix = normalized + "realms/";
    log.info(
        "Initialized TenantAwareJwtDecoder with allowed issuer prefix: '{}'", allowedIssuerPrefix);
  }

  @Override
  public Jwt decode(String token) throws JwtException {
    Instant start = Instant.now();
    try {
      // 1. Parse token claims first without verifying signature
      var jwtClaimsSet = JWTParser.parse(token).getJWTClaimsSet();
      String issuer = jwtClaimsSet.getIssuer();

      if (issuer == null || issuer.isBlank()) {
        securityMetrics.recordAuthenticationFailure();
        throw new JwtException("Missing issuer ('iss') claim in JWT");
      }

      // 2. Enforce SSRF protection - issuer must start with allowed prefix
      if (!issuer.startsWith(allowedIssuerPrefix)) {
        securityMetrics.recordAuthenticationFailure();
        log.warn(
            "Rejected JWT with disallowed issuer: '{}' (must start with '{}')",
            issuer,
            allowedIssuerPrefix);
        throw new JwtException("Issuer is not authorized by the platform configuration");
      }

      // 3. Resolve or construct NimbusJwtDecoder from cache
      JwtDecoder decoder =
          decoders.computeIfAbsent(
              issuer,
              iss -> {
                String jwkSetUri = iss + "/protocol/openid-connect/certs";
                log.info("Constructing NimbusJwtDecoder for JWKS endpoint: '{}'", jwkSetUri);
                return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
              });

      // 4. Perform actual signature and claims validation
      Jwt decoded = decoder.decode(token);

      // 5. Record metrics
      securityMetrics.recordAuthenticationSuccess();
      securityMetrics.recordTokenValidationLatency(Duration.between(start, Instant.now()));

      return decoded;

    } catch (ParseException e) {
      securityMetrics.recordAuthenticationFailure();
      throw new JwtException("Malformed JWT structure: " + e.getMessage(), e);
    } catch (JwtException e) {
      securityMetrics.recordAuthenticationFailure();
      throw e;
    } catch (Exception e) {
      securityMetrics.recordAuthenticationFailure();
      throw new JwtException("JWT validation failed: " + e.getMessage(), e);
    }
  }

  /** Clears the cached decoders. Useful for testing or key rotations. */
  public void clearCache() {
    decoders.clear();
  }
}
