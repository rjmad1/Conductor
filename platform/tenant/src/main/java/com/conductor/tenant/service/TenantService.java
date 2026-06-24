package com.conductor.tenant.service;

import com.conductor.shared.middleware.tenant.AuditLogger;
import com.conductor.shared.middleware.tenant.NatsEventPublisher;
import com.conductor.tenant.domain.Tenant;
import com.conductor.tenant.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class TenantService {

    private static final Logger log = LoggerFactory.getLogger(TenantService.class);

    private final TenantRepository tenantRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final NatsEventPublisher eventPublisher;
    private final AuditLogger auditLogger;

    public TenantService(
            TenantRepository tenantRepository,
            KeycloakAdminService keycloakAdminService,
            NatsEventPublisher eventPublisher,
            AuditLogger auditLogger) {
        this.tenantRepository = tenantRepository;
        this.keycloakAdminService = keycloakAdminService;
        this.eventPublisher = eventPublisher;
        this.auditLogger = auditLogger;
    }

    @Transactional
    public Tenant createTenant(String name, String domain, String tier) {
        log.info("Creating tenant name={} domain={} tier={}", name, domain, tier);
        
        if (tenantRepository.findByDomain(domain).isPresent()) {
            throw new IllegalArgumentException("Tenant domain already exists: " + domain);
        }

        Tenant tenant = new Tenant();
        tenant.setName(name);
        tenant.setDomain(domain);
        tenant.setSubscriptionStatus("ACTIVE");
        tenant.setSubscriptionTier(tier);
        tenant.setCreatedAt(Instant.now());
        tenant.setUpdatedAt(Instant.now());

        Tenant savedTenant = tenantRepository.save(tenant);
        String realmName = "conductor-" + savedTenant.getId();

        try {
            // Provision Keycloak Realm dynamically
            keycloakAdminService.createTenantRealm(realmName);
            keycloakAdminService.provisionDefaultRoles(realmName);
            keycloakAdminService.createClient(realmName, "conductor-frontend", true, null);
            keycloakAdminService.createClient(realmName, "conductor-backend", false, "backend_secret_" + savedTenant.getId());
        } catch (Exception e) {
            log.error("Failed to provision Keycloak resources for tenant: {}", savedTenant.getId(), e);
            throw new RuntimeException("Provisioning failed: " + e.getMessage(), e);
        }

        // Emit event & log audit record
        eventPublisher.publishEvent("tenant", "profile", "created", 
                String.format("{\"id\":\"%s\",\"name\":\"%s\",\"domain\":\"%s\"}", savedTenant.getId(), name, domain));
        
        auditLogger.logEvent("CREATE", "TENANT:" + savedTenant.getId(), "SUCCESS", "Tenant created and Keycloak realm provisioned");

        return savedTenant;
    }

    public Optional<Tenant> getTenant(UUID id) {
        return tenantRepository.findById(id);
    }

    @Transactional
    public Tenant updateTenant(UUID id, String name, String domain) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));

        tenant.setName(name);
        tenant.setDomain(domain);
        tenant.setUpdatedAt(Instant.now());
        Tenant updated = tenantRepository.save(tenant);

        eventPublisher.publishEvent("tenant", "profile", "updated", 
                String.format("{\"id\":\"%s\",\"name\":\"%s\",\"domain\":\"%s\"}", id, name, domain));
        
        auditLogger.logEvent("UPDATE", "TENANT:" + id, "SUCCESS", "Tenant profile updated");
        return updated;
    }

    @Transactional
    public void suspendTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));

        tenant.setSubscriptionStatus("SUSPENDED");
        tenant.setUpdatedAt(Instant.now());
        tenantRepository.save(tenant);

        eventPublisher.publishEvent("tenant", "profile", "suspended", String.format("{\"id\":\"%s\"}", id));
        auditLogger.logEvent("SUSPEND", "TENANT:" + id, "SUCCESS", "Tenant subscription suspended");
    }

    @Transactional
    public void deactivateTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));

        tenant.setSubscriptionStatus("DEACTIVATED");
        tenant.setUpdatedAt(Instant.now());
        tenantRepository.save(tenant);

        eventPublisher.publishEvent("tenant", "profile", "deactivated", String.format("{\"id\":\"%s\"}", id));
        auditLogger.logEvent("DEACTIVATE", "TENANT:" + id, "SUCCESS", "Tenant account deactivated");
    }

    @Transactional
    public void deleteTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));

        tenantRepository.delete(tenant);

        eventPublisher.publishEvent("tenant", "profile", "deleted", String.format("{\"id\":\"%s\"}", id));
        auditLogger.logEvent("DELETE", "TENANT:" + id, "SUCCESS", "Tenant permanently deleted");
    }
}
