package com.conductor.tenant.controller;

import com.conductor.tenant.domain.Tenant;
import com.conductor.tenant.dto.CreateTenantRequest;
import com.conductor.tenant.service.TenantService;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

  private final TenantService tenantService;

  public TenantController(TenantService tenantService) {
    this.tenantService = tenantService;
  }

  @PostMapping
  public ResponseEntity<Tenant> createTenant(@Valid @RequestBody CreateTenantRequest request) {
    Tenant tenant =
        tenantService.createTenant(
            request.getTenantKey(),
            request.getDisplayName(),
            request.getLegalName(),
            request.getDomain(),
            request.getTimezone(),
            request.getLocale(),
            request.getDefaultCurrency());
    return ResponseEntity.status(HttpStatus.CREATED).body(tenant);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Tenant> getTenant(@PathVariable UUID id) {
    Optional<Tenant> tenant = tenantService.getTenant(id);
    return tenant.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/{id}/deactivate")
  public ResponseEntity<Void> deactivateTenant(@PathVariable UUID id) {
    tenantService.deactivateTenant(id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/activate")
  public ResponseEntity<Void> activateTenant(@PathVariable UUID id) {
    tenantService.activateTenant(id);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTenant(@PathVariable UUID id) {
    tenantService.deleteTenant(id);
    return ResponseEntity.noContent().build();
  }
}
