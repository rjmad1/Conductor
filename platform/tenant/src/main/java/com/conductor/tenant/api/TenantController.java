package com.conductor.tenant.api;

import com.conductor.shared.middleware.tenant.TenantContext;
import com.conductor.tenant.domain.Tenant;
import com.conductor.tenant.service.TenantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        Tenant tenant = tenantService.createTenant(request.name(), request.domain(), request.subscriptionTier());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(tenant));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_PLATFORM_ADMIN', 'ROLE_TENANT_ADMIN')")
    public ResponseEntity<TenantResponse> getTenant(@PathVariable UUID id) {
        // Multi-tenant boundary check: Tenant Admin can only query their own tenant context
        UUID activeTenantId = TenantContext.getCurrentTenantId();
        if (activeTenantId != null && !activeTenantId.equals(id)) {
            // Hide existence of cross-tenant resource: return 404
            return ResponseEntity.notFound().build();
        }

        return tenantService.getTenant(id)
                .map(tenant -> ResponseEntity.ok(toResponse(tenant)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_PLATFORM_ADMIN', 'ROLE_TENANT_ADMIN')")
    public ResponseEntity<TenantResponse> updateTenant(@PathVariable UUID id, @Valid @RequestBody UpdateTenantRequest request) {
        UUID activeTenantId = TenantContext.getCurrentTenantId();
        if (activeTenantId != null && !activeTenantId.equals(id)) {
            return ResponseEntity.notFound().build();
        }

        Tenant tenant = tenantService.updateTenant(id, request.name(), request.domain());
        return ResponseEntity.ok(toResponse(tenant));
    }

    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<Void> suspendTenant(@PathVariable UUID id) {
        tenantService.suspendTenant(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<Void> deleteTenant(@PathVariable UUID id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }

    private TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getDomain(),
                tenant.getSubscriptionStatus(),
                tenant.getSubscriptionTier()
        );
    }

    public record CreateTenantRequest(
            @NotBlank String name,
            @NotBlank String domain,
            @NotBlank String subscriptionTier
    ) {}

    public record UpdateTenantRequest(
            @NotBlank String name,
            @NotBlank String domain
    ) {}

    public record TenantResponse(
            UUID id,
            String name,
            String domain,
            String subscriptionStatus,
            String subscriptionTier
    ) {}
}
