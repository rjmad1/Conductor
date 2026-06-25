package com.conductor.tenant.service;

import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.middleware.tenant.NatsEventPublisher;
import com.conductor.tenant.domain.Tenant;
import com.conductor.tenant.domain.TenantStatus;
import com.conductor.tenant.repository.TenantRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {

  private static final Logger log = LoggerFactory.getLogger(TenantService.class);

  private final TenantRepository tenantRepository;
  private final KeycloakAdminService keycloakAdminService;
  private final NatsEventPublisher eventPublisher;
  private final AuditLogger auditLogger;
  private final TenantMetrics tenantMetrics;

  public TenantService(
      TenantRepository tenantRepository,
      KeycloakAdminService keycloakAdminService,
      NatsEventPublisher eventPublisher,
      AuditLogger auditLogger,
      TenantMetrics tenantMetrics) {
    this.tenantRepository = tenantRepository;
    this.keycloakAdminService = keycloakAdminService;
    this.eventPublisher = eventPublisher;
    this.auditLogger = auditLogger;
    this.tenantMetrics = tenantMetrics;
  }

  @Transactional
  public Tenant createTenant(
      String tenantKey,
      String displayName,
      String legalName,
      String domain,
      String timezone,
      String locale,
      String defaultCurrency) {
    log.info(
        "Creating tenant tenantKey={} displayName={} domain={}", tenantKey, displayName, domain);

    if (tenantRepository.findByTenantKey(tenantKey).isPresent()) {
      throw new IllegalArgumentException("Tenant key already exists: " + tenantKey);
    }
    if (tenantRepository.findByDomain(domain).isPresent()) {
      throw new IllegalArgumentException("Tenant domain already exists: " + domain);
    }

    Tenant tenant = new Tenant();
    tenant.setTenantKey(tenantKey);
    tenant.setDisplayName(displayName);
    tenant.setLegalName(legalName);
    tenant.setDomain(domain);
    tenant.setStatus(TenantStatus.ACTIVE);
    if (timezone != null && !timezone.isBlank()) {
      tenant.setTimezone(timezone);
    }
    if (locale != null && !locale.isBlank()) {
      tenant.setLocale(locale);
    }
    if (defaultCurrency != null && !defaultCurrency.isBlank()) {
      tenant.setDefaultCurrency(defaultCurrency);
    }
    tenant.setCreatedAt(Instant.now());
    tenant.setUpdatedAt(Instant.now());

    Tenant savedTenant = tenantRepository.save(tenant);
    String realmName = "conductor-" + savedTenant.getId();

    try {
      // Provision Keycloak Realm dynamically
      keycloakAdminService.createTenantRealm(realmName);
      keycloakAdminService.provisionDefaultRoles(realmName);
      keycloakAdminService.createClient(realmName, "conductor-frontend", true, null);
      keycloakAdminService.createClient(
          realmName, "conductor-backend", false, "backend_secret_" + savedTenant.getId());
    } catch (Exception e) {
      log.error("Failed to provision Keycloak resources for tenant: {}", savedTenant.getId(), e);
      throw new RuntimeException("Provisioning failed: " + e.getMessage(), e);
    }

    // Emit event & log audit record
    eventPublisher.publishEvent(
        "tenant",
        "profile",
        "created",
        String.format(
            "{\"id\":\"%s\",\"name\":\"%s\",\"domain\":\"%s\"}",
            savedTenant.getId(), savedTenant.getDisplayName(), savedTenant.getDomain()));

    auditLogger.logEvent(
        "CREATE",
        "TENANT:" + savedTenant.getId(),
        "SUCCESS",
        "Tenant created and Keycloak realm provisioned");

    tenantMetrics.recordTenantCreated();

    return savedTenant;
  }

  public Optional<Tenant> getTenant(UUID id) {
    tenantMetrics.recordTenantRetrieval();
    return tenantRepository.findById(id);
  }

  public List<Tenant> listTenants(UUID startingAfter, int limit) {
    tenantMetrics.recordTenantRetrieval();
    return tenantRepository.findAllPaged(startingAfter, PageRequest.of(0, limit));
  }

  @Transactional
  public Tenant updateTenant(
      UUID id,
      String displayName,
      String legalName,
      String domain,
      String timezone,
      String locale,
      String defaultCurrency) {
    Tenant tenant =
        tenantRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));

    if (tenant.getStatus() == TenantStatus.DELETED) {
      throw new IllegalStateException("Deleted tenants cannot be modified");
    }

    if (domain != null && !domain.equals(tenant.getDomain())) {
      if (tenantRepository.findByDomain(domain).isPresent()) {
        throw new IllegalArgumentException("Tenant domain already exists: " + domain);
      }
      tenant.setDomain(domain);
    }

    if (displayName != null) {
      tenant.setDisplayName(displayName);
    }
    if (legalName != null) {
      tenant.setLegalName(legalName);
    }
    if (timezone != null) {
      tenant.setTimezone(timezone);
    }
    if (locale != null) {
      tenant.setLocale(locale);
    }
    if (defaultCurrency != null) {
      tenant.setDefaultCurrency(defaultCurrency);
    }

    tenant.setUpdatedAt(Instant.now());
    Tenant updated = tenantRepository.save(tenant);

    eventPublisher.publishEvent(
        "tenant",
        "profile",
        "updated",
        String.format(
            "{\"id\":\"%s\",\"name\":\"%s\",\"domain\":\"%s\"}",
            updated.getId(), updated.getDisplayName(), updated.getDomain()));

    auditLogger.logEvent("UPDATE", "TENANT:" + id, "SUCCESS", "Tenant profile updated");
    tenantMetrics.recordTenantUpdated();

    return updated;
  }

  @Transactional
  public void deactivateTenant(UUID id) {
    Tenant tenant =
        tenantRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));

    if (tenant.getStatus() == TenantStatus.DELETED) {
      throw new IllegalStateException("Deleted tenants cannot be modified");
    }

    tenant.setStatus(TenantStatus.DEACTIVATED);
    tenant.setUpdatedAt(Instant.now());
    tenantRepository.save(tenant);

    eventPublisher.publishEvent(
        "tenant", "profile", "deactivated", String.format("{\"id\":\"%s\"}", id));
    auditLogger.logEvent("DEACTIVATE", "TENANT:" + id, "SUCCESS", "Tenant account deactivated");
    tenantMetrics.recordTenantUpdated();
  }

  @Transactional
  public void activateTenant(UUID id) {
    Tenant tenant =
        tenantRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));

    if (tenant.getStatus() == TenantStatus.DELETED) {
      throw new IllegalStateException("Deleted tenants cannot be modified");
    }

    tenant.setStatus(TenantStatus.ACTIVE);
    tenant.setUpdatedAt(Instant.now());
    tenantRepository.save(tenant);

    eventPublisher.publishEvent(
        "tenant", "profile", "activated", String.format("{\"id\":\"%s\"}", id));
    auditLogger.logEvent("ACTIVATE", "TENANT:" + id, "SUCCESS", "Tenant account activated");
    tenantMetrics.recordTenantUpdated();
  }

  @Transactional
  public void deleteTenant(UUID id) {
    Tenant tenant =
        tenantRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));

    if (tenant.getStatus() == TenantStatus.DELETED) {
      throw new IllegalStateException("Deleted tenants cannot be modified");
    }

    tenant.setStatus(TenantStatus.DELETED);
    tenant.setDeletedAt(Instant.now());
    tenant.setUpdatedAt(Instant.now());
    tenantRepository.save(tenant);

    eventPublisher.publishEvent(
        "tenant", "profile", "deleted", String.format("{\"id\":\"%s\"}", id));
    auditLogger.logEvent("DELETE", "TENANT:" + id, "SUCCESS", "Tenant soft deleted");
    tenantMetrics.recordTenantUpdated();
  }
}
